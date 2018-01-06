package edu.hawaii.its.filedrop.repository;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.hawaii.its.filedrop.type.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    @Override
    @Cacheable(value = "rolesCache")
    List<Role> findAll();

    @Cacheable(value = "rolesByIdCache")
    Role findById(Integer id);

    Role findByRole(String role);
}
