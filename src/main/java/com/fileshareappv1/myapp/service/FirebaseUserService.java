package com.fileshareappv1.myapp.service;

import com.fileshareappv1.myapp.domain.User;
import com.fileshareappv1.myapp.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FirebaseUserService {

    private final UserRepository userRepository;

    public FirebaseUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User upsertFromJwt(Jwt jwt) {
        String uid = jwt.getSubject();
        String email = jwt.getClaim("email");

        return userRepository
            .findOneByLogin(uid)
            .or(() -> userRepository.findOneByEmailIgnoreCase(email))
            .map(u -> {
                // Cập nhật nếu cần
                u.setLastModifiedBy("firebase");
                return userRepository.save(u);
            })
            .orElseGet(() -> {
                // Tạo mới
                User u = new User();
                u.setLogin(uid);
                u.setEmail(email);
                u.setActivated(true);
                u.setCreatedBy("firebase");
                return userRepository.save(u);
            });
    }
}
