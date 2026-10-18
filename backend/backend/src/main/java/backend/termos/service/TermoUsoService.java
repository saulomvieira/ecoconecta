package backend.termos.service;

import backend.termos.model.TermoUso;
import backend.termos.repository.TermoUsoRepository;
import backend.usuarios.model.Usuario;
import backend.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TermoUsoService {

    private static final String VERSAO_INICIAL = "1.0";

    private final TermoUsoRepository repository;
    private final UsuarioRepository usuarioRepository;

    public TermoUsoService(TermoUsoRepository repository,
                           UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    // Cada salvamento cria uma nova versão; as anteriores ficam no histórico
    @Transactional
    public TermoUso publicar(TermoUso termo) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Autenticação necessária"
            );
        }

        if (!"ADMIN".equals(jwt.getClaimAsString("perfil"))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas administradores podem publicar termos de uso"
            );
        }

        Long autorId = Long.valueOf(jwt.getSubject());

        Usuario autor = usuarioRepository.findById(autorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Administrador não encontrado"
                ));

        String versao = VERSAO_INICIAL;
        TermoUso atual = repository.findFirstByAtivoTrueOrderByDataAtualizacaoDesc().orElse(null);

        if (atual != null) {
            if (atual.getConteudo().equals(termo.getConteudo())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "O conteúdo não foi alterado");
            }
            versao = proximaVersao(atual.getVersao());
            atual.setAtivo(false);
            repository.saveAndFlush(atual);
        }

        termo.setId(null);
        termo.setVersao(versao);
        termo.setAtivo(true);
        termo.setDataAtualizacao(LocalDateTime.now());
        termo.setAtualizadoPor(autor);

        return repository.save(termo);
    }

    public TermoUso buscarAtual() {
        return repository.findFirstByAtivoTrueOrderByDataAtualizacaoDesc()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Nenhum termo de uso publicado"));
    }

    public List<TermoUso> listarHistorico() {
        return repository.findAllByOrderByDataAtualizacaoDesc();
    }

    public TermoUso buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Termo de uso não encontrado"));
    }

    // "1.2" -> "1.3"
    private String proximaVersao(String versao) {
        String[] partes = versao.split("\\.");
        return partes[0] + "." + (Integer.parseInt(partes[1]) + 1);
    }
}
