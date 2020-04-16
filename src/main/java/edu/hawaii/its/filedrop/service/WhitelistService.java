package edu.hawaii.its.filedrop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.filedrop.repository.WhitelistRepository;
import edu.hawaii.its.filedrop.type.Whitelist;

@Service
public class WhitelistService {

    @Autowired
    private WhitelistRepository whitelistRepository;

    @Autowired
    private LdapService ldapService;

    @Value("${app.job.whitelist.threshold}")
    private int threshold;

    private static final Log logger = LogFactory.getLog(WhitelistService.class);

    public Whitelist addWhitelist(LdapPerson entry, LdapPerson registrant) {
        Whitelist whitelist = new Whitelist();
        whitelist.setEntry(entry.getUid());
        whitelist.setRegistrant(registrant.getUid());
        whitelist.setCheck(0);
        whitelist.setExpired(false);
        whitelist.setCreated(LocalDateTime.now());
        return addWhitelist(whitelist);
    }

    public Whitelist addWhitelist(Whitelist whitelist) {
        return whitelistRepository.save(whitelist);
    }

    public void deleteWhitelist(Whitelist whitelist) {
        whitelistRepository.delete(whitelist);
    }

    public long recordCount() {
        return whitelistRepository.count();
    }

    public int addCheck(Whitelist whitelist, int amount) {
        whitelist.setCheck(whitelist.getCheck() + amount);
        if (whitelist.getCheck() >= threshold) {
            whitelist.setExpired(true);
        }
        addWhitelist(whitelist);
        return whitelist.getCheck();
    }

    public synchronized void checkWhitelists() {
        logger.debug("Starting whitelist check...");
        for (Whitelist whitelist : findAllWhiteList()) {
            if (!ldapService.findByUhUuidOrUidOrMail(whitelist.getRegistrant()).isValid()) {
                addCheck(whitelist, 1);
            }
        }
        logger.debug("Finished whitelist check.");
    }

    public Whitelist findWhiteList(Integer id) {
        Whitelist whitelist = whitelistRepository.findById(id).orElse(null);
        if (whitelist != null) {
            whitelist.setEntryName(ldapService.findByUid(whitelist.getEntry()).getCn());
            whitelist.setRegistrantName(ldapService.findByUid(whitelist.getRegistrant()).getCn());
        }
        return whitelist;
    }

    public List<Whitelist> findAllWhiteList() {
        List<Whitelist> whitelists = whitelistRepository.findAll();
        whitelists.forEach(whitelist -> {
            whitelist.setEntryName(ldapService.findByUhUuidOrUidOrMail(whitelist.getEntry()).getCn());
            whitelist.setRegistrantName(ldapService.findByUhUuidOrUidOrMail(whitelist.getRegistrant()).getCn());
        });
        return whitelists;
    }

    public boolean isWhitelisted(String entry) {
        return whitelistRepository.findByEntry(entry) != null;
    }

    public List<String> getAllWhitelistUids() {
        return findAllWhiteList().stream().map(Whitelist::getEntry).collect(Collectors.toList());
    }
}
