package backend.auth.service;
import backend.auth.model.RecuperacaoSenha;
import backend.auth.repository.RecuperacaoSenhaRepository;
import backend.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
@Service
public class RecuperacaoSenhaService {
    private final UsuarioRepository usuarios;
    private final RecuperacaoSenhaRepository tokens;
    private final ObjectProvider<JavaMailSender> mailProvider;
    private final String remetente;
    private final String smtpHost;
    private final String urlFrontend;
    private final SecureRandom random = new SecureRandom();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public RecuperacaoSenhaService(UsuarioRepository usuarios, RecuperacaoSenhaRepository tokens,
            ObjectProvider<JavaMailSender> mailProvider,
            @Value("${app.mail.from:}") String remetente,
            @Value("${spring.mail.host:}") String smtpHost,
            @Value("${app.frontend.url:http://localhost:3000}") String urlFrontend) {
        this.usuarios=usuarios; this.tokens=tokens; this.mailProvider=mailProvider;
        this.remetente=remetente; this.smtpHost=smtpHost; this.urlFrontend=urlFrontend;
    }
    @Transactional
    public void solicitar(String email) {
        JavaMailSender mail = mailProvider.getIfAvailable();
        if (mail==null || remetente.isBlank() || smtpHost.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Envio de e-mail não configurado");
        }
        if (email==null || email.isBlank()) return;
        var usuario=usuarios.findByEmail(email.trim()).orElse(null);
        if (usuario==null || !Boolean.TRUE.equals(usuario.getAtivo())) return;
        byte[] bytes=new byte[32]; random.nextBytes(bytes);
        String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        var recuperacao=new RecuperacaoSenha();
        recuperacao.setUsuario(usuario); recuperacao.setTokenHash(hash(token));
        recuperacao.setExpiraEm(LocalDateTime.now().plusMinutes(30));
        tokens.save(recuperacao);
        SimpleMailMessage msg=new SimpleMailMessage();
        msg.setFrom(remetente); msg.setTo(usuario.getEmail());
        msg.setSubject("EcoConecta - Recuperação de senha");
        msg.setText("Para redefinir sua senha, acesse: " + urlFrontend.replaceAll("/+$", "")
                + "/redefinir-senha?token=" + token + "\nO link expira em 30 minutos.");
        mail.send(msg);
    }
    @Transactional
    public void redefinir(String token,String novaSenha) {
        if (token==null || token.isBlank() || novaSenha==null || novaSenha.length()<8)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Token ou senha inválidos (mínimo 8 caracteres)");
        var registro=tokens.findByTokenHashAndUtilizadoFalse(hash(token)).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.BAD_REQUEST,"Token inválido ou utilizado"));
        if (!LocalDateTime.now().isBefore(registro.getExpiraEm()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Token expirado");
        var usuario=registro.getUsuario();
        if (!Boolean.TRUE.equals(usuario.getAtivo()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Token inválido");
        usuario.setSenha(encoder.encode(novaSenha));
        usuarios.save(usuario);
        registro.setUtilizado(true); tokens.save(registro);
    }
    private String hash(String token) {
        try {
            byte[] bytes=MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
