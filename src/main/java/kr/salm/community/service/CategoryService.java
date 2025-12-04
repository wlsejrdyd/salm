package kr.salm.community.service;

import kr.salm.community.entity.Category;
import kr.salm.community.repository.CategoryRepository;
import kr.salm.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAllEnabled();
    }

    @Transactional(readOnly = true)
    public Category findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("카테고리"));
    }

    @Transactional(readOnly = true)
    public Category findByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> BusinessException.notFound("카테고리"));
    }

    /**
     * 초기 카테고리 생성 (앱 시작 시)
     */
    @Transactional
    public void initDefaultCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }

        List<Category> defaults = List.of(
            Category.builder().name("일상").slug("daily").icon("🏠").displayOrder(1).build(),
            Category.builder().name("주방").slug("kitchen").icon("🍳").displayOrder(2).build(),
            Category.builder().name("욕실").slug("bathroom").icon("🚿").displayOrder(3).build(),
            Category.builder().name("청소").slug("cleaning").icon("🧹").displayOrder(4).build(),
            Category.builder().name("반려동물").slug("pet").icon("🐾").displayOrder(5).build()
        );

        categoryRepository.saveAll(defaults);
        log.info("기본 카테고리 {} 개 생성 완료", defaults.size());
    }
}
