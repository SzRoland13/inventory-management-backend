package dev.roland.inventory_management_backend.service;

import dev.roland.inventory_management_backend.model.OneTimeCode;
import dev.roland.inventory_management_backend.repository.OneTimeCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OneTImeCodeServiceImpl implements OneTimeCodeService {

    private final OneTimeCodeRepository oneTImeCodeRepository;

    @Override
    public OneTimeCode save(OneTimeCode oneTimeCode) {
        return oneTImeCodeRepository.save(oneTimeCode);
    }
}
