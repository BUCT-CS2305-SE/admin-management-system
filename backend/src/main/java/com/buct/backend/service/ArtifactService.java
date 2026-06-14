package com.buct.backend.service;

import com.buct.backend.common.PageResult;
import com.buct.backend.dto.ArtifactQueryDTO;
import com.buct.backend.dto.ArtifactSaveDTO;
import com.buct.backend.entity.Artifact;

import java.util.List;

public interface ArtifactService {

    PageResult<Artifact> pageArtifacts(ArtifactQueryDTO queryDTO);

    Artifact getArtifactById(String objectId);

    void addArtifact(ArtifactSaveDTO saveDTO);

    void updateArtifact(String objectId, ArtifactSaveDTO saveDTO);

    void deleteArtifact(String objectId);

    int importArtifacts(List<ArtifactSaveDTO> artifacts);

    String exportArtifactsCsv(ArtifactQueryDTO queryDTO);
}
