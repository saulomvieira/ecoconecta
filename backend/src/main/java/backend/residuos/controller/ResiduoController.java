package backend.residuos.controller;

import backend.residuos.model.Residuo;
import backend.residuos.service.ResiduoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import backend.residuos.dto.ResiduoDisponibilidadeResponse;

import java.util.List;

@RestController
@RequestMapping("/residuos")
public class ResiduoController {

    private final ResiduoService service;

    public ResiduoController(ResiduoService service) {
        this.service = service;
    }

    @PostMapping
    public Residuo cadastrar(@Valid @RequestBody Residuo residuo) {
        return service.cadastrar(residuo);
    }

    @GetMapping
    public List<Residuo> listar() {
        return service.listar();
    }

    // Consultar disponibilidade de um resíduo
    @GetMapping("/{id}/disponibilidade")
    public ResiduoDisponibilidadeResponse consultarDisponibilidade(
            @PathVariable Long id) {

        return service.consultarDisponibilidade(id);
    }

    // Listar resíduos publicados pelo usuário autenticado
    @GetMapping("/meus")
    public List<Residuo> listarMeusResiduos(
            @AuthenticationPrincipal Jwt jwt) {

        Long usuarioId = Long.valueOf(jwt.getSubject());
        return service.listarPorUsuario(usuarioId);
    }

    // Filtrar resíduos por região
    @GetMapping("/regiao")
    public List<Residuo> listarPorRegiao(
            @RequestParam String nome) {

        return service.listarPorRegiao(nome);
    }

    // Listar resíduos mais recentes
    @GetMapping("/recentes")
    public List<Residuo> listarRecentes() {
        return service.listarRecentes();
    }


    @GetMapping("/{id}")
    public Residuo buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Residuo atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Residuo residuo) {
        return service.atualizar(id, residuo);
    }

    @DeleteMapping("/{id}")
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    // Listar feed com filtro ou priorização por região
    @GetMapping("/feed")
    public List<ResiduoDisponibilidadeResponse> listarFeed(
            @RequestParam(required = false) String regiao,
            @RequestParam(defaultValue = "false") boolean priorizarRegiao) {

        return service.listarFeed(regiao, priorizarRegiao);
    }
}
