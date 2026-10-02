package com.cafeteria.repository;

import com.cafeteria.entity.FoodItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItemEntity, Long> {

    List<FoodItemEntity> findByCategory(String category);
}
