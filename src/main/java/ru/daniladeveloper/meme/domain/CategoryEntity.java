package ru.daniladeveloper.meme.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "category")
public class CategoryEntity {

    @Id
    private Long id;


    private String name;

}
