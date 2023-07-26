package ru.daniladeveloper.meme.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.daniladeveloper.meme.domain.PictureEntity;

@Repository
public interface PictureRepository extends JpaRepository<PictureEntity, Long> {

}
