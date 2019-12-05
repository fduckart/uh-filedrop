package edu.hawaii.its.filedrop.service;

import java.util.List;
import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.hawaii.its.filedrop.repository.OfficeRepository;
import edu.hawaii.its.filedrop.type.Office;

@Service
@Transactional
public class ApplicationService {

    private static final Log logger = LogFactory.getLog(ApplicationService.class);

    @Autowired
    private OfficeRepository officeRepository;

    @PostConstruct
    public void init() {
        logger.info("init starting");

        logger.info("init finished");
    }

    @Cacheable(value = "offices")
    public List<Office> findOffices() {
        return officeRepository.findAllByOrderBySortId();
    }

    @Cacheable(value = "officesById", key = "#id")
    public Office findOffice(Integer id) {
        return officeRepository.findById(id).orElse(null);
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
}
