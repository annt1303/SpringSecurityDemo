package com.security.demo.config;

import com.security.demo.entity.Role;
import com.security.demo.entity.enums.RoleName;
import com.security.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName)
                    .orElseGet(() ->
                            roleRepository.save(
                                    Role.builder().name(roleName).build()
                            )
                    );
        }

    }
}

