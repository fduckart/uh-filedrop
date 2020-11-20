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

import edu.hawaii.its.filedrop.repository.FaqRepository;
import edu.hawaii.its.filedrop.repository.OfficeRepository;
import edu.hawaii.its.filedrop.repository.SettingRepository;
import edu.hawaii.its.filedrop.type.Faq;
import edu.hawaii.its.filedrop.type.Office;
import edu.hawaii.its.filedrop.type.Setting;

@Service
@Transactional
public class ApplicationService {

    private static final Log logger = LogFactory.getLog(ApplicationService.class);

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private FaqRepository faqRepository;

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
            @CacheEvict(value = "officesById", allEntries = true)})
    public void evictOfficeCaches() {
        // Empty.
    }

    @Cacheable(value = "settings")
    public List<Setting> findSettings() {
        return settingRepository.findAll();
    }

    @Cacheable(value = "settingsById", key = "#id")
    public Setting findSetting(Integer id) {
        return settingRepository.findById(id).orElse(null);
    }

    @CachePut(value = "settingsById", key = "#result.id")
    @CacheEvict(value = "settings", allEntries = true)
    public Setting saveSetting(Setting setting) {
        return settingRepository.save(setting);
    }

    @Caching(evict = @CacheEvict(value = "settings", allEntries = true))
    public void evictSettingCache() {
        // Empty.
    }

    @Cacheable(value = "faqCache")
    public List<Faq> findFaqs() {
        return faqRepository.findAll();
    }

    @Cacheable(value = "faqById", key = "#id")
    public Faq findFaq(Integer id) {
        return faqRepository.findById(id).orElse(null);
    }

    @CachePut(value = "faqById", key = "#result.id")
    @CacheEvict(value = "faqCache", allEntries = true)
    public Faq saveFaq(Faq faq) {
        return faqRepository.save(faq);
    }

    @Caching(evict = {
            @CacheEvict(value = "faqCache", allEntries = true),
            @CacheEvict(value = "faqById", allEntries = true)})
    public void deleteFaq(Faq faq) {
        faqRepository.delete(faq);
    }

    @Caching(evict = {
            @CacheEvict(value = "faqCache", allEntries = true),
            @CacheEvict(value = "faqById", allEntries = true)
    })
    public void evictFaqCache() {
        // Empty.
    }
}
