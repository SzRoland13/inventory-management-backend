package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.model.OneTimeCode;
import dev.roland.inventory_management_backend.repository.OneTimeCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OneTImeCodeServiceImpl implements OneTimeCodeService {

    private final OneTimeCodeRepository oneTimeCodeRepository;

    /**
     * Persists a new {@link OneTimeCode} entity or updates an existing one in the database.
     *
     * @param oneTimeCode the {@link OneTimeCode} entity to save or update
     * @return the saved or updated {@link OneTimeCode} entity
     */
    @Override
    public OneTimeCode save(OneTimeCode oneTimeCode) {
        return oneTimeCodeRepository.save(oneTimeCode);
    }

    /**
     * Retrieves a {@link OneTimeCode} entity from the database that matches the given code value.
     *
     * @param code the one time code string to look up
     * @return an {@link Optional} containing the found {@link OneTimeCode},
     * or an empty {@link Optional} if no matching token exists
     */
    @Override
    public Optional<OneTimeCode> findByCode(String code) {
        return oneTimeCodeRepository.findByCode(code);
    }

    /**
     * Deletes the specified {@link OneTimeCode} entity from the database.
     *
     * @param oneTimeCode the {@link OneTimeCode} entity to delete
     */
    @Override
    public void delete(OneTimeCode oneTimeCode) {
        oneTimeCodeRepository.delete(oneTimeCode);
    }
}
