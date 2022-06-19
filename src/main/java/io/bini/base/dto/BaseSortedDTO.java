package io.bini.base.dto;


public interface BaseSortedDTO<PK> extends BaseDTO<PK> {
    Long getIndex();

    void setIndex(Long index);
}
