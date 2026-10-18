package backend.auth.controller;

import backend.auth.service.JwtService;
import backend.auth.service.AuthService;

import backend.usuarios.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Optional<Usuario> usuario = authService.autenticar(
                request.email(),
                request.senha()
        );

        if (usuario.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "E-mail ou senha inválidos"));
        }

        String token = jwtService.gerarToken(usuario.get());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "type", "Bearer",
                "expiresIn", 3600
        ));
    }

    public record LoginRequest(String email, String senha) {
    }
}