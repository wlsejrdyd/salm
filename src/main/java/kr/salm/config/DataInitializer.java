package kr.salm.config;

import kr.salm.closet.service.ClothCategoryService;
import kr.salm.community.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final CategoryService categoryService;
    private final ClothCategoryService clothCategoryService;

    @Override
    public void run(String... args) {
        categoryService.initDefaultCategories();
        clothCategoryService.initDefaultCategories();
    }
}
