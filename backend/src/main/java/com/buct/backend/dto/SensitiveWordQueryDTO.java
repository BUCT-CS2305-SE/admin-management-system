package com.buct.backend.dto;

public class SensitiveWordQueryDTO {
    private Integer pageNum;
    private Integer pageSize;
    private String word;
    private Integer status;

    // 手动写 getter 方法
    public Integer getPageNum() {
        return pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public String getWord() {
        return word;
    }

    public Integer getStatus() {
        return status;
    }

    // 有需要的话，也可以加上 setter 方法
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}