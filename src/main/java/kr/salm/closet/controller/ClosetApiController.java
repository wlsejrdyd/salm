package kr.salm.closet.controller;

import jakarta.validation.Valid;
import kr.salm.auth.entity.User;
import kr.salm.auth.service.AuthUtil;
import kr.salm.closet.dto.*;
import kr.salm.closet.service.*;
import kr.salm.core.dto.ApiResponse;
import kr.salm.core.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/closet")
@RequiredArgsConstructor
public class ClosetApiController {

    private final ClothService clothService;
    private final WeatherService weatherService;
    private final LinkScraperService linkScraperService;

    @GetMapping("/clothes")
    public ResponseEntity<ApiResponse<PageResponse<ClothResponse>>> getClothes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        return ResponseEntity.ok(ApiResponse.success(clothService.findByUser(user, page, size)));
    }

    @GetMapping("/clothes/category/{slug}")
    public ResponseEntity<ApiResponse<List<ClothResponse>>> getClothesByCategory(@PathVariable String slug) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        return ResponseEntity.ok(ApiResponse.success(clothService.findByCategory(user, slug)));
    }

    @PostMapping("/clothes")
    public ResponseEntity<ApiResponse<ClothResponse>> addCloth(
            @Valid @ModelAttribute ClothRequest request,
            @RequestParam(required = false) MultipartFile image) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        var cloth = clothService.create(request, image, user);
        return ResponseEntity.ok(ApiResponse.success(ClothResponse.from(cloth)));
    }

    @PostMapping("/clothes/{id}/favorite")
    public ResponseEntity<ApiResponse<Void>> toggleFavorite(@PathVariable Long id) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        clothService.toggleFavorite(id, user);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/clothes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCloth(@PathVariable Long id) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        clothService.delete(id, user);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/weather")
    public ResponseEntity<ApiResponse<WeatherResponse>> getWeather(
            @RequestParam(defaultValue = "37.5665") double lat,
            @RequestParam(defaultValue = "126.9780") double lon) {
        return ResponseEntity.ok(ApiResponse.success(weatherService.getWeather(lat, lon)));
    }

    @GetMapping("/outfit/random")
    public ResponseEntity<ApiResponse<Map<String, ClothResponse>>> getRandomOutfit(
            @RequestParam(required = false) Integer temperature) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("UNAUTHORIZED", "로그인이 필요합니다."));
        }
        
        if (temperature == null) {
            var weather = weatherService.getWeather(37.5665, 126.9780);
            temperature = weather.getTemperature();
        }
        
        return ResponseEntity.ok(ApiResponse.success(clothService.getRandomOutfit(user, temperature)));
    }

    @PostMapping("/scrape")
    public ResponseEntity<ApiResponse<Map<String, String>>> scrapeLink(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("BAD_REQUEST", "URL을 입력해주세요."));
        }

        var scraped = linkScraperService.scrape(url);
        if (scraped == null) {
            return ResponseEntity.ok(ApiResponse.success(Map.of()));
        }

        Map<String, String> result = new HashMap<>();
        if (scraped.imageUrl() != null) result.put("imageUrl", scraped.imageUrl());
        if (scraped.title() != null) result.put("title", scraped.title());
        if (scraped.price() != null) result.put("price", scraped.price());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
