package ru.daniladeveloper.meme.domain;

import lombok.Builder;
import lombok.Data;
import ru.daniladeveloper.meme.domain.entity.CategoryEntity;

@Data
@Builder
public class Category {

    private Long id;
    private String name;

    public static Category from(CategoryEntity categoryEntity) {
        return Category.builder()
            .id(categoryEntity.getId())
            .name(categoryEntity.getName())
            .build();
    }
}
