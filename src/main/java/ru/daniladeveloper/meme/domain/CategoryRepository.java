package ru.daniladeveloper.meme.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Collection<CategoryEntity> findAllByNameIgnoreCase(String search);
}
