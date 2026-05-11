package com.sergio.login;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository repo;

    public DataInitializer(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() == 0) {
            repo.save(new Usuario("admin",    "1234",       "Administrador",    "ADMIN"));
            repo.save(new Usuario("sergio",   "sergio123",  "Sergio Maldonado", "ADMIN"));
            repo.save(new Usuario("invitado", "invitado",   "Usuario Invitado", "REGULAR"));
            System.out.println("Usuarios de prueba creados en la base de datos.");
        }
    }
}
