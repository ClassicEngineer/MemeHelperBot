package ru.daniladeveloper.meme.infrustructure;


import java.io.File;

public class GifMemeFile extends MemeFile {

    public GifMemeFile(File file) {
        super(file);
        this.type = MemeType.GIF;
    }
}
