package ru.daniladeveloper.meme;

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



    public MemeFile findByInput(String incomeText) {
        Optional<MemeEntity> meme = memeRepository.findById(incomeText);
        String path = Constants.NOT_FOUND_IMAGE;
        try {
            if (meme.isPresent()) {
                path = meme.get().getPicture().getPath();
            } else {
                List<MemeEntity> memes = memeRepository.findByDescriptionContainsIgnoreCase(incomeText);
                if (!memes.isEmpty()) {
                    path = memes.get(0).getPicture().getPath();
                }
            }
            File file = ResourceUtils.getFile(path);
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
                File file = ResourceUtils.getFile(MEMES_DIR + path);
                result.add(MemeFile.build(file, path));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return result;
    }


    public Long saveFilePath(String fullFileName) {
        PictureEntity saved = pictureRepository.save(new PictureEntity(0L, fullFileName));
        return saved.getId();
    }

    public void addMemByStage(ChatAddStage stage) {
        String memeId = stage.getName();

        PictureEntity picture = pictureRepository.getReferenceById(stage.getPictureId());
        CategoryEntity category = new CategoryEntity(null, stage.getCategory());
        category = categoryRepository.save(category);
        MemeEntity meme = new MemeEntity(memeId, stage.getDescription(), picture, List.of(category));

        pictureRepository.save(picture);

        memeRepository.save(meme);
    }
}
