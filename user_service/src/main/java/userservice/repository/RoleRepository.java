package userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import userservice.model.ERole;
import userservice.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(ERole name);
}
