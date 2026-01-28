package org.sid.authentification.services;

import org.sid.authentification.entities.Role;
import org.sid.authentification.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(null, "ADMIN"));
            roleRepository.save(new Role(null, "STATION-SERVICE"));
            roleRepository.save(new Role(null, "LOGISTIQUE"));
            roleRepository.save(new Role(null, "CHAUFFEUR"));
            System.out.println("Rôles initialisés");
        }
    }
}