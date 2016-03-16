package org.vaadin.peholmst.samples.springsecurity.filterbased;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class BackendService {

    @Secured("ROLE_ADMIN")
    public String adminMethod() {
        return "Hello from an admin method";
    }

    @Secured({ "ROLE_ADMIN", "ROLE_USER" })
    public String userMethod() {
        return "Hello from a user method";
    }
}
