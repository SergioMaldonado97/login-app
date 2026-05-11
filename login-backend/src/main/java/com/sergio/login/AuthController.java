package com.sergio.login;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AuditoriaService auditoriaService;

    public AuthController(UsuarioRepository usuarioRepository,
                          BCryptPasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                   HttpServletRequest request) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<Usuario> usuario = usuarioRepository.findByUsername(username);

        if (usuario.isPresent() && passwordEncoder.matches(password, usuario.get().getPassword())) {
            String token = jwtUtil.generarToken(usuario.get().getUsername(), usuario.get().getRol());
            auditoriaService.registrar(
                    EntidadAuditable.SESION, null, AccionAuditoria.LOGIN_EXITOSO,
                    null, null, usuario.get().getUsername(), request, null);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login exitoso",
                "token", token,
                "user", usuario.get().getNombre(),
                "rol", usuario.get().getRol(),
                "username", usuario.get().getUsername()
            ));
        }

        String usernameAuditado = username != null
                ? username.substring(0, Math.min(username.length(), 100))
                : "DESCONOCIDO";
        auditoriaService.registrar(
                EntidadAuditable.SESION, null, AccionAuditoria.LOGIN_FALLIDO,
                null, null, usernameAuditado, request, "Credenciales inválidas");

        return ResponseEntity.status(401).body(Map.of(
            "success", false,
            "message", "Usuario o contraseña incorrectos"
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String detalle = request.getParameter("detalle");
        auditoriaService.registrar(
                EntidadAuditable.SESION, null, AccionAuditoria.LOGOUT,
                null, null, username, request, detalle);
        return ResponseEntity.ok(Map.of("message", "Logout registrado"));
    }
}
