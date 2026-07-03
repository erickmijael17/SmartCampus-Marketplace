package com.upeu.media.repository;

import com.upeu.media.entity.MediaFile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    Optional<MediaFile> findByNombreAlmacen(String nombreAlmacen);
}
