package io.bini.base.service;

import io.bini.base.dto.BaseDTO;
import io.bini.base.model.BaseEntity;
import io.bini.base.repository.BaseRepository;
import io.bini.base.exception.ExistingRelationshipException;
import io.bini.base.mapper.GenericMapper;
import io.bini.base.mapper.GenericMapperService;
import lombok.Data;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
abstract public class BaseService<T extends BaseEntity<PK>, DTO extends BaseDTO<PK>, PK> {
    protected final BaseRepository<T, PK> repository;
    protected final GenericMapperService mapperService;
    protected final GenericMapper<T, DTO> mapper;

    public BaseService(BaseRepository<T, PK> repository, GenericMapperService mapperService) {
        this.repository = repository;
        this.mapperService = mapperService;

        @SuppressWarnings("unchecked")
        Class<T> entityClass = ((Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0]);

        @SuppressWarnings("unchecked")
        Class<DTO> dtoClass = ((Class<DTO>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[1]);

        this.mapper = mapperService.getMapper(entityClass, dtoClass);
    }

    public Optional<DTO> get(PK id) {
        Optional<T> entity = this.repository.findById(id);
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        return entity.map(this.mapper::toDto);
    }

    public Collection<DTO> list() {
        return this.repository.findAll()
            .stream().map(this.mapper::toDto)
            .collect(Collectors.toList());
    }

    public Collection<DTO> list(Specification<T> spec) {
        return this.repository.findAll(spec)
            .stream().map(this.mapper::toDto)
            .collect(Collectors.toList());
    }

    public DTO save(DTO dto) {
        T entity = this.mapper.toModel(dto);
        T savedEntity = this.repository.save(entity);
        this.repository.refresh(savedEntity);
        return this.mapper.toDto(savedEntity);
    }

    public void delete(PK id) throws ExistingRelationshipException {
        try {
            this.repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ExistingRelationshipException();
        }
    }

    public void delete(Iterable<PK> ids) throws ExistingRelationshipException {
        try {
            ids.forEach(this.repository::deleteById);
        } catch (DataIntegrityViolationException e) {
            throw new ExistingRelationshipException();
        }
    }
}
