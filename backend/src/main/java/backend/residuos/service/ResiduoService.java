
package backend.residuos.service;

import backend.residuos.model.Residuo;
import backend.residuos.repository.ResiduoRepository;
import backend.usuarios.model.Usuario;
import backend.usuarios.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import backend.residuos.dto.ResiduoDisponibilidadeResponse;
import backend.reservas.model.Reserva.StatusReserva;
import backend.reservas.repository.ReservaRepository;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.stream.Collectors;
import backend.reservas.model.Reserva;

import java.util.List;

@Service
public class ResiduoService {

    private final ReservaRepository reservaRepository;
    private final ResiduoRepository repository;
    private final UsuarioRepository usuarioRepository;


    public ResiduoService(
            ResiduoRepository repository,
            UsuarioRepository usuarioRepository,
            ReservaRepository reservaRepository) {

        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
    }

    // Cadastrar resíduo
    public Residuo cadastrar(Residuo residuo) {

        if (residuo.getUsuario() == null ||
                residuo.getUsuario().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O usuário responsável é obrigatório"
            );
        }

        Long usuarioId = residuo.getUsuario().getId();

        // Impede cadastrar resíduos em nome de outra pessoa
        exigirProprietarioOuAdmin(usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado"
                ));

        // Impede informar um ID para sobrescrever outro resíduo
        residuo.setId(null);
        residuo.setUsuario(usuario);

        return repository.save(residuo);
    }

    // Listar resíduos disponíveis para consulta
    public List<Residuo> listar() {
        return repository.findAll();
    }

    // Consultar resíduo
    public Residuo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resíduo não encontrado"
                ));
    }


    // Atualizar resíduo
    public Residuo atualizar(Long id, Residuo dados) {

        Residuo residuo = buscarPorId(id);

        // Somente o proprietário ou ADMIN pode atualizar
        exigirProprietarioOuAdmin(residuo.getUsuario().getId());

        // Impede alterar resíduos reservados ou coletados
        boolean indisponivel = reservaRepository.existsByResiduoIdAndStatusIn(
                id,
                List.of(
                        StatusReserva.RESERVADA,
                        StatusReserva.COLETADA
                )
        );

        if (indisponivel) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível alterar um resíduo reservado ou coletado"
            );
        }

        residuo.setNome(dados.getNome());
        residuo.setDescricao(dados.getDescricao());
        residuo.setCategoria(dados.getCategoria());
        residuo.setQuantidade(dados.getQuantidade());
        residuo.setUnidadeMedida(dados.getUnidadeMedida());
        residuo.setRegiao(dados.getRegiao());
        residuo.setEnderecoColeta(dados.getEnderecoColeta());
        residuo.setImagemUrl(dados.getImagemUrl());

        // O proprietário não pode ser alterado
        return repository.save(residuo);
    }


    // Excluir resíduo
    public void excluir(Long id) {

        Residuo residuo = buscarPorId(id);

        // Somente o proprietário ou ADMIN pode excluir
        exigirProprietarioOuAdmin(residuo.getUsuario().getId());

        // Impede excluir resíduos que possuem histórico de reservas
        if (reservaRepository.existsByResiduoId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível excluir um resíduo que possui histórico de reservas"
            );
        }

        repository.delete(residuo);
    }

    // Listar resíduos publicados por um usuário
    public List<Residuo> listarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    // Listar resíduos de uma região
    public List<Residuo> listarPorRegiao(String regiao) {
        if (regiao == null || regiao.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A região é obrigatória"
            );
        }

        return repository.findByRegiaoIgnoreCase(regiao.trim());
    }

    // Listar resíduos mais recentes
    public List<Residuo> listarRecentes() {
        return repository.findAllByOrderByIdDesc();
    }

    // Listar feed de resíduos com disponibilidade e prioridade por região
    public List<ResiduoDisponibilidadeResponse> listarFeed(
            String regiao,
            boolean priorizarRegiao) {

        List<Residuo> residuos;

        if (regiao != null && !regiao.isBlank()) {

            String regiaoNormalizada = regiao.trim();

            if (priorizarRegiao) {
                // Exibe todas as regiões, priorizando a selecionada
                residuos = repository.findAllPriorizandoRegiao(
                        regiaoNormalizada
                );
            } else {
                // Exibe somente os resíduos da região selecionada
                residuos = repository.findByRegiaoIgnoreCase(
                        regiaoNormalizada
                );
            }

        } else {
            // Sem região, exibe os resíduos mais recentes
            residuos = repository.findAllByOrderByIdDesc();
        }

        if (residuos.isEmpty()) {
            return List.of();
        }

        // Obtém os IDs dos resíduos encontrados
        Set<Long> ids = residuos.stream()
                .map(Residuo::getId)
                .collect(Collectors.toSet());

        // Busca reservas ativas ou concluídas
        List<Reserva> reservas = reservaRepository
                .findByResiduoIdInAndStatusIn(
                        ids,
                        List.of(
                                StatusReserva.RESERVADA,
                                StatusReserva.COLETADA
                        )
                );

        // Relaciona cada resíduo ao seu status
        Map<Long, String> statusPorResiduo = new HashMap<>();

        for (Reserva reserva : reservas) {

            Long residuoId = reserva.getResiduo().getId();

            if (reserva.getStatus() == StatusReserva.COLETADA) {

                statusPorResiduo.put(residuoId, "COLETADO");

            } else {

                statusPorResiduo.putIfAbsent(
                        residuoId,
                        "RESERVADO"
                );
            }
        }

        // Retorna os resíduos com suas disponibilidades
        return residuos.stream()
                .map(residuo -> new ResiduoDisponibilidadeResponse(
                        residuo,
                        statusPorResiduo.getOrDefault(
                                residuo.getId(),
                                "DISPONIVEL"
                        )
                ))
                .toList();
    }

    // Listar feed de resíduos com disponibilidade
    public List<ResiduoDisponibilidadeResponse> listarFeed(String regiao) {

        List<Residuo> residuos;

        if (regiao != null && !regiao.isBlank()) {
            residuos = repository.findByRegiaoIgnoreCase(regiao.trim());
        } else {
            residuos = repository.findAllByOrderByIdDesc();
        }

        if (residuos.isEmpty()) {
            return List.of();
        }

        Set<Long> ids = residuos.stream()
                .map(Residuo::getId)
                .collect(Collectors.toSet());

        List<Reserva> reservas = reservaRepository
                .findByResiduoIdInAndStatusIn(
                        ids,
                        List.of(
                                StatusReserva.RESERVADA,
                                StatusReserva.COLETADA
                        )
                );

        Map<Long, String> statusPorResiduo = new HashMap<>();

        for (Reserva reserva : reservas) {
            Long residuoId = reserva.getResiduo().getId();

            if (reserva.getStatus() == StatusReserva.COLETADA) {
                statusPorResiduo.put(residuoId, "COLETADO");
            } else {
                statusPorResiduo.putIfAbsent(residuoId, "RESERVADO");
            }
        }

        return residuos.stream()
                .map(residuo -> new ResiduoDisponibilidadeResponse(
                        residuo,
                        statusPorResiduo.getOrDefault(
                                residuo.getId(),
                                "DISPONIVEL"
                        )
                ))
                .toList();
    }


    // Verifica se o usuário é proprietário ou administrador
    private void exigirProprietarioOuAdmin(Long usuarioId) {

        Jwt jwt = obterJwt();

        boolean admin = "ADMIN".equals(jwt.getClaimAsString("perfil"));
        boolean proprietario = jwt.getSubject().equals(usuarioId.toString());

        if (!admin && !proprietario) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para modificar este resíduo"
            );
        }
    }

    // Recupera o JWT do usuário autenticado
    private Jwt obterJwt() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Autenticação necessária"
            );
        }

        return jwt;
    }


    // Consultar a disponibilidade de um resíduo
    public ResiduoDisponibilidadeResponse consultarDisponibilidade(Long id) {

        Residuo residuo = buscarPorId(id);

        String status;

        if (reservaRepository.existsByResiduoIdAndStatus(
                id, StatusReserva.COLETADA)) {

            status = "COLETADO";

        } else if (reservaRepository.existsByResiduoIdAndStatus(
                id, StatusReserva.RESERVADA)) {

            status = "RESERVADO";

        } else {
            status = "DISPONIVEL";
        }

        return new ResiduoDisponibilidadeResponse(residuo, status);
    }



    // Consulta administrativa: nome, categoria e disponibilidade.
    public List<backend.admin.dto.ResiduoAdminResponse> buscarResiduosAdmin(
            String busca, String categoria, String status) {
        exigirAdmin();
        String termo = busca == null ? "" : busca.trim().toLowerCase(java.util.Locale.ROOT);
        String filtroCategoria = categoria == null ? "" : categoria.trim();
        String filtroStatus = status == null ? "" : status.trim().toUpperCase(java.util.Locale.ROOT);
        if (!filtroStatus.isEmpty() && !List.of("DISPONIVEL", "RESERVADO", "COLETADO").contains(filtroStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido");
        }
        return listarFeed(null, false).stream()
                .filter(item -> termo.isEmpty() ||
                        item.residuo().getNome().toLowerCase(java.util.Locale.ROOT).contains(termo))
                .filter(item -> filtroCategoria.isEmpty() ||
                        item.residuo().getCategoria().equalsIgnoreCase(filtroCategoria))
                .filter(item -> filtroStatus.isEmpty() || item.status().equals(filtroStatus))
                .map(item -> backend.admin.dto.ResiduoAdminResponse.from(item.residuo(), item.status()))
                .toList();
    }

    public backend.admin.dto.ResiduoAdminResponse detalharResiduoAdmin(Long id) {
        exigirAdmin();
        var disponibilidade = consultarDisponibilidade(id);
        return backend.admin.dto.ResiduoAdminResponse.from(
                disponibilidade.residuo(), disponibilidade.status());
    }

    private void exigirAdmin() {
        if (!"ADMIN".equals(obterJwt().getClaimAsString("perfil"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso permitido apenas para administradores");
        }
    }

}
