package com.buct.backend.service;
import com.buct.backend.dto.SensitiveWordQueryDTO;
import com.buct.backend.dto.SensitiveWordSaveDTO;
import com.buct.backend.entity.SensitiveWord;
import com.buct.backend.common.PageResult;

public interface SensitiveWordService {
    PageResult<SensitiveWord> pageList(SensitiveWordQueryDTO dto);
    void save(SensitiveWordSaveDTO dto);
    void update(Long id, SensitiveWordSaveDTO dto);
    void delete(Long id);
}