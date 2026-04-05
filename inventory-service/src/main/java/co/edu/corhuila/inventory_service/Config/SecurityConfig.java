package co.edu.corhuila.inventory_service.Config;

import co.edu.corhuila.inventory_service.Service.JwtFilter;
import co.edu.corhuila.inventory_service.Service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/status").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Product batches
                        .requestMatchers(HttpMethod.POST, "/api/products/*/batches")
                        .hasAnyRole("ADMIN", "FARMACEUTICO")

                        // Products: ADMIN can create, update and delete
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // Reports (products): only ADMIN and AUDITOR
                        .requestMatchers(HttpMethod.GET, "/api/products/active-table")
                        .hasAnyRole("ADMIN", "AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/products/active-summary")
                        .hasAnyRole("ADMIN", "AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/products/fefo-snapshot")
                        .hasAnyRole("ADMIN", "FARMACEUTICO")

                        // Products: ADMIN, PHARMACIST and AUDITOR can view
                        .requestMatchers(HttpMethod.GET, "/api/products/**")
                        .hasAnyRole("ADMIN", "FARMACEUTICO", "AUDITOR")

                        // Reports (movements): only ADMIN and AUDITOR
                        .requestMatchers(HttpMethod.GET, "/api/movements")
                        .hasAnyRole("ADMIN", "AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/movements/entrance")
                        .hasAnyRole("ADMIN", "AUDITOR", "FARMACEUTICO")
                        .requestMatchers(HttpMethod.GET, "/api/movements/exit")
                        .hasAnyRole("ADMIN", "AUDITOR", "FARMACEUTICO")
                        .requestMatchers(HttpMethod.GET, "/api/movements/updated")
                        .hasAnyRole("ADMIN", "AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/movements/report/users-activity")
                        .hasAnyRole("ADMIN", "AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/movements/filter-by-user")
                        .hasAnyRole("ADMIN", "AUDITOR")

                        // Legacy aliases for movements (kept restricted to reporting roles)
                        .requestMatchers(HttpMethod.GET, "/api/motions/**")
                        .hasAnyRole("ADMIN", "AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/Motion/**")
                        .hasAnyRole("ADMIN", "AUDITOR")

                        .requestMatchers(HttpMethod.POST, "/api/movements/entries")
                        .hasRole("FARMACEUTICO")
                        .requestMatchers(HttpMethod.POST, "/api/movements/exits")
                        .hasRole("FARMACEUTICO")

                        // Other movement reads (if any future path): ADMIN, AUDITOR and FARMACEUTICO
                        .requestMatchers(HttpMethod.GET, "/api/movements/**")
                        .hasAnyRole("ADMIN", "AUDITOR", "FARMACEUTICO")

                        // Batch-aware movements
                        .requestMatchers(HttpMethod.POST, "/api/movements/**")
                        .hasAnyRole("ADMIN", "FARMACEUTICO")

                        // Reports namespace
                        .requestMatchers(HttpMethod.GET, "/api/reports/**")
                        .hasAnyRole("ADMIN", "AUDITOR")

                        // Everything else requires authentication
                        .anyRequest().authenticated()

                )
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

