package com.sergio.login;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioRepository repo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioController(UsuarioRepository repo,
                             BCryptPasswordEncoder passwordEncoder,
                             AuditoriaService auditoriaService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<UsuarioDTO> getAll() {
        return repo.findAll().stream()
            .map(u -> new UsuarioDTO(u.getId(), u.getUsername(), u.getNombre(), u.getRol()))
            .toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body,
                                    HttpServletRequest request) {
        String username = body.get("username");
        String password = body.get("password");
        String nombre   = body.get("nombre");
        String rol      = body.getOrDefault("rol", "REGULAR");

        if (username == null || password == null || nombre == null ||
            username.isBlank() || password.isBlank() || nombre.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Todos los campos son obligatorios"));
        }
        if (!rol.equals("ADMIN") && !rol.equals("REGULAR")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Rol inválido"));
        }
        if (repo.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "El usuario ya existe"));
        }

        Usuario saved = repo.save(new Usuario(username, passwordEncoder.encode(password), nombre, rol));
        UsuarioDTO dto = new UsuarioDTO(saved.getId(), saved.getUsername(), saved.getNombre(), saved.getRol());

        String actor = getActor();
        auditoriaService.registrar(
                EntidadAuditable.USUARIO, saved.getId(), AccionAuditoria.CREAR,
                null, dto, actor, request, null);

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Map<String, String> body,
                                    HttpServletRequest request) {
        Optional<Usuario> opt = repo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Usuario u = opt.get();

        // Capturar estado anterior ANTES de aplicar cambios
        UsuarioDTO anterior = new UsuarioDTO(u.getId(), u.getUsername(), u.getNombre(), u.getRol());

        String nombre   = body.getOrDefault("nombre",   u.getNombre());
        String password = body.getOrDefault("password", u.getPassword());
        String rol      = body.getOrDefault("rol",      u.getRol());

        if (!rol.equals("ADMIN") && !rol.equals("REGULAR")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Rol inválido"));
        }

        Usuario updated = new Usuario(
                u.getUsername(),
                password.isBlank() ? u.getPassword() : passwordEncoder.encode(password),
                nombre,
                rol);
        setId(updated, id);
        repo.save(updated);

        UsuarioDTO posterior = new UsuarioDTO(id, updated.getUsername(), updated.getNombre(), updated.getRol());
        String detalle = construirDetalleEdicion(anterior, posterior, body);

        String actor = getActor();
        auditoriaService.registrar(
                EntidadAuditable.USUARIO, id, AccionAuditoria.MODIFICAR,
                anterior, posterior, actor, request, detalle);

        return ResponseEntity.ok(posterior);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Optional<Usuario> opt = repo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Capturar estado anterior ANTES de eliminar
        Usuario aEliminar = opt.get();
        UsuarioDTO anteriorDto = new UsuarioDTO(
                aEliminar.getId(), aEliminar.getUsername(),
                aEliminar.getNombre(), aEliminar.getRol());

        repo.deleteById(id);

        String actor = getActor();
        auditoriaService.registrar(
                EntidadAuditable.USUARIO, id, AccionAuditoria.ELIMINAR,
                anteriorDto, null, actor, request, null);

        return ResponseEntity.ok(Map.of("message", "Usuario eliminado"));
    }

    private String getActor() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "DESCONOCIDO";
        }
    }

    private String construirDetalleEdicion(UsuarioDTO ant, UsuarioDTO nvo, Map<String, String> body) {
        List<String> cambios = new ArrayList<>();
        if (!ant.rol().equals(nvo.rol())) {
            cambios.add("rol: " + ant.rol() + " -> " + nvo.rol());
        }
        if (body.containsKey("password") && !body.get("password").isBlank()) {
            cambios.add("password actualizado");
        }
        return cambios.isEmpty() ? null : String.join(", ", cambios);
    }

    private void setId(Usuario u, Long id) {
        try {
            var field = Usuario.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(u, id);
        } catch (Exception ignored) {}
    }

    public record UsuarioDTO(Long id, String username, String nombre, String rol) {}
}
