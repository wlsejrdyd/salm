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

    public List<Category> findAll() {
        return categoryRepository.findAllEnabled();
    }

    public Category findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("카테고리"));
    }

    @Transactional
    public void initDefaultCategories() {
        if (categoryRepository.count() > 0) return;

        List<Category> defaults = List.of(
            Category.builder().name("요리/레시피").slug("cooking").icon("cooking").displayOrder(1).build(),
            Category.builder().name("생활 꿀팁").slug("lifehack").icon("lightbulb").displayOrder(2).build(),
            Category.builder().name("DIY/인테리어").slug("diy").icon("hammer").displayOrder(3).build(),
            Category.builder().name("뷰티/패션").slug("beauty").icon("sparkles").displayOrder(4).build(),
            Category.builder().name("운동/건강").slug("fitness").icon("dumbbell").displayOrder(5).build(),
            Category.builder().name("육아").slug("parenting").icon("baby").displayOrder(6).build(),
            Category.builder().name("반려동물").slug("pet").icon("paw").displayOrder(7).build()
        );
        categoryRepository.saveAll(defaults);
        log.info("기본 카테고리 {} 개 생성", defaults.size());
    }
}
