package dev.roland.inventory_management_backend.features.document_prefix;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import dev.roland.inventory_management_backend.common.dto.ApiResponse;
import dev.roland.inventory_management_backend.common.message.GenericMessageKey;
import dev.roland.inventory_management_backend.features.document_prefix.dto.DocumentPrefixesResponse;
import dev.roland.inventory_management_backend.features.document_prefix.dto.DocumentPrefixesUpdateRequest;
import dev.roland.inventory_management_backend.features.document_prefix.service.DocumentPrefixService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping(DocumentPrefixController.DOCUMENT_PREFIX_BASE_ENDPOINT)
@RequiredArgsConstructor
public class DocumentPrefixController {
  public static final String DOCUMENT_PREFIX_BASE_ENDPOINT = "api/v1/prefix";

  private final DocumentPrefixService documentPrefixService;

  @GetMapping
  public ResponseEntity<ApiResponse<DocumentPrefixesResponse>> getAll() {
    DocumentPrefixesResponse response = documentPrefixService.getAllPrefixes();

    return ResponseEntity.ok(ApiResponse.success(GenericMessageKey.REQUEST_SUCCESS, response));
  }

  @PostMapping()
  public ResponseEntity<ApiResponse<DocumentPrefixesResponse>> update(
      @RequestBody DocumentPrefixesUpdateRequest request) {
    DocumentPrefixesResponse response = documentPrefixService.updatePrefixes(request.getPrefixes());

    return ResponseEntity.ok(ApiResponse.success(GenericMessageKey.REQUEST_SUCCESS, response));
  }
}
