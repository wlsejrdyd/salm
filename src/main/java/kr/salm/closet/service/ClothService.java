package kr.salm.closet.service;

import kr.salm.auth.entity.User;
import kr.salm.closet.dto.ClothRequest;
import kr.salm.closet.dto.ClothResponse;
import kr.salm.closet.entity.Cloth;
import kr.salm.closet.entity.ClothCategory;
import kr.salm.closet.repository.ClothCategoryRepository;
import kr.salm.closet.repository.ClothRepository;
import kr.salm.core.dto.PageResponse;
import kr.salm.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothService {

    private final ClothRepository clothRepository;
    private final ClothCategoryRepository categoryRepository;
    private final LinkScraperService linkScraperService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public Cloth create(ClothRequest request, MultipartFile image, User user) {
        ClothCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> BusinessException.notFound("카테고리"));

        String imagePath = null;
        String name = request.getName();

        if (image != null && !image.isEmpty()) {
            imagePath = saveImage(image);
        } else if (request.getProductUrl() != null && !request.getProductUrl().isBlank()) {
            var scraped = linkScraperService.scrape(request.getProductUrl());
            if (scraped != null) {
                if (scraped.imageUrl() != null) {
                    imagePath = scraped.imageUrl();
                }
                if ((name == null || name.isBlank()) && scraped.title() != null) {
                    name = scraped.title();
                }
            }
        }

        if (name == null || name.isBlank()) {
            name = category.getName();  // 카테고리명으로 기본값
        }

        Cloth cloth = Cloth.builder()
                .user(user)
                .category(category)
                .name(name)
                .brand(request.getBrand())
                .color(request.getColor())
                .imagePath(imagePath)
                .productUrl(request.getProductUrl())
                .build();

        return clothRepository.save(cloth);
    }

    @Transactional(readOnly = true)
    public PageResponse<ClothResponse> findByUser(User user, int page, int size) {
        Page<Cloth> clothes = clothRepository.findByUser(user, PageRequest.of(page, size));
        List<ClothResponse> content = clothes.getContent().stream()
                .map(ClothResponse::from)
                .collect(Collectors.toList());
        return PageResponse.of(clothes, content);
    }

    @Transactional(readOnly = true)
    public PageResponse<ClothResponse> findByCategoryPaged(User user, String categorySlug, int page, int size) {
        ClothCategory category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> BusinessException.notFound("카테고리"));
        
        Page<Cloth> clothes = clothRepository.findByUserAndCategoryPaged(user, category, PageRequest.of(page, size));
        
        List<ClothResponse> content = clothes.getContent().stream()
                .map(ClothResponse::from)
                .collect(Collectors.toList());
        return PageResponse.of(clothes, content);
    }

    @Transactional(readOnly = true)
    public List<ClothResponse> findByCategory(User user, String categorySlug) {
        ClothCategory category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> BusinessException.notFound("카테고리"));
        return clothRepository.findByUserAndCategory(user, category).stream()
                .map(ClothResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 현재 온도에 맞는 랜덤 코디 추천
     * - 카테고리에 내장된 온도 범위로 필터링
     */
    @Transactional(readOnly = true)
    public Map<String, ClothResponse> getRandomOutfit(User user, int temperature) {
        Map<String, ClothResponse> outfit = new HashMap<>();
        Random random = new Random();

        List<String> parentTypes = List.of("outer", "tops", "bottoms", "shoes");

        for (String parentType : parentTypes) {
            // 현재 온도에 맞는 카테고리의 옷 조회
            List<Cloth> candidates = clothRepository.findByUserAndParentTypeAndTemperature(user, parentType, temperature);
            
            if (!candidates.isEmpty()) {
                Cloth selected = candidates.get(random.nextInt(candidates.size()));
                outfit.put(parentType, ClothResponse.from(selected));
            }
        }

        return outfit;
    }

    @Transactional(readOnly = true)
    public ClothResponse findById(Long id, User user) {
        Cloth cloth = clothRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("옷"));
        if (!cloth.getUser().getId().equals(user.getId())) {
            throw BusinessException.forbidden("접근 권한이 없습니다.");
        }
        return ClothResponse.from(cloth);
    }

    @Transactional
    public void toggleFavorite(Long id, User user) {
        Cloth cloth = clothRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("옷"));
        if (!cloth.getUser().getId().equals(user.getId())) {
            throw BusinessException.forbidden("접근 권한이 없습니다.");
        }
        cloth.setIsFavorite(!cloth.getIsFavorite());
    }

    @Transactional
    public void delete(Long id, User user) {
        Cloth cloth = clothRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("옷"));
        if (!cloth.getUser().getId().equals(user.getId())) {
            throw BusinessException.forbidden("삭제 권한이 없습니다.");
        }
        cloth.setIsActive(false);
    }

    private String saveImage(MultipartFile file) {
        try {
            String ext = getExtension(file.getOriginalFilename());
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String filename = UUID.randomUUID() + "." + ext;
            Path path = Paths.get(uploadDir, "clothes", datePath, filename);
            Files.createDirectories(path.getParent());
            file.transferTo(path.toFile());
            return "/clothes/" + datePath + "/" + filename;
        } catch (Exception e) {
            log.error("이미지 저장 실패: {}", e.getMessage());
            throw BusinessException.badRequest("이미지 저장에 실패했습니다.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
