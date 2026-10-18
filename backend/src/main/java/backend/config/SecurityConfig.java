
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
import backend.usuarios.repository.UsuarioRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthenticationConverter converter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        // Rotas públicas
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/esqueci-senha", "/auth/redefinir-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()

                        // Todos os endpoints administrativos exigem perfil ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Listagens administrativas
                        .requestMatchers(HttpMethod.GET, "/usuarios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reservas").hasRole("ADMIN")

                        // Exclusão de usuários
                        .requestMatchers(HttpMethod.DELETE, "/usuarios/*").hasRole("ADMIN")

                        // Consulta de planos: usuários autenticados
                        .requestMatchers(HttpMethod.GET, "/planos", "/planos/*")
                        .hasAnyRole("USUARIO", "ADMIN")

                        // Alterações de planos: apenas administradores
                        .requestMatchers(HttpMethod.POST, "/planos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/planos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/planos/*").hasRole("ADMIN")


                        // Publicação de termos: apenas administradores
                        .requestMatchers(HttpMethod.POST, "/termos").hasRole("ADMIN")

                        // Demais operações exigem autenticação
                        .anyRequest().hasAnyRole("USUARIO", "ADMIN")
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(converter)
                ));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(UsuarioRepository usuarios) {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String perfil = jwt.getClaimAsString("perfil");

            if (!"USUARIO".equals(perfil) && !"ADMIN".equals(perfil)) {
                return List.of();
            }

            try {
                var usuario = usuarios.findById(Long.valueOf(jwt.getSubject())).orElse(null);
                if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo())
                        || !usuario.getPerfil().name().equals(perfil)) {
                    return List.of();
                }
            } catch (NumberFormatException ex) {
                return List.of();
            }
            return List.of(new SimpleGrantedAuthority("ROLE_" + perfil));
        });

        return converter;
    }
}
