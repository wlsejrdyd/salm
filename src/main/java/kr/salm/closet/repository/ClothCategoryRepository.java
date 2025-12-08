package kr.salm.closet.repository;

import kr.salm.closet.entity.ClothCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ClothCategoryRepository extends JpaRepository<ClothCategory, Long> {
    Optional<ClothCategory> findBySlug(String slug);
    
    @Query("SELECT DISTINCT c.parentType FROM ClothCategory c WHERE c.enabled = true ORDER BY c.layerOrder")
    List<String> findDistinctParentTypes();

    @Query("SELECT c FROM ClothCategory c WHERE c.enabled = true ORDER BY c.parentType, c.layerOrder")
    List<ClothCategory> findAllEnabled();

    @Query("SELECT c FROM ClothCategory c WHERE c.parentType = :parentType AND c.enabled = true ORDER BY c.tempMin")
    List<ClothCategory> findByParentType(@Param("parentType") String parentType);

    // 현재 온도에 맞는 카테고리들
    @Query("SELECT c FROM ClothCategory c WHERE c.parentType = :parentType AND c.enabled = true " +
           "AND c.tempMin <= :temp AND c.tempMax >= :temp ORDER BY c.layerOrder")
    List<ClothCategory> findByParentTypeAndTemperature(@Param("parentType") String parentType, @Param("temp") int temperature);

    default List<ClothCategory> findParentTypes() {
        return findAllEnabled();
    }
}
