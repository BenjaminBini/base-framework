package io.bini.base.mapper;

import org.mapstruct.Context;

import java.util.List;

/**
 * Generic mapper interface used to define generic mapping operations.
 *
 * @author sinan.yumak
 */
public interface GenericMapper<S, T> {

    @DoIgnore
    default T toDto(S source) {
        return this.toDto(source, new CycleAvoidingMappingContext());
    }

    @DoIgnore
    default S toModel(T target) {
        return this.toModel(target, new CycleAvoidingMappingContext());
    }

    /**
     * Converts given model to dto.
     */
    T toDto(S source, @Context CycleAvoidingMappingContext context);

    /**
     * Converts given dto to model.
     */
    S toModel(T target, @Context CycleAvoidingMappingContext context);


    /**
     * Converts given model list to dto list.
     */
    List<T> toDto(List<S> sourceList);

    /**
     * Converts given dto list to model list.
     */
    List<S> toModel(List<T> targetList);


}
