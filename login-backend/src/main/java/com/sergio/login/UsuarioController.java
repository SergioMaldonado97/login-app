package com.sergio.login;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioController {

    private final UsuarioRepository repo;

    public UsuarioController(UsuarioRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<UsuarioDTO> getAll() {
        return repo.findAll().stream()
            .map(u -> new UsuarioDTO(u.getId(), u.getUsername(), u.getNombre(), u.getRol()))
            .toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
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
        Usuario saved = repo.save(new Usuario(username, password, nombre, rol));
        return ResponseEntity.ok(new UsuarioDTO(saved.getId(), saved.getUsername(), saved.getNombre(), saved.getRol()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<Usuario> opt = repo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Usuario u = opt.get();
        String nombre   = body.getOrDefault("nombre",   u.getNombre());
        String password = body.getOrDefault("password", u.getPassword());
        String rol      = body.getOrDefault("rol",      u.getRol());

        if (!rol.equals("ADMIN") && !rol.equals("REGULAR")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Rol inválido"));
        }

        Usuario updated = new Usuario(u.getUsername(), password.isBlank() ? u.getPassword() : password, nombre, rol);
        setId(updated, id);
        repo.save(updated);
        return ResponseEntity.ok(new UsuarioDTO(id, updated.getUsername(), updated.getNombre(), updated.getRol()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado"));
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
