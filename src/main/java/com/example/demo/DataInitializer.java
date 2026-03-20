package com.example.demo;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.IRoleRepository;
import com.example.demo.repository.IUserRepository;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Initialize Roles if not present
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            adminRole.setDescription("Administrator role");
            roleRepository.save(adminRole);

            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("User role");
            roleRepository.save(userRole);
            
            Role managerRole = new Role();
            managerRole.setName("MANAGER");
            managerRole.setDescription("Manager role");
            roleRepository.save(managerRole);
        }

        // Initialize Admin User if not present
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123"); // Password will be encoded by userService.save
            admin.setEmail("admin@example.com");
            admin.setPhone("0123456789");
            
            // Find the ADMIN role we just created (or that already exists)
            Role adminRole = roleRepository.findAll().stream()
                    .filter(r -> r.getName().equals("ADMIN"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);
            
            userService.save(admin);
        }
    }
}
