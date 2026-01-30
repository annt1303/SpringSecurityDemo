package com.security.demo.security;

import com.security.demo.entity.Role;
import com.security.demo.entity.User;
import com.security.demo.entity.UserAuthProvider;
import com.security.demo.entity.enums.AuthProvider;
import com.security.demo.entity.enums.RoleName;
import com.security.demo.repository.RoleRepository;
import com.security.demo.repository.UserAuthProviderRepository;
import com.security.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserAuthProviderRepository authProviderRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = userRequest
                .getClientRegistration()
                .getRegistrationId();

        AuthProvider provider = AuthProvider.valueOf(
                registrationId.toUpperCase()
        );

        Object providerIdObj = attributes.get("id");
        if (providerIdObj == null) {
            throw new OAuth2AuthenticationException("Provider user id not found");
        }

        String providerUserId = providerIdObj.toString();

        String email = (String) attributes.get("email");

        if (email == null) {
            throw new OAuth2AuthenticationException(
                    "Email not found from OAuth2 provider"
            );
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                            .orElseThrow();

                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName((String) attributes.get("name"));
                    newUser.setEnabled(true);
                    newUser.setRoles(Set.of(userRole));

                    return userRepository.save(newUser);
                });

        authProviderRepository
                .findByUserAndProvider(user, provider)
                .orElseGet(() -> {
                    UserAuthProvider uap = new UserAuthProvider();
                    uap.setUser(user);
                    uap.setProvider(provider);
                    uap.setProviderUserId(providerUserId);

                    return authProviderRepository.save(uap);
                });

        return oAuth2User;
    }
}
