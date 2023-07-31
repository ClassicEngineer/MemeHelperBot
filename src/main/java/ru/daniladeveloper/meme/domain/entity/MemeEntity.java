package ru.daniladeveloper.meme.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "meme")
public class MemeEntity {

    @Id
    private String name;


    @Column(name = "description")
    private String description;

    @OneToOne
    @JoinColumn(name = "picture_id", referencedColumnName = "id")
    private PictureEntity picture;

    @OneToMany
    @JoinTable(name = "meme_categories", joinColumns = { @JoinColumn(name = "meme_name") },
        inverseJoinColumns = { @JoinColumn(name = "category_id") })
    private List<CategoryEntity> categories;
}
