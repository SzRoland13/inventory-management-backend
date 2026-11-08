package dev.roland.inventory_management_backend.features.one_time_code.service.impl;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import dev.roland.inventory_management_backend.common.message.MessageKey;
import dev.roland.inventory_management_backend.common.message.NotFoundMessageKey;
import dev.roland.inventory_management_backend.features.one_time_code.OneTimeCode;
import dev.roland.inventory_management_backend.features.one_time_code.repository.OneTimeCodeRepository;
import dev.roland.inventory_management_backend.features.one_time_code.service.OneTimeCodeService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;

/** Implements the one time code service operations. */
@Service
@RequiredArgsConstructor
public class OneTimeCodeServiceImpl implements OneTimeCodeService {

  private final OneTimeCodeRepository oneTimeCodeRepository;

  /** {@inheritDoc} */
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP",
      justification =
          "BaseService CRUD methods use this accessor to reach the Spring Data repository.")
  @Override
  public JpaRepository<OneTimeCode, Long> getRepository() {
    return oneTimeCodeRepository;
  }

  /** {@inheritDoc} */
  @Override
  public MessageKey getNotFoundMessageKey() {
    return NotFoundMessageKey.ONE_TIME_CODE;
  }

  /** {@inheritDoc} */
  @Override
  public Optional<OneTimeCode> findByCodeHashForUpdate(final String codeHash) {
    return oneTimeCodeRepository.findByCodeHashForUpdate(codeHash);
  }

  /**
   * Retrieves a {@link OneTimeCode} entity from the database that matches the given user id.
   *
   * @param userId the one time code string to look up
   * @return an {@link Optional} containing the found {@link OneTimeCode}, or an empty {@link
   *     Optional} if no matching token exists
   */
  @Override
  public Optional<OneTimeCode> findByUserId(final Long userId) {
    return oneTimeCodeRepository.findByUserId(userId);
  }

  /** {@inheritDoc} */
  @Override
  public void deleteByUserId(final Long userId) {
    oneTimeCodeRepository.deleteByUserId(userId);
  }
}
