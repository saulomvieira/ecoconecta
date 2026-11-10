package backend.usuarios.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelefoneTest {

    @Test
    void mantemSomenteOsDigitos() {
        assertThat(UsuarioService.normalizarTelefone("(85) 99999-9999")).isEqualTo("85999999999");
        assertThat(UsuarioService.normalizarTelefone(" 85 3222-1111 ")).isEqualTo("8532221111");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "abc", "()-"})
    void telefoneAusenteEhObrigatorio(String valor) {
        assertThatThrownBy(() -> UsuarioService.normalizarTelefone(valor))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(e.getReason()).isEqualTo("O telefone é obrigatório");
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "8",                // curto demais
            "85",               // só o DDD
            "859999999",        // 9 dígitos
            "859999999999",     // 12 dígitos
            "0899999999",       // DDD começando em 0
            "1099999999",       // DDD inexistente
            "85899999999"       // 11 dígitos que não é celular (falta o 9)
    })
    void rejeitaTelefoneInvalido(String valor) {
        assertThatThrownBy(() -> UsuarioService.normalizarTelefone(valor))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(e.getReason()).startsWith("Telefone inválido");
                });
    }
}
