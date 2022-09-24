package edu.hawaii.its.filedrop.service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RestrictionService {

    @Value("${app.restrictions.sender.student}")
    private List<String> studentRestrictions;

    @Value("${app.restrictions.sender.faculty}")
    private List<String> facultyRestrictions;

    @Value("${app.restrictions.sender.staff}")
    private List<String> staffRestrictions;

    @Value("${app.restrictions.sender.affiliate}")
    private List<String> affiliateRestrictions;

    @Value("${app.restrictions.sender.other}")
    private List<String> otherRestrictions;

    private Map<String, List<String>> allRestrictions;

    @PostConstruct
    public void init() {
        allRestrictions = new HashMap<>();
        allRestrictions.put("affiliate", affiliateRestrictions);
        allRestrictions.put("faculty", facultyRestrictions);
        allRestrictions.put("other", otherRestrictions);
        allRestrictions.put("staff", staffRestrictions);
        allRestrictions.put("student", studentRestrictions);
    }

}
