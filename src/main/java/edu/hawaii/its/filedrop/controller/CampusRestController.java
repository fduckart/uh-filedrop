package edu.hawaii.its.filedrop.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.hawaii.its.filedrop.service.CampusService;
import edu.hawaii.its.filedrop.type.Campus;

@RestController
public class CampusRestController {

    private static final Log logger = LogFactory.getLog(CampusRestController.class);

    @Autowired
    private CampusService campusService;

    @GetMapping(value = "/api/campuses")
    public ResponseEntity<List<Campus>> campuses() {
        logger.info("Entered REST campuses...");
        List<Campus> data = campusService.findAll();
        return ResponseEntity
                .ok()
                .body(data);
    }
}
