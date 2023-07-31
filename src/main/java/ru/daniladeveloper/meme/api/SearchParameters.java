package ru.daniladeveloper.meme.api;

import lombok.Data;
import ru.daniladeveloper.meme.infrustructure.FindMemeResult;

@Data
public class SearchParameters {

    private FindMemeResult findResult;

}
