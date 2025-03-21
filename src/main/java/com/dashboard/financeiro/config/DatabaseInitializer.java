package com.dashboard.financeiro.config;

import com.dashboard.financeiro.model.Role;
import com.dashboard.financeiro.model.Role.RoleName;
import com.dashboard.financeiro.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Verifica e cria as roles padrão se não existirem
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                System.out.println("Role " + roleName + " criada com sucesso!");
            }
        }
    }
}
