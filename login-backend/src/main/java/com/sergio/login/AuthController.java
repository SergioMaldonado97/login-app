package com.sergio.login;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UsuarioRepository usuarioRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<Usuario> usuario = usuarioRepository.findByUsername(username);

        if (usuario.isPresent() && passwordEncoder.matches(password, usuario.get().getPassword())) {
            String token = jwtUtil.generarToken(usuario.get().getUsername(), usuario.get().getRol());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login exitoso",
                "token", token,
                "user", usuario.get().getNombre(),
                "rol", usuario.get().getRol(),
                "username", usuario.get().getUsername()
            ));
        }
        return ResponseEntity.status(401).body(Map.of(
            "success", false,
            "message", "Usuario o contraseña incorrectos"
        ));
    }
}
