package kr.salm.config;

import kr.salm.auth.service.CustomOAuth2UserService;
import kr.salm.auth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService oAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/**")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico", "/manifest.webmanifest", "/sw.js").permitAll()
                // 미디어: Nginx가 /media/** 와 레거시 /videos/**, /thumbnails/**, /clothes/** 를 직접 서빙하는 것이 정상.
                // Spring ResourceHandler는 nginx 미설정 환경/로컬 개발용 폴백.
                .requestMatchers("/media/**", "/videos/**", "/thumbnails/**", "/clothes/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/", "/login", "/signup", "/oauth2/**", "/api/auth/check/**").permitAll()
                .requestMatchers("/videos", "/videos/{id:[0-9]+}", "/category/**", "/feed").permitAll()
                .requestMatchers("/api/videos", "/api/videos/{id:[0-9]+}", "/api/videos/{id}/comments").permitAll()
                // closet
                .requestMatchers("/closet/**").authenticated()
                .requestMatchers("/api/closet/**").authenticated()
                // 나머지
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=oauth")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService)
                )
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
