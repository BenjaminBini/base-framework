package io.bini.base.service;

import io.bini.base.model.BaseEntity;
import io.bini.base.repository.BaseRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

public class BaseRepositoryImpl<T extends BaseEntity<PK>, PK> extends SimpleJpaRepository<T, PK> implements BaseRepository<T, PK> {
    private final EntityManager entityManager;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation,
                                  EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Transactional
    public void refresh(T entity) {
        entityManager.refresh(entity);
    }
}
