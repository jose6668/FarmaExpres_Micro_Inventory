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


                        // Públicas
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/status").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Productos: ADMIN puede crear, actualizar y eliminar
                         .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                         .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                         .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // Productos: ADMIN, FARMACEUTICO y AUDITOR pueden ver
                        .requestMatchers(HttpMethod.GET, "/api/products/**")
                        .hasAnyRole("ADMIN", "FARMACEUTICO", "AUDITOR")

                        // Movimientos: ADMIN y AUDITOR pueden ver
                        .requestMatchers(HttpMethod.GET, "/api/motions/**")
                        .hasAnyRole("ADMIN", "AUDITOR")

                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()


                )
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
