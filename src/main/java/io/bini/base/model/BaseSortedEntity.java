package io.bini.base.model;

import java.util.Collection;

public interface BaseSortedEntity<PK, T extends BaseSortedEntity<?,?>> extends BaseEntity<PK> {
    Long getIndex();

    void setIndex(Long index);

    Collection<T> getSiblings();
}
