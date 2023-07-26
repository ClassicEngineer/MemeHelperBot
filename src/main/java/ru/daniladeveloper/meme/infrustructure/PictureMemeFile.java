package ru.daniladeveloper.meme.infrustructure;


import java.io.File;

public class PictureMemeFile extends MemeFile {


    public PictureMemeFile(File file) {
        super(file);
        this.type = MemeType.PICTURE;
    }


}
