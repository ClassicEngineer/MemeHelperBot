package ru.daniladeveloper.meme.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public interface MemeRepository extends JpaRepository<MemeEntity, String> {
    List<MemeEntity> findByDescriptionContainsIgnoreCase(String description);

    List<MemeEntity> findAllByCategoriesIn(Collection<CategoryEntity> categories);
}
