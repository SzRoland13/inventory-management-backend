package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.exception.ApiException;
import dev.roland.inventory_management_backend.messageKey.MessageKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.List;

public interface BaseService<T, ID extends Serializable> {

    JpaRepository<T, ID> getRepository();

    MessageKey getNotFoundMessageKey();

    default T save(T entity) {
        return getRepository().save(entity);
    }

    default T findById(ID id) {
        return getRepository()
                .findById(id)
                .orElseThrow(() ->
                        new ApiException(getNotFoundMessageKey())
                );
    }

    default void deleteById(ID id) {
        if (!getRepository().existsById(id)) {
            throw new ApiException(getNotFoundMessageKey());
        }
        getRepository().deleteById(id);
    }

    default List<T> findAll() {
        return getRepository().findAll();
    }
}
