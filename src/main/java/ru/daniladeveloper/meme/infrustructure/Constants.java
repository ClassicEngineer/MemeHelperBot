package ru.daniladeveloper.meme.infrustructure;

import java.util.List;


public final class Constants {

    public static final String FIND = "find";
    public static final String ADD = "add";

    public static final String MENU = "menu";

    public static final String CANCEL = "cancel";

    public static final String MEMES_DIR = "/tmp/memes/";

    public static final String NOT_FOUND_IMAGE = MEMES_DIR + "404.png";



    public static final List<String> COMMANDS = List.of(ADD, FIND);
    public static final Integer CATEGORIES_LIMIT = 10;
}
