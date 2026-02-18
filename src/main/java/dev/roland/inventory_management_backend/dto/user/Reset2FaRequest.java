package dev.roland.inventory_management_backend.dto.user;

import lombok.*;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Builder
public class Reset2FaRequest {
    private final List<Long> ids;
}
