package ru.daniladeveloper.meme.api;

import lombok.Data;

@Data
public class AddParameters {
    private String name;

    private String description;

    private Long pictureId;

    private String category;
}
