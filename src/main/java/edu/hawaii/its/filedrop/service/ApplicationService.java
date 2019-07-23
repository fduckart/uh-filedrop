package edu.hawaii.its.filedrop.service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.hawaii.its.filedrop.repository.AdministratorRepository;
import edu.hawaii.its.filedrop.repository.OfficeRepository;
import edu.hawaii.its.filedrop.type.Administrator;
import edu.hawaii.its.filedrop.type.Office;
import edu.hawaii.its.filedrop.type.Role;
import edu.hawaii.its.filedrop.type.Role.SecurityRole;

@Service
@Transactional
public class ApplicationService {

    private static final Log logger = LogFactory.getLog(ApplicationService.class);

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @PostConstruct
    public void init() {
        logger.info("init starting");

        logger.info("init finished");
    }

    @Cacheable(value = "offices")
    public List<Office> findOffices() {
        return officeRepository.findAll(new Sort("sortId"));
    }

    @Cacheable(value = "officesById", key = "#id")
    public Office findOffice(Integer id) {
        return officeRepository.findById(id).get();
    }

    @CachePut(value = "officesById", key = "#result.id")
    @CacheEvict(value = "offices", allEntries = true)
    public Office addOffice(Office office) {
        return officeRepository.save(office);
    }

    @Caching(evict = {
            @CacheEvict(value = "offices", allEntries = true),
            @CacheEvict(value = "officesById", allEntries = true) })
    public void evictOfficeCaches() {
        // Empty.
    }

    public Set<SecurityRole> findSystemRoles(String uhUuid) {

        Set<SecurityRole> roleSet = new TreeSet<>();

        List<Administrator> admins = administratorRepository.findAllByPersonUhUuid(uhUuid);
        for (Administrator a : admins) {
            roleSet.add(toSecurityRole(a.getRole()));
        }

        return roleSet;
    }

    private SecurityRole toSecurityRole(Role role) {
        return SecurityRole.valueOf(role.getSecurityRole());
    }

    public List<Administrator> findAdministrators() {
        return administratorRepository.findAll();
    }

    public boolean isAdministrator(String uhUuid) {
        return administratorRepository.isAdministrator(uhUuid);
    }

    public boolean isSuperuser(String uhUuid) {
        return administratorRepository.isSuperuser(uhUuid);
    }

}
