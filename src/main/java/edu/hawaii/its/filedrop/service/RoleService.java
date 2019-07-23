package edu.hawaii.its.filedrop.service;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import edu.hawaii.its.filedrop.repository.RoleRepository;
import edu.hawaii.its.filedrop.type.Role;
import edu.hawaii.its.filedrop.type.Role.SecurityRole;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void init() {
        // Make sure the roles in the database match
        // up with the enums we need for the
        // general programming in this application.
        // The first and last SecurityRole is
        // not stored in the database currently.
        SecurityRole[] roles = SecurityRole.values();
        for (int i = 1; i < roles.length - 1; i++) {
            SecurityRole r = roles[i];
            boolean exists = roleRepository.existsBySecurityRole(r.name());
            Assert.isTrue(exists, "Security Role not found: " + r.name());
        }

        for (Role r : roleRepository.findAll()) {
            SecurityRole role = SecurityRole.find(r.getSecurityRole());
            Assert.notNull(role, "Role (enum) not found: " + role);
        }
    }

    public long count() {
        return roleRepository.count();
    }

    @Cacheable(value = "rolesCache")
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Cacheable(value = "rolesByIdCache")
    public Role findById(Integer id) {
        return roleRepository.findById(id).get();
    }

    public Role findByRole(String role) {
        return roleRepository.findByRole(role);
    }

    List<Role> findBySecurityRole(String securityRole) {
        return roleRepository.findBySecurityRole(securityRole);
    }

    public boolean existsBySecurityRole(String securityRole) {
        return roleRepository.existsBySecurityRole(securityRole);
    }

    public Role save(Role role) {
        return roleRepository.save(role);
    }

}
