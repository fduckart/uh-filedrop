package edu.hawaii.its.filedrop.access;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import edu.hawaii.its.filedrop.util.Strings;

@Service
public final class UserBuilder {

    private static final Log logger = LogFactory.getLog(UserBuilder.class);

    @Autowired
    private AuthorizationService authorizationService;

    public final User make(Map<String, ?> map) {
        return make(new UhCasAttributes(map));
    }

    public final User make(UhAttributes attributes) {

        String uid = attributes.getUid();
        if (Strings.isBlank(uid)) {
            // Should not happen, but just in case.
            throw new UsernameNotFoundException("uid is blank");
        }

        logger.debug("Lookup roles for user via service.");
        String uhuuid = attributes.getUhUuid();
        SecurityRoleHolder roleHolder = authorizationService.fetchRoles(uhuuid);

        logger.info("Adding roles. uid: " + uid + "; roles: " + roleHolder.getAuthorites());
        User user = new User(uid, uhuuid, roleHolder.getAuthorites());
        logger.debug("Done adding roles; uid: " + uid);

        // Put all the attributes into the user
        // object just for the demonstration.
        // Above is what might commonly occur.
        user.setAttributes(attributes);

        return user;
    }

}
