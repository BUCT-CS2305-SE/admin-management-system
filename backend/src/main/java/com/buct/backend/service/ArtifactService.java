package com.buct.backend.service;

import com.buct.backend.common.PageResult;
import com.buct.backend.dto.ArtifactQueryDTO;
import com.buct.backend.dto.ArtifactSaveDTO;
import com.buct.backend.entity.Artifact;

public interface ArtifactService {

    PageResult<Artifact> pageArtifacts(ArtifactQueryDTO queryDTO);

    Artifact getArtifactById(Long id);

    void addArtifact(ArtifactSaveDTO saveDTO);

    void updateArtifact(Long id, ArtifactSaveDTO saveDTO);

    void deleteArtifact(Long id);
}
