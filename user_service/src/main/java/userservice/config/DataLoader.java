package userservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import userservice.model.ERole;
import userservice.model.Role;
import userservice.repository.RoleRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Autowired
    public DataLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Добавляем роли в базу данных, если они еще не существуют
        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_USER));
        }
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
        }
    }
}