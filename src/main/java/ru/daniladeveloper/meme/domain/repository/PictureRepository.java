package ru.daniladeveloper.meme.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.daniladeveloper.meme.domain.entity.PictureEntity;

@Repository
public interface PictureRepository extends JpaRepository<PictureEntity, Long> {

}
