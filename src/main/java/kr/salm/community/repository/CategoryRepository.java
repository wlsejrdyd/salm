package kr.salm.community.repository;

import kr.salm.community.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);

    @Query("SELECT c FROM Category c WHERE c.enabled = true ORDER BY c.displayOrder ASC")
    List<Category> findAllEnabled();

    boolean existsBySlug(String slug);

    boolean existsByName(String name);
}
