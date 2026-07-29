package com.parismolapo.bookingreminder.security;

import com.parismolapo.bookingreminder.config.RoleInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OwnershipGuard {

    private static final String ADMIN_AUTHORITY = "ROLE_" + RoleInitializer.ROLE_ADMIN;

    /**
     * Throws if the caller is not an admin and does not own the given business.
     */
    public void checkBusinessAccess(Long businessId) {

        AuthUser caller = currentUser();

        if (isAdmin()) {
            return;
        }

        Long ownedBusinessId = caller.getBusinessId();

        if (ownedBusinessId == null || !ownedBusinessId.equals(businessId)) {
            log.warn("User {} attempted to access business {}",
                    caller.getUsername(), businessId);
            throw new AccessDeniedException(
                    "You do not have access to this business");
        }
    }

    public AuthUser currentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new AccessDeniedException("You must be logged in");
        }

        return authUser;
    }

    public boolean isAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ADMIN_AUTHORITY::equals);
    }

    /**
     * The business the caller owns. Null for admins.
     */
    public Long currentBusinessId() {
        return currentUser().getBusinessId();
    }
}