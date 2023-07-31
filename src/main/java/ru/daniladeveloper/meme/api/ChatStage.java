package ru.daniladeveloper.meme.api;

import lombok.Getter;
import lombok.Setter;
import ru.daniladeveloper.meme.application.Stage;

@Getter
public class ChatStage {

    private Stage stage;

    private AddParameters addParameters;

    private SearchParameters searchParameters;

    public ChatStage(Stage stage) {
        this.stage = stage;
        this.addParameters = new AddParameters();
        this.searchParameters = new SearchParameters();
    }


    public void setStage(Stage stage) {
        this.stage = stage;
        if (stage.equals(Stage.START)) {
            this.addParameters = new AddParameters();
            this.searchParameters = new SearchParameters();
        }
    }
}
