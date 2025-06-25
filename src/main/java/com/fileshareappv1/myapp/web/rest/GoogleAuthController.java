package com.fileshareappv1.myapp.web.rest;

import com.fileshareappv1.myapp.domain.User;
import com.fileshareappv1.myapp.repository.AuthorityRepository;
import com.fileshareappv1.myapp.repository.UserRepository;
import com.fileshareappv1.myapp.security.TokenProvider;
import com.fileshareappv1.myapp.service.UserService;
import com.fileshareappv1.myapp.service.dto.AdminUserDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GoogleAuthController {

    private final Logger log = LoggerFactory.getLogger(GoogleAuthController.class);
    private final TokenProvider tokenProvider;
    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final UserService userService;

    public GoogleAuthController(
        TokenProvider tokenProvider,
        UserRepository userRepository,
        AuthorityRepository authorityRepository,
        UserService userService
    ) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.userService = userService;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @PostMapping("/test-auth")
    @PermitAll
    public ResponseEntity<AuthenticateController.JWTToken> authenticateWithGoogle(@RequestBody Map<String, String> body)
        throws FirebaseAuthException {
        String idToken = body.get("ggToken");
        log.debug("Verifying Google ID token…");
        FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken);

        // You can look up your User by decoded.getUid() or decoded.getEmail()
        String uid = decoded.getUid();
        String email = decoded.getEmail();
        // (Optional) Auto-create a JHipster user here if none exists.
        Optional<User> existing = userRepository.findOneByLogin(uid);
        if (existing.isEmpty()) {
            AdminUserDTO dto = new AdminUserDTO();
            dto.setLogin(uid);
            dto.setEmail(email);
            dto.setActivated(true);
            authorityRepository.findById("ROLE_USER").ifPresent(a -> dto.setAuthorities(Set.of(a.getName())));
            userService.createUser(dto);
        }
        // Create a Spring Authentication and issue a JHipster JWT
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(email, null, java.util.List.of());
        var jwt = tokenProvider.createToken(auth, true);
        return ResponseEntity.ok(new AuthenticateController.JWTToken(jwt));
    }
}
