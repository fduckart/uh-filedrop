package edu.hawaii.its.filedrop.access;

import org.springframework.stereotype.Service;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    @Override
    public RoleHolder fetchRoles(String uhUuid) {
        return fetchRoles(uhUuid, true);
    }

    @Override
    public RoleHolder fetchRoles(String uhUuid, boolean isAuthenticated) {
        RoleHolder roleHolder = new RoleHolder();

        if (!isAuthenticated) {
            // User not logged in.
            roleHolder.add(Role.ANONYMOUS);
            return roleHolder;
        }

        roleHolder.add(Role.USER);
        roleHolder.add(Role.ADMIN);

        return roleHolder;
    }

}
