package ru.daniladeveloper.meme;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import ru.daniladeveloper.meme.domain.*;
import ru.daniladeveloper.meme.infrustructure.Constants;
import ru.daniladeveloper.meme.infrustructure.MemeFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static ru.daniladeveloper.meme.infrustructure.Constants.*;

@Component
@RequiredArgsConstructor
public class MemeStorage {




    private final MemeRepository memeRepository;
    private final PictureRepository pictureRepository;
    private final CategoryRepository categoryRepository;


//    @PostConstruct
//    public void post() {
//        String memeId = "ThumbUp";
//        String picturePath = "finger_up.gif";
//        PictureEntity picture = new PictureEntity(1L, picturePath);
//        CategoryEntity films = new CategoryEntity(1L, "фильмы");
//        CategoryEntity old = new CategoryEntity(2L, "олд");
//
//        MemeEntity meme = new MemeEntity(memeId, "Парень поднимает палец вверх сидя за компьютером", picture, List.of(films, old));
//
//        pictureRepository.save(picture);
//        categoryRepository.saveAll(List.of(films, old));
//        memeRepository.save(meme);
//
//    }

    public MemeFile findByInput(String incomeText) {
        String[] request = incomeText.split(FIND);
        String search = request[1].trim();
        Optional<MemeEntity> meme = memeRepository.findById(search);
        String path = Constants.NOT_FOUND_IMAGE;
        try {
            if (meme.isPresent()) {
                path = meme.get().getPicture().getPath();
            } else {
                List<MemeEntity> memes = memeRepository.findByDescriptionContainsIgnoreCase(search);
                if (!memes.isEmpty()) {
                    path = memes.get(0).getPicture().getPath();
                }
            }
            File file = ResourceUtils.getFile(MEMES + path);
            return MemeFile.build(file, path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public Collection<MemeFile> findByCategory(String incomeText) {
        List<MemeFile> result = new ArrayList<>();
        String[] request = incomeText.split(CATEGORY);
        String search = request[1].trim();

        var categories = categoryRepository.findAllByNameIgnoreCase(search);

        var memes = memeRepository.findAllByCategoriesIn(categories);
        try {
            for (var mem : memes) {
                String path = mem.getPicture().getPath();
                File file = ResourceUtils.getFile(MEMES + path);
                result.add(MemeFile.build(file, path));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return result;
    }


}
