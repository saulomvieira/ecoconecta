
package backend.residuos.service;

import backend.residuos.model.Residuo;
import backend.residuos.repository.ResiduoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ResiduoService {

    private final ResiduoRepository repository;

    public ResiduoService(ResiduoRepository repository) {
        this.repository = repository;
    }

    public Residuo cadastrar(Residuo residuo) {
        return repository.save(residuo);
    }

    public List<Residuo> listar() {
        return repository.findAll();
    }

    public Residuo buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resíduo não encontrado"
                ));
    }

    public Residuo atualizar(Long id, Residuo dados) {
        Residuo residuo = buscarPorId(id);

        residuo.setNome(dados.getNome());
        residuo.setDescricao(dados.getDescricao());
        residuo.setCategoria(dados.getCategoria());
        residuo.setQuantidade(dados.getQuantidade());
        residuo.setUnidadeMedida(dados.getUnidadeMedida());
        residuo.setRegiao(dados.getRegiao());

        return repository.save(residuo);
    }

    public void excluir(Long id) {
        Residuo residuo = buscarPorId(id);
        repository.delete(residuo);
    }
}
