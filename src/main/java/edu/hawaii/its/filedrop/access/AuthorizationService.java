package edu.hawaii.its.filedrop.access;

import static edu.hawaii.its.filedrop.type.Role.SecurityRole.ADMINISTRATOR;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.ANONYMOUS;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.SUPERUSER;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.UH;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.hawaii.its.filedrop.service.PersonService;

@Service
public class AuthorizationService {

    @Autowired
    private PersonService personService;

    public SecurityRoleHolder fetchRoles(String uhUuid) {
        SecurityRoleHolder roleHolder = new SecurityRoleHolder();

        if (uhUuid == null) {
            roleHolder.add(ANONYMOUS);
            return roleHolder;
        }

        roleHolder.add(UH);

        if (personService.isAdministrator(uhUuid)) {
            roleHolder.add(ADMINISTRATOR);
        }

        if (personService.isSuperuser(uhUuid)) {
            roleHolder.add(SUPERUSER);
        }

        return roleHolder;
    }

}
