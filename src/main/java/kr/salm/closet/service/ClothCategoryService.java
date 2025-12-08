package kr.salm.closet.service;

import kr.salm.closet.entity.ClothCategory;
import kr.salm.closet.repository.ClothCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothCategoryService {
    
    private final ClothCategoryRepository categoryRepository;

    public List<ClothCategory> findAll() {
        return categoryRepository.findAllEnabled();
    }

    public List<ClothCategory> findRootCategories() {
        return categoryRepository.findParentTypes();
    }

    @Transactional
    public void initDefaultCategories() {
        if (categoryRepository.count() > 0) return;

        List<ClothCategory> categories = List.of(
            // ===== 아우터 (outer) =====
            ClothCategory.builder().name("패딩").slug("puffer").parentType("outer").icon("🧥").tempMin(-20).tempMax(5).layerOrder(1).build(),
            ClothCategory.builder().name("코트").slug("coat").parentType("outer").icon("🧥").tempMin(0).tempMax(12).layerOrder(1).build(),
            ClothCategory.builder().name("자켓").slug("jacket").parentType("outer").icon("🧥").tempMin(10).tempMax(20).layerOrder(1).build(),
            ClothCategory.builder().name("가디건").slug("cardigan").parentType("outer").icon("🧥").tempMin(12).tempMax(22).layerOrder(1).build(),
            ClothCategory.builder().name("바람막이").slug("windbreaker").parentType("outer").icon("🧥").tempMin(10).tempMax(25).layerOrder(1).build(),

            // ===== 상의 (tops) =====
            ClothCategory.builder().name("니트").slug("knit").parentType("tops").icon("👕").tempMin(0).tempMax(15).layerOrder(2).build(),
            ClothCategory.builder().name("맨투맨").slug("sweatshirt").parentType("tops").icon("👕").tempMin(8).tempMax(20).layerOrder(2).build(),
            ClothCategory.builder().name("후드").slug("hoodie").parentType("tops").icon("👕").tempMin(5).tempMax(18).layerOrder(2).build(),
            ClothCategory.builder().name("셔츠").slug("shirt").parentType("tops").icon("👕").tempMin(15).tempMax(28).layerOrder(2).build(),
            ClothCategory.builder().name("티셔츠").slug("tshirt").parentType("tops").icon("👕").tempMin(20).tempMax(35).layerOrder(2).build(),
            ClothCategory.builder().name("민소매").slug("sleeveless").parentType("tops").icon("👕").tempMin(25).tempMax(40).layerOrder(2).build(),

            // ===== 하의 (bottoms) =====
            ClothCategory.builder().name("청바지").slug("jeans").parentType("bottoms").icon("👖").tempMin(-10).tempMax(30).layerOrder(3).build(),
            ClothCategory.builder().name("슬랙스").slug("slacks").parentType("bottoms").icon("👖").tempMin(5).tempMax(30).layerOrder(3).build(),
            ClothCategory.builder().name("면바지").slug("cotton-pants").parentType("bottoms").icon("👖").tempMin(10).tempMax(30).layerOrder(3).build(),
            ClothCategory.builder().name("반바지").slug("shorts").parentType("bottoms").icon("🩳").tempMin(23).tempMax(40).layerOrder(3).build(),
            ClothCategory.builder().name("스커트").slug("skirt").parentType("bottoms").icon("👗").tempMin(15).tempMax(35).layerOrder(3).build(),

            // ===== 신발 (shoes) =====
            ClothCategory.builder().name("운동화").slug("sneakers").parentType("shoes").icon("👟").tempMin(-10).tempMax(35).layerOrder(4).build(),
            ClothCategory.builder().name("부츠").slug("boots").parentType("shoes").icon("👢").tempMin(-20).tempMax(15).layerOrder(4).build(),
            ClothCategory.builder().name("구두").slug("dress-shoes").parentType("shoes").icon("👞").tempMin(5).tempMax(30).layerOrder(4).build(),
            ClothCategory.builder().name("샌들").slug("sandals").parentType("shoes").icon("🩴").tempMin(22).tempMax(40).layerOrder(4).build(),
            ClothCategory.builder().name("슬리퍼").slug("slippers").parentType("shoes").icon("🩴").tempMin(20).tempMax(40).layerOrder(4).build(),

            // ===== 악세서리 (accessories) =====
            ClothCategory.builder().name("모자").slug("hat").parentType("accessories").icon("🧢").tempMin(-30).tempMax(40).layerOrder(5).build(),
            ClothCategory.builder().name("목도리").slug("scarf").parentType("accessories").icon("🧣").tempMin(-20).tempMax(10).layerOrder(5).build(),
            ClothCategory.builder().name("장갑").slug("gloves").parentType("accessories").icon("🧤").tempMin(-20).tempMax(5).layerOrder(5).build(),
            ClothCategory.builder().name("가방").slug("bag").parentType("accessories").icon("👜").tempMin(-30).tempMax(40).layerOrder(5).build()
        );
        
        categoryRepository.saveAll(categories);
        log.info("옷 카테고리 {}개 생성 완료", categories.size());
    }
}
