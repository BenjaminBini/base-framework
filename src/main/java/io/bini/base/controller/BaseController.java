package io.bini.base.controller;

import io.bini.base.dto.APIError;
import io.bini.base.dto.BaseDTO;
import io.bini.base.model.BaseEntity;
import io.bini.base.exception.ExistingRelationshipException;
import io.bini.base.service.BaseService;
import io.bini.base.specifiction.GenericSpecificationBuilder;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.*;

@Data
public abstract class BaseController<T extends BaseEntity<PK>, DTO extends BaseDTO<PK>, PK> {

    protected final BaseService<T, DTO, PK> service;

    public BaseController(BaseService<T, DTO, PK> service) {
        this.service = service;
    }

    @GetMapping
    protected Collection<DTO> list(@RequestParam Map<String, String> searchParams) {
        GenericSpecificationBuilder<T> builder = new GenericSpecificationBuilder<>();
        searchParams.forEach((key, value) -> builder.with(key, ":", value));
        return service.list(builder.build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DTO> get(@PathVariable("id") PK id) {
        return this.service.get(id)
            .map(t -> ResponseEntity.ok().body(t))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DTO> save(@RequestBody DTO dto) {
        return ResponseEntity.ok(this.service.save(dto));
    }

    @PutMapping
    public ResponseEntity<DTO> update(@RequestBody DTO dto) {
        Optional<DTO> existingEntity = this.service.get(dto.getId());
        if (existingEntity.isEmpty()) {
            return ResponseEntity.status(500).build();
        }
        copyNonNullProperties(dto, existingEntity.get());
        return ResponseEntity.ok(this.service.save(existingEntity.get()));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<DTO> delete(@PathVariable("id") PK id) throws ExistingRelationshipException {
        try {
            this.service.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<DTO> delete(@RequestBody Iterable<PK> ids) throws ExistingRelationshipException {
        try {
            this.service.delete(ids);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    private void copyNonNullProperties(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
        removeEmptyForeignKeyObjects(target);
    }

    private void removeEmptyForeignKeyObjects(Object object) {
        final BeanWrapper targetEntity = new BeanWrapperImpl(object);
        java.beans.PropertyDescriptor[] pds = targetEntity.getPropertyDescriptors();
        for (java.beans.PropertyDescriptor pd : pds) {
            if (BaseDTO.class.isAssignableFrom(pd.getPropertyType())) {
                BaseDTO<?> entity = (BaseDTO<?>) targetEntity.getPropertyValue(pd.getName());
                if (entity != null && entity.getId() == null) {
                    targetEntity.setPropertyValue(pd.getName(), null);
                }
            }
        }
    }

    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }


    @ExceptionHandler({ExistingRelationshipException.class})
    public ResponseEntity<Object> handleRelationshipException(
        ExistingRelationshipException ex, WebRequest request) {
        APIError apiError =
            new APIError(HttpStatus.BAD_REQUEST, "existing-relationship", "existing-relationship");
        return new ResponseEntity<>(
            apiError, new HttpHeaders(), apiError.getStatus());
    }

    /*
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(
        ConstraintViolationException ex, WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getRootBeanClass().getName() + " " +
                violation.getPropertyPath() + ": " + violation.getMessage());
        }

        ApiError apiError =
            new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
        return new ResponseEntity<>(
            apiError, new HttpHeaders(), apiError.getStatus());
    }

     */
}
