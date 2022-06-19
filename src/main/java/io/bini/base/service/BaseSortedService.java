package io.bini.base.service;

import io.bini.base.repository.BaseRepository;
import io.bini.base.dto.BaseSortedDTO;
import io.bini.base.model.BaseSortedEntity;
import io.bini.base.exception.ExistingRelationshipException;
import io.bini.base.mapper.GenericMapperService;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

abstract public class BaseSortedService<T extends BaseSortedEntity<PK, T>, DTO extends BaseSortedDTO<PK>, PK> extends BaseService<T,
    DTO, PK> {
    public BaseSortedService(BaseRepository<T, PK> repository, GenericMapperService mapperService) {
        super(repository, mapperService);
    }

    @Override
    public void delete(PK id) throws ExistingRelationshipException {
        if (id == null) {
            return;
        }
        T deletedEntity = this.repository.getReferenceById(id);
        List<T> remainingEntities = deletedEntity.getSiblings().stream()
            .filter(e -> e.getId() != id).collect(Collectors.toList());
        super.delete(id);
        resetEntitiesIndex(remainingEntities);
    }

    @Override
    public void delete(Iterable<PK> ids) throws ExistingRelationshipException {
        PK pk = ids.iterator().next();
        if (pk == null) {
            return;
        }
        T deletedEntity = this.repository.getReferenceById(pk);
        List<T> remainingEntities = deletedEntity.getSiblings().stream()
            .filter(e -> StreamSupport.stream(ids.spliterator(), false)
                .noneMatch(id -> id.equals(e.getId()))).collect(Collectors.toList());
        super.delete(ids);

        resetEntitiesIndex(remainingEntities);
    }

    public void resetEntitiesIndex(List<T> entities) {
        List<T> sortedEntities =
            entities.stream().sorted(Comparator.comparingLong(BaseSortedEntity::getIndex)).collect(Collectors.toList());
        Long i = 0L;
        for (T entity : sortedEntities) {
            entity.setIndex(i);
            i++;
        }
        this.repository.saveAll(sortedEntities);
    }

    public Collection<DTO> updateEntityIndex(PK id, Long newIndex) {
        T entity = this.repository.getReferenceById(id);
        Long initialIndex = entity.getIndex();
        if (newIndex >= entity.getSiblings().size()) {
            newIndex = (long) (entity.getSiblings().size() - 1);
        }
        if (newIndex < 0) {
            newIndex = 0L;
        }
        if (newIndex.equals(entity.getIndex())) {
            return entity.getSiblings().stream().map(this.mapper::toDto)
                .collect(Collectors.toList());
        }
        boolean itemMovedUp = newIndex < entity.getIndex();
        Long finalNewIndex = newIndex;
        List<T> sortedSiblings = entity.getSiblings().stream()
            .sorted(Comparator.comparingLong(i -> itemMovedUp ? i.getIndex() : -i.getIndex()))
            .collect(Collectors.toList());
        for (T sibling : sortedSiblings) {
            if ((sibling.getIndex() < finalNewIndex || sibling.getIndex() > initialIndex) && itemMovedUp) {
                continue;
            }
            if ((sibling.getIndex() > finalNewIndex || sibling.getIndex() < initialIndex) && !itemMovedUp) {
                continue;
            }
            if (sibling.getId() == id) {
                sibling.setIndex(finalNewIndex);
            } else {
                if (itemMovedUp) {
                    newIndex++;
                } else {
                    newIndex--;
                }
                sibling.setIndex(newIndex);
            }
        }
        this.repository.saveAll(sortedSiblings);
        return sortedSiblings.stream()
            .sorted(Comparator.comparingLong(BaseSortedEntity::getIndex))
            .map(this.mapper::toDto)
            .collect(Collectors.toList());
    }
}
