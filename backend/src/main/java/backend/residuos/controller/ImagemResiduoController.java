
package backend.residuos.controller;

import backend.residuos.service.ResiduoService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/residuos")
public class ImagemResiduoController {

    private final ResiduoService residuos;
    private final Path pasta;

    public ImagemResiduoController(
            ResiduoService residuos,
            @Value("${app.upload.dir:uploads/residuos}") String pasta
    ) {
        this.residuos = residuos;
        this.pasta = Path.of(pasta).toAbsolutePath().normalize();
    }

    @PostMapping(
            value = "/{id}/imagem",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, String> enviar(
            @PathVariable Long id,
            @RequestParam("arquivo") MultipartFile arquivo,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {

        var residuo = residuos.buscarPorId(id);

        // Verifica se o usuário tem permissão
        if (jwt == null ||
                (!"ADMIN".equals(jwt.getClaimAsString("perfil"))
                        && !jwt.getSubject().equals(
                        residuo.getUsuario().getId().toString()
                ))) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Sem permissão para editar este resíduo"
            );
        }

        // Valida o tamanho do arquivo
        if (arquivo.isEmpty() || arquivo.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Imagem vazia ou maior que 5 MB"
            );
        }

        // Identifica o formato real da imagem
        byte[] assinatura;

        try (InputStream stream = arquivo.getInputStream()) {
            assinatura = stream.readNBytes(12);
        }

        String extensao = null;

        // JPEG
        if (assinatura.length >= 3
                && (assinatura[0] & 255) == 0xFF
                && (assinatura[1] & 255) == 0xD8
                && (assinatura[2] & 255) == 0xFF) {

            extensao = "jpg";
        }

        // PNG
        else if (assinatura.length >= 8
                && (assinatura[0] & 255) == 0x89
                && assinatura[1] == 0x50
                && assinatura[2] == 0x4E
                && assinatura[3] == 0x47) {

            extensao = "png";
        }

        // WebP
        else if (assinatura.length >= 12
                && new String(
                assinatura, 0, 4, StandardCharsets.US_ASCII
        ).equals("RIFF")
                && new String(
                assinatura, 8, 4, StandardCharsets.US_ASCII
        ).equals("WEBP")) {

            extensao = "webp";
        }

        if (extensao == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Use JPG, PNG ou WebP"
            );
        }

        // Cria a pasta de uploads
        Files.createDirectories(pasta);

        // Gera um nome único
        String nome = UUID.randomUUID() + "." + extensao;
        Path destino = pasta.resolve(nome);

        // Salva a imagem
        try (InputStream stream = arquivo.getInputStream()) {
            Files.copy(stream, destino);
        }

        // Atualiza a URL da imagem
        residuo.setImagemUrl("/residuos/imagens/" + nome);

        try {
            residuos.atualizar(id, residuo);
        } catch (RuntimeException ex) {
            Files.deleteIfExists(destino);
            throw ex;
        }

        return Map.of(
                "imagemUrl",
                residuo.getImagemUrl()
        );
    }

    @GetMapping("/imagens/{nome}")
    public ResponseEntity<Resource> obter(
            @PathVariable String nome
    ) {

        // Valida o nome do arquivo
        if (!nome.matches("[0-9a-fA-F-]{36}\\.(jpg|png|webp)")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Path arquivo = pasta.resolve(nome).normalize();

        // Verifica se o arquivo existe
        if (!arquivo.startsWith(pasta)
                || !Files.isRegularFile(arquivo)) {

            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Identifica o tipo da imagem
        String tipo = nome.endsWith(".png")
                ? "image/png"
                : nome.endsWith(".webp")
                  ? "image/webp"
                  : "image/jpeg";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(tipo))
                .header(
                        HttpHeaders.CACHE_CONTROL,
                        "private, max-age=3600"
                )
                .body(new FileSystemResource(arquivo));
    }
}
