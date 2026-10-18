
package backend.assinaturas.service;

import backend.assinaturas.model.Assinatura;
import backend.assinaturas.model.StatusAssinatura;
import backend.assinaturas.repository.AssinaturaRepository;
import backend.planos.model.Plano;
import backend.planos.repository.PlanoRepository;
import backend.usuarios.model.Usuario;
import backend.usuarios.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssinaturaService {

    private final AssinaturaRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final PlanoRepository planoRepository;

    public AssinaturaService(
            AssinaturaRepository repository,
            UsuarioRepository usuarioRepository,
            PlanoRepository planoRepository) {

        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.planoRepository = planoRepository;
    }

    // Criar assinatura
    public Assinatura cadastrar(Assinatura assinatura) {

        if (assinatura == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Os dados da assinatura são obrigatórios"
            );
        }

        Usuario usuario = buscarUsuario(assinatura);
        exigirProprietarioOuAdmin(usuario.getId());

        Plano plano = buscarPlano(assinatura);

        if (!Boolean.TRUE.equals(plano.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Plano inativo");
        }

        repository.findByUsuarioIdAndStatus(
                usuario.getId(),
                StatusAssinatura.ATIVA
        ).ifPresent(this::verificarExpiracao);

        if (repository.existsByUsuarioIdAndStatus(
                usuario.getId(),
                StatusAssinatura.ATIVA)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O usuário já possui uma assinatura ativa"
            );
        }

        LocalDate hoje = LocalDate.now();

        assinatura.setId(null);
        assinatura.setUsuario(usuario);
        assinatura.setPlano(plano);
        assinatura.setStatus(StatusAssinatura.ATIVA);
        assinatura.setDataInicio(hoje);
        assinatura.setDataFim(hoje.plusDays(30));

        return repository.save(assinatura);
    }

    // Listar todas: apenas ADMIN
    public List<Assinatura> listar(StatusAssinatura status) {
        exigirAdmin();

        return status == null
                ? repository.findAll()
                : repository.findByStatus(status);
    }

    // Consultar: proprietário ou ADMIN
    public Assinatura buscarPorId(Long id) {

        Assinatura assinatura = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Assinatura não encontrada"
                ));

        exigirProprietarioOuAdmin(assinatura.getUsuario().getId());

        verificarExpiracao(assinatura);

        return assinatura;
    }

    // Atualizar plano/status: apenas ADMIN
    public Assinatura atualizar(Long id, Assinatura dados) {

        exigirAdmin();

        Assinatura assinatura = buscarPorId(id);

        if (dados.getPlano() != null) {
            assinatura.setPlano(buscarPlano(dados));
        }

        if (dados.getStatus() != null
                && dados.getStatus() != assinatura.getStatus()) {

            if (dados.getStatus() == StatusAssinatura.ATIVA
                    && repository.existsByUsuarioIdAndStatusAndIdNot(
                    assinatura.getUsuario().getId(),
                    StatusAssinatura.ATIVA,
                    assinatura.getId())) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "O usuário já possui outra assinatura ativa"
                );
            }

            assinatura.setStatus(dados.getStatus());
        }

        return repository.save(assinatura);
    }

    // Cancelar: proprietário ou ADMIN
    public Assinatura cancelar(Long id) {

        Assinatura assinatura = buscarPorId(id);

        if (assinatura.getStatus() == StatusAssinatura.CANCELADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta assinatura já está cancelada"
            );
        }

        assinatura.setStatus(StatusAssinatura.CANCELADA);

        return repository.save(assinatura);
    }

    // Verificar se uma assinatura venceu
    private boolean verificarExpiracao(Assinatura assinatura) {

        if (assinatura.getStatus() != StatusAssinatura.ATIVA) {
            return false;
        }

        LocalDate dataFim = assinatura.getDataFim();

        if (dataFim == null) {
            return false;
        }

        if (!LocalDate.now().isBefore(dataFim)) {
            assinatura.setStatus(StatusAssinatura.EXPIRADA);
            repository.save(assinatura);
            return true;
        }

        return false;
    }

    // Renovar assinatura por mais 30 dias — somente ADMIN
    public Assinatura renovar(Long id) {

        exigirAdmin();

        Assinatura assinatura = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Assinatura não encontrada"
                ));

        if (assinatura.getStatus() == StatusAssinatura.CANCELADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível renovar uma assinatura cancelada"
            );
        }

        repository.findByUsuarioIdAndStatus(
                assinatura.getUsuario().getId(),
                StatusAssinatura.ATIVA
        ).ifPresent(this::verificarExpiracao);

        if (repository.existsByUsuarioIdAndStatusAndIdNot(
                assinatura.getUsuario().getId(),
                StatusAssinatura.ATIVA,
                assinatura.getId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O usuário já possui outra assinatura ativa"
            );
        }

        LocalDate hoje = LocalDate.now();

        // Se ainda estiver válida, acrescenta 30 dias ao vencimento atual.
        // Caso contrário, inicia um novo período a partir de hoje.
        LocalDate base = assinatura.getDataFim() != null
                && assinatura.getDataFim().isAfter(hoje)
                ? assinatura.getDataFim()
                : hoje;

        assinatura.setDataFim(base.plusDays(30));
        assinatura.setStatus(StatusAssinatura.ATIVA);

        return repository.save(assinatura);
    }

    private Usuario buscarUsuario(Assinatura assinatura) {

        if (assinatura.getUsuario() == null
                || assinatura.getUsuario().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O usuário é obrigatório"
            );
        }

        return usuarioRepository.findById(
                assinatura.getUsuario().getId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuário não encontrado"
        ));
    }

    private Plano buscarPlano(Assinatura assinatura) {

        if (assinatura.getPlano() == null
                || assinatura.getPlano().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O plano é obrigatório"
            );
        }

        Plano plano = planoRepository.findById(
                assinatura.getPlano().getId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Plano não encontrado"
        ));

        if (!Boolean.TRUE.equals(plano.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O plano selecionado está inativo"
            );
        }

        return plano;
    }

    private Jwt obterJwt() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Autenticação necessária"
            );
        }

        return jwt;
    }

    private boolean ehAdmin() {
        return "ADMIN".equals(obterJwt().getClaimAsString("perfil"));
    }

    private void exigirProprietarioOuAdmin(Long usuarioId) {

        Jwt jwt = obterJwt();

        boolean proprietario =
                jwt.getSubject().equals(usuarioId.toString());

        if (!ehAdmin() && !proprietario) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar esta assinatura"
            );
        }
    }

    private void exigirAdmin() {

        if (!ehAdmin()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Operação permitida apenas para administradores"
            );
        }
    }
}
