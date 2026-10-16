
package backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // Rotas públicas
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()

                        // Listagens administrativas
                        .requestMatchers(HttpMethod.GET, "/usuarios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reservas").hasRole("ADMIN")

                        // Exclusão de usuários
                        .requestMatchers(HttpMethod.DELETE, "/usuarios/*").hasRole("ADMIN")

                        // Consulta de planos: usuários autenticados
                        .requestMatchers(HttpMethod.GET, "/planos", "/planos/*")
                        .authenticated()

                        // Alterações de planos: apenas administradores
                        .requestMatchers(HttpMethod.POST, "/planos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/planos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/planos/*").hasRole("ADMIN")


                        // Publicação de termos: apenas administradores
                        .requestMatchers(HttpMethod.POST, "/termos").hasRole("ADMIN")

                        // Demais operações exigem autenticação
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                ));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String perfil = jwt.getClaimAsString("perfil");

            if (!"USUARIO".equals(perfil) && !"ADMIN".equals(perfil)) {
                return List.of();
            }

            return List.of(
                    new SimpleGrantedAuthority("ROLE_" + perfil)
            );
        });

        return converter;
    }
}
