package kr.salm.auth.service;

import kr.salm.auth.entity.User;
import kr.salm.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

/**
 * OAuth2 로그인 서비스 (스탠바이 - OAuth 키 입력 시 활성화)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String registrationId = request.getClientRegistration().getRegistrationId();
        String userNameAttribute = request.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        OAuthAttributes oAuthAttributes = extractAttributes(registrationId, attributes);

        User user = saveOrUpdate(registrationId, oAuthAttributes);

        log.info("OAuth2 로그인: {} ({})", user.getUsername(), registrationId);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                attributes,
                userNameAttribute
        );
    }

    private OAuthAttributes extractAttributes(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> extractGoogle(attributes);
            case "kakao" -> extractKakao(attributes);
            case "naver" -> extractNaver(attributes);
            default -> throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_provider"),
                    "지원하지 않는 OAuth 제공자입니다: " + registrationId
            );
        };
    }

    private OAuthAttributes extractGoogle(Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .providerId((String) attributes.get("sub"))
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .picture((String) attributes.get("picture"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private OAuthAttributes extractKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .providerId(String.valueOf(attributes.get("id")))
                .email((String) kakaoAccount.get("email"))
                .name((String) profile.get("nickname"))
                .picture((String) profile.get("profile_image_url"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private OAuthAttributes extractNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .providerId((String) response.get("id"))
                .email((String) response.get("email"))
                .name((String) response.get("name"))
                .picture((String) response.get("profile_image"))
                .build();
    }

    private User saveOrUpdate(String provider, OAuthAttributes attrs) {
        User user = userRepository.findByProviderAndProviderId(provider, attrs.providerId())
                .map(existingUser -> {
                    // 기존 사용자 정보 업데이트
                    if (attrs.name() != null) existingUser.setNickname(attrs.name());
                    if (attrs.picture() != null) existingUser.setProfileImage(attrs.picture());
                    return existingUser;
                })
                .orElseGet(() -> createOAuthUser(provider, attrs));

        return userRepository.save(user);
    }

    private User createOAuthUser(String provider, OAuthAttributes attrs) {
        return User.builder()
                .username(provider + "_" + attrs.providerId())
                .password("OAUTH_USER")
                .email(attrs.email() != null ? attrs.email() : "")
                .nickname(attrs.name() != null ? attrs.name() : provider + " 사용자")
                .profileImage(attrs.picture())
                .provider(provider)
                .providerId(attrs.providerId())
                .role("USER")
                .enabled(true)
                .build();
    }

    private record OAuthAttributes(
            String providerId,
            String email,
            String name,
            String picture
    ) {
        static Builder builder() {
            return new Builder();
        }

        static class Builder {
            private String providerId;
            private String email;
            private String name;
            private String picture;

            Builder providerId(String providerId) { this.providerId = providerId; return this; }
            Builder email(String email) { this.email = email; return this; }
            Builder name(String name) { this.name = name; return this; }
            Builder picture(String picture) { this.picture = picture; return this; }

            OAuthAttributes build() {
                return new OAuthAttributes(providerId, email, name, picture);
            }
        }
    }
}
