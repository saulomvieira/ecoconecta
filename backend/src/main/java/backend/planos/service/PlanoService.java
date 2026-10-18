package backend.planos.service;

import backend.planos.model.Plano;
import backend.assinaturas.repository.AssinaturaRepository;
import backend.planos.repository.PlanoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PlanoService {

    private final PlanoRepository planoRepository;
    private final AssinaturaRepository assinaturaRepository;

    public PlanoService(PlanoRepository planoRepository, AssinaturaRepository assinaturaRepository) {
        this.planoRepository = planoRepository;
        this.assinaturaRepository = assinaturaRepository;
    }

    public Plano cadastrar(Plano plano) {
        plano.setId(null);
        return planoRepository.save(plano);
    }

    public List<Plano> listarTodos() {
        return planoRepository.findAll();
    }

    public Plano buscarPorId(Long id) {
        return planoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Plano não encontrado"));
    }

    public Plano atualizar(Long id, Plano dados) {
        Plano plano = buscarPorId(id);

        plano.setNome(dados.getNome());
        plano.setPreco(dados.getPreco());
        plano.setLimiteColetasMes(dados.getLimiteColetasMes());
        plano.setLimiteColetasSemana(dados.getLimiteColetasSemana());
        plano.setBeneficios(dados.getBeneficios());
        plano.setAtivo(dados.getAtivo());

        return planoRepository.save(plano);
    }

    public void excluir(Long id) {
        Plano plano = buscarPorId(id);
        if (assinaturaRepository.existsByPlanoId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O plano possui histórico de assinaturas; desative-o em vez de excluir");
        }
        planoRepository.delete(plano);
    }
}
