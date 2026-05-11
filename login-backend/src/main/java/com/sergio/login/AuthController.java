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

    public AuthController(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<Usuario> usuario = usuarioRepository.findByUsername(username);

        if (usuario.isPresent() && passwordEncoder.matches(password, usuario.get().getPassword())) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login exitoso",
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
