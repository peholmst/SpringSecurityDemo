package org.vaadin.peholmst.samples.springsecurity.filterbased;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.vaadin.spring.access.ViewAccessControl;
import com.vaadin.ui.UI;

/**
 * This demonstrates how you can control access to views.
 */
@Component
public class SampleViewAccessControl implements ViewAccessControl {

    @Override
    public boolean isAccessGranted(UI ui, String beanName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (beanName.equals("adminView")) {
                return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else {
                return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"));
            }
        }
        return false;
    }
}
