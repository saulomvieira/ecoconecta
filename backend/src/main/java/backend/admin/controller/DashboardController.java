
package backend.admin.controller;

import backend.admin.dto.DashboardResponse;
import backend.admin.service.DashboardService;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import backend.admin.dto.ResiduoMensalResponse;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import backend.admin.dto.AtividadeRecenteResponse;

@RestController
@RequestMapping("/admin")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // Estatísticas do painel administrativo
    @GetMapping("/dashboard")
    public DashboardResponse obterDashboard() {

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
                    "Acesso permitido apenas para administradores"
            );
        }

        return dashboardService.obterEstatisticas();
    }

    @GetMapping("/residuos-mensais")
    public List<ResiduoMensalResponse> listarResiduosMensais(
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuário não autenticado"
            );
        }

        String perfil = jwt.getClaimAsString("perfil");

        if (!"ADMIN".equals(perfil)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso permitido apenas para administradores"
            );
        }

        return dashboardService.listarResiduosPorMes();
    }

    @GetMapping("/atividades-recentes")
    public List<AtividadeRecenteResponse> listarAtividadesRecentes(
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuário não autenticado"
            );
        }

        String perfil = jwt.getClaimAsString("perfil");

        if (!"ADMIN".equals(perfil)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso permitido apenas para administradores"
            );
        }

        return dashboardService.listarAtividadesRecentes();
    }
}
