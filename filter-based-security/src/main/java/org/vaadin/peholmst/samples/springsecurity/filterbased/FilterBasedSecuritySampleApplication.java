package org.vaadin.peholmst.samples.springsecurity.filterbased;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@SpringBootApplication
public class FilterBasedSecuritySampleApplication {

    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(securedEnabled = true)
    public static class SecurityConfiguration extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            //@formatter:off
            auth
                .inMemoryAuthentication()
                    .withUser("admin").password("p").roles("ADMIN")
                    .and()
                    .withUser("user").password("p").roles("USER");
            //@formatter:on
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            //@formatter:off
            http
                .csrf().disable() // Use Vaadin's CSRF protection
                .authorizeRequests().anyRequest().authenticated() // User must be authenticated to access any part of the application
                .and()
                .formLogin().loginPage("/login").permitAll() // Login page is accessible to anybody
                .and()
                .logout().logoutUrl("/logout").logoutSuccessUrl("/login?logged-out").permitAll() // Logout success page is accessible to anybody
                .and()
                .sessionManagement().sessionFixation().newSession(); // Create completely new session
            //@formatter:on
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers("/css/*"); // Static resources are ignored
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(FilterBasedSecuritySampleApplication.class, args);
    }
}
