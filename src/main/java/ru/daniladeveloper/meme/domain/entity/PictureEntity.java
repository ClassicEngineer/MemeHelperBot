package ru.daniladeveloper.meme.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "picture")
public class PictureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "picture_id_seq")
    @SequenceGenerator(name = "picture_id_seq", sequenceName = "picture_id_seq", allocationSize = 1)
    public Long id;

    @Basic
    @Column(name = "path")
    private String path;
}
