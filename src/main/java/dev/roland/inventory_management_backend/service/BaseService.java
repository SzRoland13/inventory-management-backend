package dev.roland.inventory_management_backend.service;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.exception.NotFoundException;
import dev.roland.inventory_management_backend.message_key.MessageKey;
import dev.roland.inventory_management_backend.model.interfaces.IdInterface;

public interface BaseService<T extends IdInterface<ID>, ID extends Serializable> {

  JpaRepository<T, ID> getRepository();

  MessageKey getNotFoundMessageKey();

  default T save(T entity) {
    return getRepository().save(entity);
  }

  default T findByIdOrThrow(ID id) {
    return getRepository()
        .findById(id)
        .orElseThrow(() -> new NotFoundException(getNotFoundMessageKey()));
  }

  default void deleteById(ID id) {
    getRepository().delete(findByIdOrThrow(id));
  }

  default boolean existsById(ID id) {
    return getRepository().existsById(id);
  }

  default void delete(T entity) {
    if (!existsById(entity.getId())) {
      throw new NotFoundException(getNotFoundMessageKey());
    }

    getRepository().delete(entity);
  }

  default List<T> findAll() {
    return getRepository().findAll();
  }

  default T update(ID id, Consumer<T> updater) {
    T entity = findByIdOrThrow(id);
    updater.accept(entity);
    return save(entity);
  }
}
