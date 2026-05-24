package com.buct.backend.controller;

import com.buct.backend.common.PageResult;
import com.buct.backend.common.Result;
import com.buct.backend.dto.ArtifactQueryDTO;
import com.buct.backend.dto.ArtifactSaveDTO;
import com.buct.backend.entity.Artifact;
import com.buct.backend.service.ArtifactService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/artifacts")
public class ArtifactController {

    private final ArtifactService artifactService;

    public ArtifactController(ArtifactService artifactService) {
        this.artifactService = artifactService;
    }

    @GetMapping("/page")
    public Result<PageResult<Artifact>> pageArtifacts(ArtifactQueryDTO queryDTO) {
        return Result.success(artifactService.pageArtifacts(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<Artifact> getArtifactById(@PathVariable Long id) {
        return Result.success(artifactService.getArtifactById(id));
    }

    @PostMapping
    public Result<Void> addArtifact(@Valid @RequestBody ArtifactSaveDTO saveDTO) {
        artifactService.addArtifact(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateArtifact(@PathVariable Long id,
                                       @Valid @RequestBody ArtifactSaveDTO saveDTO) {
        artifactService.updateArtifact(id, saveDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteArtifact(@PathVariable Long id) {
        artifactService.deleteArtifact(id);
        return Result.success();
    }
}
