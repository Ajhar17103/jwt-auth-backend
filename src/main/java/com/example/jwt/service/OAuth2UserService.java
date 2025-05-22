package com.example.jwt.service;

import com.example.jwt.entity.Users;
import com.example.jwt.repository.UsersRepo;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UsersRepo usersRepo;

    public OAuth2UserService(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        String email = user.getAttribute("email");
        String name = user.getAttribute("name");

        Users existingUser = usersRepo.findByEmail(email)
                .orElseGet(() -> {
                    Users newUser = new Users();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setRole("USER");
                    newUser.setPassword(""); // Not used for OAuth
                    newUser.setActive(true);
                    newUser.setIs_deleted(0);
                    return usersRepo.save(newUser);
                });

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(existingUser.getRole())),
                user.getAttributes(),
                "email"
        );
    }

}

