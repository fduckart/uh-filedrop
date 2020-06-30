package edu.hawaii.its.filedrop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.hawaii.its.filedrop.repository.AllowlistRepository;
import edu.hawaii.its.filedrop.type.Allowlist;

@Service
public class AllowlistService {

    @Autowired
    private AllowlistRepository allowlistRepository;

    @Autowired
    private LdapService ldapService;

    @Value("${app.job.allowlist.threshold}")
    private int threshold;

    private static final Log logger = LogFactory.getLog(AllowlistService.class);

    public Allowlist addAllowlist(LdapPerson entry, LdapPerson registrant) {
        Allowlist allowlist = new Allowlist();
        allowlist.setEntry(entry.getUid());
        allowlist.setRegistrant(registrant.getUid());
        allowlist.setCheck(0);
        allowlist.setExpired(false);
        allowlist.setCreated(LocalDateTime.now());
        return addAllowlist(allowlist);
    }

    public Allowlist addAllowlist(Allowlist allowlist) {
        return allowlistRepository.save(allowlist);
    }

    public void deleteAllowlist(Allowlist allowlist) {
        allowlistRepository.delete(allowlist);
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
        return allowlist.getCheck();
    }

    public synchronized void checkAllowlists() {
        logger.debug("Starting allowlist check...");
        for (Allowlist allowlist : findAllAllowList()) {
            if (!ldapService.findByUhUuidOrUidOrMail(allowlist.getRegistrant()).isValid()) {
                addCheck(allowlist, 1);
            }
        }
        logger.debug("Finished allowlist check.");
    }

    public Allowlist findAllowList(Integer id) {
        Allowlist allowlist = allowlistRepository.findById(id).orElse(null);
        if (allowlist != null) {
            allowlist.setEntryName(ldapService.findByUid(allowlist.getEntry()).getCn());
            allowlist.setRegistrantName(ldapService.findByUid(allowlist.getRegistrant()).getCn());
        }
        return allowlist;
    }

    public List<Allowlist> findAllAllowList() {
        List<Allowlist> allowlists = allowlistRepository.findAll();
        allowlists.forEach(whitelist -> {
            whitelist.setEntryName(ldapService.findByUhUuidOrUidOrMail(whitelist.getEntry()).getCn());
            whitelist.setRegistrantName(ldapService.findByUhUuidOrUidOrMail(whitelist.getRegistrant()).getCn());
        });
        return allowlists;
    }

    public boolean isAllowlisted(String entry) {
        return allowlistRepository.findByEntry(entry) != null;
    }

    public List<String> getAllAllowlistUids() {
        return findAllAllowList().stream().map(Allowlist::getEntry).collect(Collectors.toList());
    }
}
