package dev.roland.inventory_management_backend.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import dev.roland.inventory_management_backend.common.exception.NotFoundException;
import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.user.User;

@ExtendWith(MockitoExtension.class)
class BaseServiceTest {

  @Mock private JpaRepository<User, Long> repository;

  private BaseService<User, Long> service;

  @BeforeEach
  void setUp() {
    service =
        new BaseService<>() {
          @Override
          public JpaRepository<User, Long> getRepository() {
            return repository;
          }

          @Override
          public MessageKey getNotFoundMessageKey() {
            return NotFoundMessageKey.USER;
          }
        };
  }

  @Test
  void delegatesSaveAndFindOperations() {
    final User user = User.builder().id(1L).build();
    when(repository.save(user)).thenReturn(user);
    when(repository.findById(1L)).thenReturn(Optional.of(user));
    when(repository.findAll()).thenReturn(List.of(user));

    assertSame(user, service.save(user));
    assertSame(user, service.findByIdOrThrow(1L));
    assertEquals(List.of(user), service.findAll());
  }

  @Test
  void findByIdOrThrowReportsMissingEntity() {
    when(repository.findById(2L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.findByIdOrThrow(2L));
  }

  @Test
  void deleteByIdDeletesExistingEntityAndRejectsMissingEntity() {
    final User user = User.builder().id(3L).build();
    when(repository.findById(3L)).thenReturn(Optional.of(user));
    when(repository.findById(4L)).thenReturn(Optional.empty());

    service.deleteById(3L);
    assertThrows(NotFoundException.class, () -> service.deleteById(4L));

    verify(repository).delete(user);
  }

  @Test
  void deleteChecksExistenceBeforeDeleting() {
    final User user = User.builder().id(5L).build();
    when(repository.existsById(5L)).thenReturn(true);
    when(repository.existsById(6L)).thenReturn(false);

    service.delete(user);
    assertThrows(NotFoundException.class, () -> service.delete(User.builder().id(6L).build()));

    verify(repository).delete(user);
    verify(repository, never()).delete(User.builder().id(6L).build());
  }

  @Test
  void updateMutatesAndSavesExistingEntity() {
    final User user = User.builder().id(7L).username("before").build();
    when(repository.findById(7L)).thenReturn(Optional.of(user));
    when(repository.save(user)).thenReturn(user);

    final User updated = service.update(7L, entity -> entity.setUsername("after"));

    assertSame(user, updated);
    assertEquals("after", updated.getUsername());
    verify(repository).save(user);
  }
}
