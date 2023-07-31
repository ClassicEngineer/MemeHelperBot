package ru.daniladeveloper.meme.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.daniladeveloper.meme.domain.entity.CategoryEntity;
import ru.daniladeveloper.meme.domain.entity.MemeEntity;

import java.util.Collection;
import java.util.List;
import java.util.Set;


@Repository
public interface MemeRepository extends JpaRepository<MemeEntity, String> {
    List<MemeEntity> findByDescriptionContainsIgnoreCase(String description);

    List<MemeEntity> findAllByCategoriesIn(Collection<CategoryEntity> categories);

    @Query(value = "SELECT name FROM meme", nativeQuery = true)
    Set<String> findAllName();
}
