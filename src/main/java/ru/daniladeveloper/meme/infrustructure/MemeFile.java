package ru.daniladeveloper.meme.infrustructure;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;


public abstract class MemeFile {

    @Getter
    private final InputFile inputFile;

    public MemeType type;

    public MemeFile(File file) {
        this.inputFile = new InputFile(file);
        this.type = MemeType.PICTURE;
    }

    public static MemeFile build(File file, String path) {
            if (path.contains(".gif")) {
                return new GifMemeFile(file);
            } else {
                return new PictureMemeFile(file);
            }
            //TODO: implement normally types
        }

}

