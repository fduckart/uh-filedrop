package edu.hawaii.its.filedrop.service;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.hawaii.its.filedrop.repository.AllowlistRepository;
import edu.hawaii.its.filedrop.type.Allowlist;

@Transactional
@Service
public class AllowlistService {

    @Autowired
    private AllowlistRepository allowlistRepository;

    @Autowired
    private LdapService ldapService;

    @Value("${app.job.allowlist.threshold}")
    private int threshold;

    private static final Log logger = LogFactory.getLog(AllowlistService.class);

    @Cacheable(value = "allowlistCache")
    public List<Allowlist> findAll() {
        return allowlistRepository.findAll();
    }

    @Cacheable(value = "allowlistById", key = "#id")
    public Allowlist findById(Integer id) {
        return allowlistRepository.findById(id).orElse(null);
    }

    @CachePut(value = "allowlistById", key = "#result.id")
    @CacheEvict(value = "allowlistCache", allEntries = true)
    public Allowlist addAllowlist(Allowlist allowlist) {
        return allowlistRepository.save(allowlist);
    }

    @Caching(evict = {
            @CacheEvict(value = "allowlistCache", allEntries = true),
            @CacheEvict(value = "allowlistById", allEntries = true)})
    public void evictAllowlistCache() {
        // Empty.
    }

    @Caching(evict = {
            @CacheEvict(value = "allowlistCache", allEntries = true),
            @CacheEvict(value = "allowlistById", allEntries = true)})
    public void deleteAllowlist(Allowlist allowlist) {
        allowlistRepository.delete(allowlist);
    }

    public Allowlist addAllowlist(LdapPerson entry, LdapPerson registrant) {
        Allowlist allowlist = new Allowlist();
        allowlist.setEntry(entry.getUid());
        allowlist.setRegistrant(registrant.getUid());
        allowlist.setCheck(0);
        allowlist.setExpired(false);
        allowlist.setCreated(LocalDateTime.now());
        return addAllowlist(allowlist);
    }

    public long recordCount() {
        return allowlistRepository.count();
    }

    public int addCheck(Allowlist allowlist, int amount) {
        allowlist.setCheck(allowlist.getCheck() + amount);
        if (allowlist.getCheck() >= threshold) {
            logger.debug("addCheck; Expired: " + allowlist);
            allowlist.setExpired(true);
        }
        allowlist = addAllowlist(allowlist);
        logger.debug("addCheck; Add check: " + allowlist);
        return allowlist.getCheck();
    }

    public synchronized void checkAllowlists() {
        logger.debug("Starting allowlist check...");
        for (Allowlist allowlist : findAll()) {
            if (!ldapService.findByUhUuidOrUidOrMail(allowlist.getRegistrant()).isValid()) {
                addCheck(allowlist, 1);
            } else if (allowlist.isExpired()) {
                allowlist.setExpired(false);
                allowlist.setCheck(0);
                addAllowlist(allowlist);
                logger.debug("checkAllowlist; Unexpired: " + allowlist);
            }
        }
        evictAllowlistCache();
        logger.debug("Finished allowlist check.");
    }

    public boolean isAllowlisted(String entry) {
        return allowlistRepository.findByEntry(entry) != null;
    }
}
