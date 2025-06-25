package com.fileshareappv1.myapp.security;

import com.fileshareappv1.myapp.service.FirebaseUserService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter gaConverter = new JwtGrantedAuthoritiesConverter();
    private final FirebaseUserService firebaseUserService;

    public CustomJwtAuthenticationConverter(FirebaseUserService firebaseUserService) {
        this.firebaseUserService = firebaseUserService;
        gaConverter.setAuthorityPrefix("ROLE_");
        gaConverter.setAuthoritiesClaimName("roles"); // nếu bạn dùng custom-claim 'roles'
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // 1. Upsert User
        firebaseUserService.upsertFromJwt(jwt);
        // 2. Lấy authority từ claim (nếu có)
        var auths = gaConverter.convert(jwt);
        // 3. Trả về token
        return new JwtAuthenticationToken(jwt, auths, jwt.getSubject());
    }
}
