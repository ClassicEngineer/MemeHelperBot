package ru.daniladeveloper.meme.infrustructure;

import lombok.Getter;
import ru.daniladeveloper.meme.domain.Meme;
import ru.daniladeveloper.meme.domain.entity.MemeEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Getter
public class FindMemeResult {

    private MemeFile result;

    private Map<Integer, Meme> orderToMemes;

    private final boolean isMultiple;

    public FindMemeResult(MemeFile result) {
        this.result = result;
        this.isMultiple = false;
    }

    public FindMemeResult(List<MemeEntity> memes) {
        int i = 0 ;
        this.orderToMemes = new HashMap<>();
        for (MemeEntity meme : memes) {
            orderToMemes.put(i, new Meme(meme.getName()));
            i++;
        }
        this.isMultiple = true;
    }

    public String toMultipleChoice() {
        StringBuilder stringBuilder = new StringBuilder();
        for (var entry : orderToMemes.entrySet()) {
            stringBuilder.append(entry.getKey())
                .append(". ")
                .append(entry.getValue().getName())
                .append("\n");
        }
        return  stringBuilder.toString();
    }

    public Optional<Meme> getMemeByNumber(String number) {
        try {
            int i = Integer.parseInt(number);
            return Optional.ofNullable(orderToMemes.get(i));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
