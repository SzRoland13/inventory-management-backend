package dev.roland.inventory_management_backend.features.document_prefix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import dev.roland.inventory_management_backend.features.document_prefix.dto.DocumentPrefixesResponse;
import dev.roland.inventory_management_backend.features.document_prefix.dto.DocumentPrefixesUpdateRequest;
import dev.roland.inventory_management_backend.features.document_prefix.service.DocumentPrefixService;
import dev.roland.inventory_management_backend.features.user.enumeration.UserStatus;

@SpringBootTest
@ActiveProfiles("test")
class DocumentPrefixControllerAuthorizationTest {

  @Autowired private DocumentPrefixController controller;

  @MockitoBean private DocumentPrefixService documentPrefixService;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void adminCanUpdateDocumentPrefixes() {
    final DocumentPrefixesUpdateRequest request = DocumentPrefixesUpdateRequest.builder().build();
    final DocumentPrefixesResponse response = DocumentPrefixesResponse.builder().build();
    authenticateAs("ROLE_ADMIN");
    when(documentPrefixService.updatePrefixes(null)).thenReturn(response);

    final var result = controller.update(request);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    verify(documentPrefixService).updatePrefixes(null);
  }

  @Test
  void nonAdminCannotUpdateDocumentPrefixes() {
    final DocumentPrefixesUpdateRequest request = DocumentPrefixesUpdateRequest.builder().build();
    authenticateAs("ROLE_SALES");

    assertThrows(AccessDeniedException.class, () -> controller.update(request));

    verify(documentPrefixService, never()).updatePrefixes(null);
  }

  private void authenticateAs(final String roleAuthority) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(
                "active-user",
                "",
                List.of(
                    UserStatus.ACTIVE.toGrantedAuthority(),
                    new SimpleGrantedAuthority(roleAuthority))));
  }
}
