package com.ossanasur.cbconnect.historique;

import com.ossanasur.cbconnect.common.entity.InternalHistorique;
import com.ossanasur.cbconnect.exception.NoChangesDetectedException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.entity.Profil;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service de versioning generique, inspire d'OssanAuth.
 * Utilise la reflexion pour la detection des changements.
 *
 * @param <E> Entite metier (extends InternalHistorique)
 * @param <R> DTO Request
 */
public abstract class AbstractVersioningService<E extends InternalHistorique, R> {

    protected abstract JpaRepository<E, Integer> getRepository();

    protected abstract E findActiveByTrackingId(UUID trackingId);

    protected abstract E mapToEntity(R request, E existing);

    protected abstract UUID getTrackingId(E entity);

    protected abstract void setTrackingId(E entity, UUID newId);

    @Transactional
    public E createVersion(UUID trackingId, R request, String loginAuteur) {
        E current = getActiveVersion(trackingId);
        E proposed = mapToEntity(request, current);
        if (!hasChanges(current, proposed)) {
            throw new NoChangesDetectedException("Aucune modification detectee");
        }
        current.setActiveData(false);
        current.setUpdatedAt(LocalDateTime.now());
        current.setUpdatedBy(loginAuteur);
        getRepository().save(current);

        E newVersion = cloneEntity(proposed);
        newVersion.setHistoriqueId(null);
        newVersion.setParentCodeId(getTrackingId(current).toString());
        setTrackingId(newVersion, UUID.randomUUID());
        newVersion.setCreatedAt(LocalDateTime.now());
        newVersion.setCreatedBy(loginAuteur);
        newVersion.setActiveData(true);
        newVersion.setDeletedData(false);
        newVersion.setUpdatedAt(null);
        newVersion.setUpdatedBy(null);
        return getRepository().save(newVersion);
    }

    @Transactional
    public void softDelete(UUID trackingId, String loginAuteur) {
        E entity = getActiveVersion(trackingId);
        entity.setActiveData(false);
        entity.setDeletedData(true);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setDeletedBy(loginAuteur);
        getRepository().save(entity);
    }

    public E getActiveVersion(UUID trackingId) {
        E entity = findActiveByTrackingId(trackingId);
        if (entity == null || !entity.isActiveData()) {
            throw new RessourceNotFoundException("Entite non trouvee ou inactive : " + trackingId);
        }
        return entity;
    }

    protected boolean hasChanges(E current, E proposed) {
        Set<String> ignored = Set.of(
                "historiqueId", "createdAt", "updatedAt", "deletedAt",
                "createdBy", "updatedBy", "deletedBy", "activeData",
                "deletedData", "parentCodeId", "fromTable", "excel", "libelle");
        for (Field field : getAllFields(current.getClass())) {
            field.setAccessible(true);
            if (ignored.contains(field.getName()))
                continue;
            try {
                Object cv = field.get(current), pv = field.get(proposed);
                if (cv instanceof InternalHistorique && pv instanceof InternalHistorique) {
                    if (!Objects.equals(((InternalHistorique) cv).getParentCodeId(),
                            ((InternalHistorique) pv).getParentCodeId()))
                        return true;
                } else if (cv instanceof Collection && pv instanceof Collection) {
                    if (!collectionsEqual((Collection<?>) cv, (Collection<?>) pv))
                        return true;
                } else if (!Objects.equals(cv, pv))
                    return true;
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Erreur detection changements", e);
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    protected E cloneEntity(E source) {
        try {
            E clone = (E) source.getClass().getDeclaredConstructor().newInstance();
            for (Field field : getAllFields(source.getClass())) {
                field.setAccessible(true);
                field.set(clone, field.get(source));
            }
            return clone;
        } catch (Exception e) {
            throw new RuntimeException("Erreur de clonage", e);
        }
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private boolean collectionsEqual(Collection<?> c1, Collection<?> c2) {
        if (c1.size() != c2.size())
            return false;
        return new HashSet<>(c1).containsAll(c2);
    }
}
