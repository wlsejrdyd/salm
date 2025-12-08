package kr.salm.auth.service;

import kr.salm.auth.entity.User;
import kr.salm.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        OAuthUserInfo userInfo = extractUserInfo(provider, attributes);
        User user = saveOrUpdate(provider, userInfo);
        
        return new CustomOAuth2User(user, attributes);
    }

    private OAuthUserInfo extractUserInfo(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "google" -> new OAuthUserInfo(
                (String) attributes.get("sub"),
                (String) attributes.get("email"),
                (String) attributes.get("name"),
                (String) attributes.get("picture")
            );
            default -> throw new OAuth2AuthenticationException("지원하지 않는 OAuth 제공자: " + provider);
        };
    }

    private User saveOrUpdate(String provider, OAuthUserInfo info) {
        return userRepository.findByProviderAndProviderId(provider, info.id)
            .map(user -> {
                // 기존 사용자 정보 업데이트
                user.setNickname(info.name);
                user.setProfileImage(info.picture);
                return user;
            })
            .orElseGet(() -> {
                // 신규 사용자 생성
                String username = provider + "_" + info.id;
                
                // 이메일 중복 체크
                if (userRepository.existsByEmail(info.email)) {
                    // 기존 계정과 연동
                    User existingUser = userRepository.findByEmail(info.email).get();
                    existingUser.setProvider(provider);
                    existingUser.setProviderId(info.id);
                    existingUser.setProfileImage(info.picture);
                    log.info("기존 계정과 OAuth 연동: {}", info.email);
                    return existingUser;
                }

                User newUser = User.builder()
                    .username(username)
                    .email(info.email)
                    .nickname(info.name)
                    .profileImage(info.picture)
                    .provider(provider)
                    .providerId(info.id)
                    .build();
                
                log.info("OAuth 신규 가입: {} ({})", info.email, provider);
                return userRepository.save(newUser);
            });
    }

    private record OAuthUserInfo(String id, String email, String name, String picture) {}
}
