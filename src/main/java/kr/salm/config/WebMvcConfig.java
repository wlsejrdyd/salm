package kr.salm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * 미디어 서빙 정책:
 *   - 운영에서는 Nginx 가 location /media/ 로 직접 서빙 (X-Accel/sendfile, aio threads).
 *   - 아래 ResourceHandler 는 Nginx 미설정 환경(로컬 개발/장애시)을 위한 폴백.
 *   - 레거시 /videos/, /thumbnails/, /clothes/ 경로는 기존 DB 데이터 호환을 위해 유지.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 신규: /media/{videos|thumbnails|clothes}/...
        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());

        // 레거시 호환
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:" + uploadDir + "/videos/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:" + uploadDir + "/thumbnails/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
        registry.addResourceHandler("/clothes/**")
                .addResourceLocations("file:" + uploadDir + "/clothes/")
                .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
    }
}
