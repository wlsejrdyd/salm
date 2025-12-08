package kr.salm.closet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class LinkScraperService {

    /**
     * URL에서 og:image 또는 이미지 URL 추출
     */
    public ScrapedData scrape(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            return null;
        }

        try {
            String html = fetchHtml(urlString);
            if (html == null) return null;

            String image = extractOgImage(html);
            String title = extractOgTitle(html);
            String price = extractPrice(html);

            return new ScrapedData(image, title, price);
        } catch (Exception e) {
            log.error("링크 스크래핑 실패: {} - {}", urlString, e.getMessage());
            return null;
        }
    }

    private String fetchHtml(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null && count < 500) {
                    sb.append(line);
                    count++;
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("HTML 가져오기 실패: {}", e.getMessage());
            return null;
        }
    }

    private String extractOgImage(String html) {
        // og:image 찾기
        Pattern pattern = Pattern.compile("<meta[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // 반대 순서도 체크
        pattern = Pattern.compile("<meta[^>]*content=[\"']([^\"']+)[\"'][^>]*property=[\"']og:image[\"']", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // twitter:image 시도
        pattern = Pattern.compile("<meta[^>]*name=[\"']twitter:image[\"'][^>]*content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String extractOgTitle(String html) {
        Pattern pattern = Pattern.compile("<meta[^>]*property=[\"']og:title[\"'][^>]*content=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // title 태그
        pattern = Pattern.compile("<title>([^<]+)</title>", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String extractPrice(String html) {
        // 가격 패턴 (한국 원화)
        Pattern pattern = Pattern.compile("([0-9,]+)\\s*원");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1) + "원";
        }
        return null;
    }

    public record ScrapedData(String imageUrl, String title, String price) {}
}
