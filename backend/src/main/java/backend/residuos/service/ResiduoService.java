
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

import java.util.List;

@Service
public class ResiduoService {

    private final ResiduoRepository repository;
    private final UsuarioRepository usuarioRepository;

    public ResiduoService(
            ResiduoRepository repository,
            UsuarioRepository usuarioRepository) {

        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
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

        exigirProprietarioOuAdmin(residuo.getUsuario().getId());

        residuo.setNome(dados.getNome());
        residuo.setDescricao(dados.getDescricao());
        residuo.setCategoria(dados.getCategoria());
        residuo.setQuantidade(dados.getQuantidade());
        residuo.setUnidadeMedida(dados.getUnidadeMedida());
        residuo.setRegiao(dados.getRegiao());
        residuo.setEnderecoColeta(dados.getEnderecoColeta());
        residuo.setImagemUrl(dados.getImagemUrl());

        // O proprietário não pode ser alterado por este endpoint.
        return repository.save(residuo);
    }

    // Excluir resíduo
    public void excluir(Long id) {

        Residuo residuo = buscarPorId(id);

        exigirProprietarioOuAdmin(residuo.getUsuario().getId());

        repository.delete(residuo);
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
}
