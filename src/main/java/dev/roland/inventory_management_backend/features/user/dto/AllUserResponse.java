package dev.roland.inventory_management_backend.features.user.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Wraps the collection of user records returned by the user-listing endpoint. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllUserResponse {
  private List<UserDtoWithAvatar> users;
}
