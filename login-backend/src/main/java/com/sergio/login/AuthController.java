package com.sergio.login;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<Usuario> usuario = usuarioRepository.findByUsername(username);

        if (usuario.isPresent() && usuario.get().getPassword().equals(password)) {
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
