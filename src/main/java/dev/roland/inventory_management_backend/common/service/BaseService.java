package dev.roland.inventory_management_backend.common.service;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.persistance.IdInterface;

public interface BaseService<T extends IdInterface<ID>, ID extends Serializable> {

  /**
   * Returns the Spring Data repository backing this service.
   *
   * @return repository used for persistence operations
   */
  JpaRepository<T, ID> getRepository();

  /**
   * Returns the message key used when this service cannot find an entity.
   *
   * @return domain-specific not-found message key
   */
  MessageKey getNotFoundMessageKey();

  /**
   * Persists the supplied entity.
   *
   * @param entity entity to create or update
   * @return persisted entity returned by the repository
   */
  default T save(T entity) {
    return getRepository().save(entity);
  }

  /**
   * Finds an entity by id or throws a domain not-found exception.
   *
   * @param id entity identifier
   * @return matching entity
   * @throws NotFoundException when no entity exists for the id
   */
  default T findByIdOrThrow(ID id) {
    return getRepository()
        .findById(id)
        .orElseThrow(() -> new NotFoundException(getNotFoundMessageKey()));
  }

  /**
   * Deletes an entity by id after verifying that it exists.
   *
   * @param id identifier of the entity to delete
   * @throws NotFoundException when no entity exists for the id
   */
  default void deleteById(ID id) {
    getRepository().delete(findByIdOrThrow(id));
  }

  /**
   * Checks whether an entity exists for the supplied id.
   *
   * @param id entity identifier
   * @return true when an entity exists, otherwise false
   */
  default boolean existsById(ID id) {
    return getRepository().existsById(id);
  }

  /**
   * Deletes the supplied entity after verifying that its id exists.
   *
   * @param entity entity to delete
   * @throws NotFoundException when no entity exists for the entity id
   */
  default void delete(T entity) {
    if (!existsById(entity.getId())) {
      throw new NotFoundException(getNotFoundMessageKey());
    }

    getRepository().delete(entity);
  }

  /**
   * Returns all entities managed by this service.
   *
   * @return list of all persisted entities
   */
  default List<T> findAll() {
    return getRepository().findAll();
  }

  /**
   * Finds an entity, applies the supplied mutation, and persists the result.
   *
   * @param id entity identifier
   * @param updater mutation to apply to the managed entity
   * @return persisted updated entity
   * @throws NotFoundException when no entity exists for the id
   */
  default T update(ID id, Consumer<T> updater) {
    T entity = findByIdOrThrow(id);
    updater.accept(entity);
    return save(entity);
  }
}
