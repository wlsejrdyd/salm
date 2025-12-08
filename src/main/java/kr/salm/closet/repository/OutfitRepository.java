package kr.salm.closet.repository;

import kr.salm.auth.entity.User;
import kr.salm.closet.entity.Outfit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface OutfitRepository extends JpaRepository<Outfit, Long> {
    
    @Query("SELECT o FROM Outfit o WHERE o.user = :user ORDER BY o.createdAt DESC")
    Page<Outfit> findByUser(@Param("user") User user, Pageable pageable);
    
    @Query("SELECT o FROM Outfit o WHERE o.user = :user AND o.weatherType = :weatherType")
    List<Outfit> findByUserAndWeatherType(@Param("user") User user, @Param("weatherType") Outfit.WeatherType weatherType);
    
    long countByUser(User user);
}
