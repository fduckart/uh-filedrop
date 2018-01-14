package edu.hawaii.its.filedrop.access;

import static edu.hawaii.its.filedrop.type.Role.SecurityRole.ADMINISTRATOR;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.ANONYMOUS;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.SUPERUSER;
import static edu.hawaii.its.filedrop.type.Role.SecurityRole.UH;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.hawaii.its.filedrop.service.ApplicationService;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    @Autowired
    private ApplicationService applicationService;

    @Override
    public SecurityRoleHolder fetchRoles(String uhUuid) {
        return fetchRoles(uhUuid, true);
    }

    @Override
    public SecurityRoleHolder fetchRoles(String uhUuid, boolean isAuthenticated) {
        SecurityRoleHolder roleHolder = new SecurityRoleHolder();

        if (!isAuthenticated) {
            // User not logged in.
            roleHolder.add(ANONYMOUS);
            return roleHolder;
        }

        roleHolder.add(UH);

        if (applicationService.isAdministrator(uhUuid)) {
            roleHolder.add(ADMINISTRATOR);
        }

        if (applicationService.isSuperuser(uhUuid)) {
            roleHolder.add(SUPERUSER);
        }

        return roleHolder;
    }

}
