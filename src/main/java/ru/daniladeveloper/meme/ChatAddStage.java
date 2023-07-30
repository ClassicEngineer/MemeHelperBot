package ru.daniladeveloper.meme;

import lombok.Getter;
import lombok.Setter;
import ru.daniladeveloper.meme.infrustructure.Stage;

@Getter
@Setter
public class ChatAddStage {

    private Stage stage;

    private String name;

    private String description;

    private Long pictureId;

    private String category;

    public ChatAddStage(Stage stage) {
        this.stage = stage;
    }


}
