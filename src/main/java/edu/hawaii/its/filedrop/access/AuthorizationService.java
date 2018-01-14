package edu.hawaii.its.filedrop.access;

public interface AuthorizationService {

    public SecurityRoleHolder fetchRoles(String uhUuid);

    public SecurityRoleHolder fetchRoles(String uhUuid, boolean isAuthenticated);

}
