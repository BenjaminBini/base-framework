package io.bini.base.repository;

import io.bini.base.model.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity<PK>, PK> extends JpaRepository<T, PK>,
    JpaSpecificationExecutor<T> {
    void refresh(T entity);
}
