package dev.roland.inventory_management_backend.dto.user;

import dev.roland.inventory_management_backend.model.User;
import dev.roland.inventory_management_backend.enums.UserRole;
import dev.roland.inventory_management_backend.enums.UserStatus;
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
        this.twoFaEnabled = user.is2faEnabled();
        this.otcSetupCompleted = user.isOtcSetupComplete();
        this.userStatus = user.getStatus();
    }

    private long id;
    private String username;
    private String email;
    private UserRole role;
    private boolean twoFaEnabled;
    private boolean otcSetupCompleted;
    private UserStatus userStatus;
}
