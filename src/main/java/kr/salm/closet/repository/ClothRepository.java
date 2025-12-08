package kr.salm.closet.repository;

import kr.salm.auth.entity.User;
import kr.salm.closet.entity.Cloth;
import kr.salm.closet.entity.ClothCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ClothRepository extends JpaRepository<Cloth, Long> {
    
    @Query("SELECT c FROM Cloth c WHERE c.user = :user AND c.isActive = true ORDER BY c.createdAt DESC")
    Page<Cloth> findByUser(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT c FROM Cloth c WHERE c.user = :user AND c.category = :category AND c.isActive = true ORDER BY c.createdAt DESC")
    Page<Cloth> findByUserAndCategoryPaged(@Param("user") User user, @Param("category") ClothCategory category, Pageable pageable);
    
    @Query("SELECT c FROM Cloth c WHERE c.user = :user AND c.category = :category AND c.isActive = true")
    List<Cloth> findByUserAndCategory(@Param("user") User user, @Param("category") ClothCategory category);

    @Query("SELECT c FROM Cloth c WHERE c.user = :user AND c.category.parentType = :parentType AND c.isActive = true")
    List<Cloth> findByUserAndParentType(@Param("user") User user, @Param("parentType") String parentType);

    // 온도에 맞는 카테고리의 옷 (카테고리에 내장된 온도 사용!)
    @Query("SELECT c FROM Cloth c WHERE c.user = :user AND c.category.parentType = :parentType AND c.isActive = true " +
           "AND c.category.tempMin <= :temp AND c.category.tempMax >= :temp")
    List<Cloth> findByUserAndParentTypeAndTemperature(@Param("user") User user, @Param("parentType") String parentType, @Param("temp") int temperature);
    
    @Query("SELECT c FROM Cloth c WHERE c.user = :user AND c.isFavorite = true AND c.isActive = true")
    List<Cloth> findFavorites(@Param("user") User user);
    
    long countByUser(User user);
}
