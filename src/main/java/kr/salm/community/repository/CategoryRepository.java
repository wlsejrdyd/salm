package kr.salm.community.repository;

import kr.salm.community.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.*;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    
    @Query("SELECT c FROM Category c WHERE c.enabled = true ORDER BY c.displayOrder")
    List<Category> findAllEnabled();
}
