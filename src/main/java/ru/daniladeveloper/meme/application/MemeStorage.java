package ru.daniladeveloper.meme.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import ru.daniladeveloper.meme.api.AddParameters;
import ru.daniladeveloper.meme.domain.Category;
import ru.daniladeveloper.meme.domain.Meme;
import ru.daniladeveloper.meme.domain.entity.CategoryEntity;
import ru.daniladeveloper.meme.domain.entity.MemeEntity;
import ru.daniladeveloper.meme.domain.entity.PictureEntity;
import ru.daniladeveloper.meme.domain.repository.CategoryRepository;
import ru.daniladeveloper.meme.domain.repository.MemeRepository;
import ru.daniladeveloper.meme.domain.repository.PictureRepository;
import ru.daniladeveloper.meme.infrustructure.Constants;
import ru.daniladeveloper.meme.infrustructure.FindMemeResult;
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

    public FindMemeResult findByInput(String incomeText) {
        incomeText = incomeText.trim();
        Optional<MemeEntity> meme = memeRepository.findById(incomeText);
        String path = Constants.NOT_FOUND_IMAGE;
        try {
            if (meme.isPresent()) {
                path = meme.get().getPicture().getPath();
            } else {
                List<MemeEntity> memes = memeRepository.findByDescriptionContainsIgnoreCase(incomeText);
                if (memes.size() == 1) {
                    path = memes.get(0).getPicture().getPath();
                } else if (memes.size() > 1) {
                    return new FindMemeResult(memes);
                }
            }
            File file = ResourceUtils.getFile(path);
            return new FindMemeResult(MemeFile.build(file, path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public MemeFile findByName(Meme mem) {
        String path = NOT_FOUND_IMAGE;
        try {
            Optional<MemeEntity> meme = memeRepository.findById(mem.getName());
            if (meme.isPresent()) {
                path = meme.get().getPicture().getPath();
            }
            File file = ResourceUtils.getFile(path);
            return MemeFile.build(file, path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Category> findCategories(String request) {
        if (request.isEmpty()) {
           return categoryRepository.findTopCategories(CATEGORIES_LIMIT).stream()
               .map(Category::from).toList();
        }
        else {
            return categoryRepository.findAllByNameIgnoreCase(request).stream()
                .map(Category::from)
                .toList();
        }
    }


    public Collection<MemeFile> findByCategory(String incomeText) {
        List<MemeFile> result = new ArrayList<>();
        String search = incomeText.trim();

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

    public void addMemByStage(AddParameters parameters) {
        String memeId = parameters.getName();

        PictureEntity picture = pictureRepository.getReferenceById(parameters.getPictureId());
        Optional<CategoryEntity> optionalCategory = categoryRepository.findByNameIgnoreCase(parameters.getCategory());
        CategoryEntity category = optionalCategory.orElse(new CategoryEntity(null, parameters.getCategory()));
        category = categoryRepository.save(category);
        MemeEntity meme = new MemeEntity(memeId, parameters.getDescription(), picture, List.of(category));

        pictureRepository.save(picture);

        memeRepository.save(meme);
    }

    public boolean isNameUnique(String name) {
        return !memeRepository.findAllName().contains(name);
    }


}
