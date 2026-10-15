package backend.planos.service;

import backend.planos.model.Assinatura;
import backend.planos.model.Plano;
import backend.planos.model.StatusAssinatura;
import backend.planos.repository.AssinaturaRepository;
import backend.planos.repository.PlanoRepository;
import backend.usuarios.model.Usuario;
import backend.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssinaturaService {

    private final AssinaturaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final PlanoRepository planoRepository;

    public AssinaturaService(AssinaturaRepository repository,
                             UsuarioRepository usuarioRepository,
                             PlanoRepository planoRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.planoRepository = planoRepository;
    }

    public Assinatura cadastrar(Assinatura assinatura) {
        Usuario usuario = buscarUsuario(assinatura);
        Plano plano = buscarPlano(assinatura);

        if (repository.existsByUsuarioIdAndStatus(usuario.getId(), StatusAssinatura.ATIVA)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "O usuário já possui uma assinatura ativa");
        }

        assinatura.setId(null);
        assinatura.setUsuario(usuario);
        assinatura.setPlano(plano);
        assinatura.setStatus(StatusAssinatura.ATIVA);
        assinatura.setDataInicio(LocalDate.now());

        return repository.save(assinatura);
    }

    public List<Assinatura> listar(StatusAssinatura status) {
        return status == null ? repository.findAll() : repository.findByStatus(status);
    }

    public Assinatura buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assinatura não encontrada"));
    }

    // Permite trocar o plano e/ou o status (ex.: marcar como inadimplente)
    public Assinatura atualizar(Long id, Assinatura dados) {
        Assinatura assinatura = buscarPorId(id);

        if (dados.getPlano() != null) {
            assinatura.setPlano(buscarPlano(dados));
        }

        if (dados.getStatus() != null && dados.getStatus() != assinatura.getStatus()) {
            if (dados.getStatus() == StatusAssinatura.ATIVA
                    && repository.existsByUsuarioIdAndStatus(
                    assinatura.getUsuario().getId(), StatusAssinatura.ATIVA)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "O usuário já possui uma assinatura ativa");
            }
            assinatura.setStatus(dados.getStatus());
        }

        return repository.save(assinatura);
    }

    public Assinatura cancelar(Long id) {
        Assinatura assinatura = buscarPorId(id);
        assinatura.setStatus(StatusAssinatura.CANCELADA);
        return repository.save(assinatura);
    }

    private Usuario buscarUsuario(Assinatura assinatura) {
        if (assinatura.getUsuario() == null || assinatura.getUsuario().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O usuário é obrigatório");
        }
        return usuarioRepository.findById(assinatura.getUsuario().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    private Plano buscarPlano(Assinatura assinatura) {
        if (assinatura.getPlano() == null || assinatura.getPlano().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O plano é obrigatório");
        }
        Plano plano = planoRepository.findById(assinatura.getPlano().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Plano não encontrado"));

        if (!Boolean.TRUE.equals(plano.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O plano selecionado está inativo");
        }
        return plano;
    }
}
