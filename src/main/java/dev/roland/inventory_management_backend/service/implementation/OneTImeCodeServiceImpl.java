package dev.roland.inventory_management_backend.service.implementation;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.message_key.MessageKey;
import dev.roland.inventory_management_backend.message_key.NotFoundMessageKey;
import dev.roland.inventory_management_backend.model.OneTimeCode;
import dev.roland.inventory_management_backend.repository.OneTimeCodeRepository;
import dev.roland.inventory_management_backend.service.OneTimeCodeService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OneTImeCodeServiceImpl implements OneTimeCodeService {

  private final OneTimeCodeRepository oneTimeCodeRepository;

  @Override
  public JpaRepository<OneTimeCode, Long> getRepository() {
    return oneTimeCodeRepository;
  }

  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.ONE_TIME_CODE;
  }

  /**
   * Retrieves a {@link OneTimeCode} entity from the database that matches the given code value.
   *
   * @param code the one time code string to look up
   * @return an {@link Optional} containing the found {@link OneTimeCode}, or an empty {@link
   *     Optional} if no matching token exists
   */
  @Override
  public Optional<OneTimeCode> findByCode(String code) {
    return oneTimeCodeRepository.findByCode(code);
  }

  /**
   * Retrieves a {@link OneTimeCode} entity from the database that matches the given user id.
   *
   * @param userId the one time code string to look up
   * @return an {@link Optional} containing the found {@link OneTimeCode}, or an empty {@link
   *     Optional} if no matching token exists
   */
  @Override
  public Optional<OneTimeCode> findByUserId(Long userId) {
    return oneTimeCodeRepository.findByUserId(userId);
  }
}
