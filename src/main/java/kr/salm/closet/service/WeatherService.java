package kr.salm.closet.service;

import kr.salm.closet.dto.WeatherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    @Value("${weather.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherResponse getWeather(double lat, double lon) {
        if (apiKey == null || apiKey.isBlank()) {
            // API 키 없으면 Mock 데이터
            return mockWeather();
        }

        try {
            String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric&lang=kr",
                lat, lon, apiKey
            );
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return parseResponse(response);
        } catch (Exception e) {
            log.error("날씨 API 호출 실패: {}", e.getMessage());
            return mockWeather();
        }
    }

    private WeatherResponse parseResponse(Map<String, Object> data) {
        Map<String, Object> main = (Map<String, Object>) data.get("main");
        Map<String, Object> weather = ((java.util.List<Map<String, Object>>) data.get("weather")).get(0);
        Map<String, Object> wind = (Map<String, Object>) data.get("wind");

        int temp = ((Number) main.get("temp")).intValue();
        String condition = (String) weather.get("main");

        return WeatherResponse.builder()
                .city((String) data.get("name"))
                .temperature(temp)
                .feelsLike(((Number) main.get("feels_like")).intValue())
                .condition(condition)
                .icon((String) weather.get("icon"))
                .humidity(((Number) main.get("humidity")).intValue())
                .windSpeed(((Number) wind.get("speed")).doubleValue())
                .recommendation(getRecommendation(temp, condition))
                .build();
    }

    private WeatherResponse mockWeather() {
        return WeatherResponse.builder()
                .city("서울")
                .temperature(5)
                .feelsLike(2)
                .condition("Clear")
                .icon("01d")
                .humidity(50)
                .windSpeed(3.5)
                .recommendation("쌀쌀해요! 따뜻한 외투를 챙기세요 🧥")
                .build();
    }

    private String getRecommendation(int temp, String condition) {
        if (temp >= 28) return "더워요! 시원한 반팔과 반바지 추천 🩳";
        if (temp >= 23) return "따뜻해요! 가벼운 셔츠나 얇은 긴팔 추천 👕";
        if (temp >= 17) return "선선해요! 가디건이나 얇은 자켓 추천 🧥";
        if (temp >= 10) return "쌀쌀해요! 자켓이나 니트 추천 🧶";
        if (temp >= 5) return "추워요! 코트나 두꺼운 외투 추천 🧥";
        return "많이 추워요! 패딩과 목도리 필수! 🥶";
    }
}
