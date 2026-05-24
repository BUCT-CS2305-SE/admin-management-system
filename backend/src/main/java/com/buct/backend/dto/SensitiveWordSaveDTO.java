package com.buct.backend.dto;

public class SensitiveWordSaveDTO {
    private String word;
    private Integer status;

    // 必须的 getter 方法
    public String getWord() {
        return word;
    }

    public Integer getStatus() {
        return status;
    }

    // 可选的 setter 方法（后面修改时会用到）
    public void setWord(String word) {
        this.word = word;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}