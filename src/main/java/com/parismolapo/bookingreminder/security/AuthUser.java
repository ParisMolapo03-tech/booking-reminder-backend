package com.parismolapo.bookingreminder.security;

import com.parismolapo.bookingreminder.entity.Role;
import com.parismolapo.bookingreminder.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser implements UserDetails {

    private final User user;

    public AuthUser(User user) {
        this.user = user;
    }

    public Long getUserId() {
        return user.getId();
    }

    public Long getBusinessId() {
        return user.getBusiness() == null ? null : user.getBusiness().getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(Role::getName)
                .map(name -> new SimpleGrantedAuthority("ROLE_" + name))
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}