package ru.daniladeveloper.meme.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.daniladeveloper.meme.domain.entity.CategoryEntity;

import java.util.Collection;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Collection<CategoryEntity> findAllByNameIgnoreCase(String search);

    @Query(value = "SELECT * FROM category WHERE id IN (SELECT category_id FROM meme_categories GROUP BY category_id ORDER BY count(category_id)  LIMIT :limit );", nativeQuery = true)
    Collection<CategoryEntity> findTopCategories(Integer limit);

    Optional<CategoryEntity> findByNameIgnoreCase(String category);

}
