package edu.hawaii.its.filedrop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Override
    List<Role> findAll();

    Optional<Role> findById(Integer id);

    Role findByRole(String role);

    List<Role> findBySecurityRole(String securityRole);

    boolean existsBySecurityRole(String securityRole);
}
