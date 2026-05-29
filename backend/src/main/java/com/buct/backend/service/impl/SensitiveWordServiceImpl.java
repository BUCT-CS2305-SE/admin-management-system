package com.buct.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.buct.backend.dto.SensitiveWordQueryDTO;
import com.buct.backend.dto.SensitiveWordSaveDTO;
import com.buct.backend.entity.SensitiveWord;
import com.buct.backend.mapper.SensitiveWordMapper;
import com.buct.backend.common.BusinessException;
import com.buct.backend.common.PageResult;
import com.buct.backend.service.SensitiveWordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {
    private final SensitiveWordMapper mapper;

    // 手动添加构造器，替代 @RequiredArgsConstructor，解决所有报错
    public SensitiveWordServiceImpl(SensitiveWordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PageResult<SensitiveWord> pageList(SensitiveWordQueryDTO dto) {
        Page<SensitiveWord> page=new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<SensitiveWord> qw=new LambdaQueryWrapper<>();
        qw.like(dto.getWord() != null, SensitiveWord::getWord, dto.getWord())
                .eq(dto.getStatus() != null, SensitiveWord::getStatus, dto.getStatus())
                .orderByDesc(SensitiveWord::getCreateTime);
        Page<SensitiveWord> result=mapper.selectPage(page, qw);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public void save(SensitiveWordSaveDTO dto) {
        SensitiveWord word=new SensitiveWord();
        word.setWord(dto.getWord());
        word.setStatus(dto.getStatus());
        word.setCreateTime(LocalDateTime.now());
        mapper.insert(word);
    }

    @Override
    public void update(Long id, SensitiveWordSaveDTO dto) {
        SensitiveWord word=mapper.selectById(id);
        if (word == null) throw new BusinessException("敏感词不存在");
        word.setWord(dto.getWord());
        word.setStatus(dto.getStatus());
        mapper.updateById(word);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}