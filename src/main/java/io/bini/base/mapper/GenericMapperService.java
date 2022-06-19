package io.bini.base.mapper;

import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class used to access GenericMappers.
 *
 * @author sinan.yumak
 */
@Component
public class GenericMapperService {

    /**
     * Holds GenericMapper informations with source and target types as keys.
     */
    private final Map<String, GenericMapper> mapperInfos = new HashMap<>();

    private final ApplicationContext appContext;

    public GenericMapperService(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    @PostConstruct
    private void init() {
        Collection<GenericMapper> foundMappers = findGenericMappers();
        for (GenericMapper foundMapper : foundMappers) {
            // source type of the mapper
            String sourceType = getMapperSourceType(foundMapper);
            // target type of the mapper
            String targetType = getMapperTargetType(foundMapper);
            // initialize mapper infos
            mapperInfos.put(sourceType + "-" + targetType, foundMapper);
            mapperInfos.put(targetType + "-" + sourceType, foundMapper);
        }
    }

    /**
     * Searches for the mappers in Spring context that is inherited from
     * GenericMapper interface.
     */
    private Collection<GenericMapper> findGenericMappers() {
        return appContext.getBeansOfType(GenericMapper.class).values();
    }

    /**
     * Returns the source type of GenericMapper.
     */
    private String getMapperSourceType(GenericMapper mapper) {
        Class<?>[] mapperTypeInfos =
            GenericTypeResolver.resolveTypeArguments(
                mapper.getClass(),
                GenericMapper.class
            );
        return mapperTypeInfos[0].getSimpleName();
    }

    /**
     * Returns the target type of GenericMapper.
     */
    private String getMapperTargetType(GenericMapper mapper) {
        Class<?>[] mapperTypeInfos =
            GenericTypeResolver.resolveTypeArguments(
                mapper.getClass(),
                GenericMapper.class
            );
        return mapperTypeInfos[1].getSimpleName();
    }

    /**
     * Returns the GenericMapper for the given source and the target type.
     */
    public <S, T> GenericMapper<S, T> getMapper(Class<S> sourceType, Class<T> targetType) {
        String mapperKey = sourceType.getSimpleName() + "-" + targetType.getSimpleName();
        return mapperInfos.get(mapperKey);
    }
}
