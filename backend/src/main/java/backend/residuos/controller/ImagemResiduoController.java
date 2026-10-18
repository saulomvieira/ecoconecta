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
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/residuos")
public class ImagemResiduoController {
    private final ResiduoService residuos;
    private final Path pasta;

    public ImagemResiduoController(ResiduoService residuos,
                                   @Value("${app.upload.dir:uploads/residuos}") String pasta) {
        this.residuos = residuos;
        this.pasta = Path.of(pasta).toAbsolutePath().normalize();
    }

    @PostMapping(value = "/{id}/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> enviar(@PathVariable Long id, @RequestParam("arquivo") MultipartFile arquivo,
                                      @AuthenticationPrincipal Jwt jwt) throws IOException {
        var residuo = residuos.buscarPorId(id);
        if (jwt == null || (!"ADMIN".equals(jwt.getClaimAsString("perfil"))
                && !jwt.getSubject().equals(residuo.getUsuario().getId().toString())))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem permissão para editar este resíduo");
        if (arquivo.isEmpty() || arquivo.getSize() > 5 * 1024 * 1024)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem vazia ou maior que 5 MB");
        byte[] assinatura = arquivo.getInputStream().readNBytes(12);
        String extensao = null;
        if (assinatura.length >= 3 && (assinatura[0] & 255) == 0xFF && (assinatura[1] & 255) == 0xD8 && (assinatura[2] & 255) == 0xFF)
            extensao = "jpg";
        else if (assinatura.length >= 8 && (assinatura[0] & 255) == 0x89 && assinatura[1] == 0x50 && assinatura[2] == 0x4E && assinatura[3] == 0x47)
            extensao = "png";
        else if (assinatura.length >= 12 && new String(assinatura, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF")
                && new String(assinatura, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP"))
            extensao = "webp";
        if (extensao == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use JPG, PNG ou WebP");
        Files.createDirectories(pasta);
        String nome = UUID.randomUUID() + "." + extensao;
        Path destino = pasta.resolve(nome);
        try (var stream = arquivo.getInputStream()) {
            Files.copy(stream, destino);
        }
        residuo.setImagemUrl("/residuos/imagens/" + nome);
        try {
            residuos.atualizar(id, residuo);
        } catch (RuntimeException ex) {
            Files.deleteIfExists(destino);
            throw ex;
        }
        return Map.of("imagemUrl", residuo.getImagemUrl());
    }

    @GetMapping("/imagens/{nome}")
    public ResponseEntity<Resource> obter(@PathVariable String nome) {
        if (!nome.matches("[0-9a-fA-F-]{36}\\.(jpg|png|webp)"))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Path arquivo = pasta.resolve(nome).normalize();
        if (!arquivo.startsWith(pasta) || !Files.isRegularFile(arquivo))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        String tipo = nome.endsWith(".png") ? "image/png" : nome.endsWith(".webp") ? "image/webp" : "image/jpeg";
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(tipo))
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(new FileSystemResource(arquivo));
    }
}
