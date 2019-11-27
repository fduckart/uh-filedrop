package edu.hawaii.its.filedrop.access;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {

    public User getCurrentUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return (User) principal;
            }
        }
        return new AnonymousUser();
    }

    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    public String getCurrentUhuuid() {
        return getCurrentUser().getUhuuid();
    }

    public String toString() {
        return "UserContextServiceImpl [context=" + SecurityContextHolder.getContext() + "]";
    }
}
