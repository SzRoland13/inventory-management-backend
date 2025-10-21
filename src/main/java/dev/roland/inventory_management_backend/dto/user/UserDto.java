package dev.roland.inventory_management_backend.dto.user;

import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.model.enums.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.TwoFaEnabled = user.is2faEnabled();
        this.otcSetupCompleted = user.isOtcSetupComplete();
    }

    private long id;
    private String username;
    private String email;
    private UserRole role;
    private boolean TwoFaEnabled;
    private boolean otcSetupCompleted;
}
