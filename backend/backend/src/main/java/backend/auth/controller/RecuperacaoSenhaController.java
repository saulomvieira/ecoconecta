package backend.auth.controller;
import backend.auth.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/auth")
public class RecuperacaoSenhaController {
    private final RecuperacaoSenhaService service;
    public RecuperacaoSenhaController(RecuperacaoSenhaService service) {this.service=service;}
    @PostMapping("/esqueci-senha")
    public Map<String,String> solicitar(@Valid @RequestBody SolicitarRequest req) {
        service.solicitar(req.email());
        return Map.of("mensagem","Se a conta existir, enviaremos as instruções de recuperação.");
    }
    @PostMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinir(@Valid @RequestBody RedefinirRequest req) {
        service.redefinir(req.token(),req.novaSenha());
    }
    public record SolicitarRequest(@NotBlank @Email String email) {}
    public record RedefinirRequest(@NotBlank String token,@NotBlank String novaSenha) {}
}
