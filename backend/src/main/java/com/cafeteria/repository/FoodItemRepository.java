package com.cafeteria.repository;

import com.cafeteria.entity.FoodItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItemEntity, Long> {

    List<FoodItemEntity> findByCategory(String category);

    Optional<FoodItemEntity> findByNameIgnoreCaseAndCategoryIgnoreCase(String name, String category);
}
