package backend.termos.controller;
import backend.termos.model.AceiteTermo;
import backend.termos.repository.AceiteTermoRepository;
import backend.termos.service.TermoUsoService;
import backend.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Map;
@RestController
@RequestMapping("/termos")
public class AceiteTermoController {
    private final AceiteTermoRepository aceites;
    private final UsuarioRepository usuarios;
    private final TermoUsoService termos;
    public AceiteTermoController(AceiteTermoRepository aceites,
            UsuarioRepository usuarios,TermoUsoService termos) {
        this.aceites=aceites;this.usuarios=usuarios;this.termos=termos;
    }
    @PostMapping("/aceitar")
    @Transactional
    public Map<String,Object> aceitar(@AuthenticationPrincipal Jwt jwt) {
        if (jwt==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        Long id=Long.valueOf(jwt.getSubject());
        var usuario=usuarios.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var termo=termos.buscarAtual();
        if (!aceites.existsByUsuarioIdAndTermoId(id,termo.getId())) {
            var aceite=new AceiteTermo();
            aceite.setUsuario(usuario);aceite.setTermo(termo);
            aceite.setDataAceite(LocalDateTime.now());
            aceites.save(aceite);
        }
        return Map.of("aceito",true,"versao",termo.getVersao());
    }
    @GetMapping("/aceite-atual")
    public Map<String,Object> consultar(@AuthenticationPrincipal Jwt jwt) {
        if (jwt==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        var termo=termos.buscarAtual();
        return Map.of("aceito",aceites.existsByUsuarioIdAndTermoId(
                Long.valueOf(jwt.getSubject()),termo.getId()),"versao",termo.getVersao());
    }
}
