package backend.termos.service;

import backend.termos.model.TermoUso;
import backend.termos.repository.TermoUsoRepository;
import backend.usuarios.model.Usuario;
import backend.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        if (termo.getAtualizadoPor() == null || termo.getAtualizadoPor().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O usuário responsável é obrigatório");
        }

        Usuario autor = usuarioRepository.findById(termo.getAtualizadoPor().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado"));

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
