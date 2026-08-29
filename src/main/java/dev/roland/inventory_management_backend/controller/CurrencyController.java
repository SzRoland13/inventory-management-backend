package dev.roland.inventory_management_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import dev.roland.inventory_management_backend.dto.ApiResponse;
import dev.roland.inventory_management_backend.dto.currency.CurrenciesResponse;
import dev.roland.inventory_management_backend.message_key.GenericMessageKey;
import dev.roland.inventory_management_backend.service.CurrencyService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping(CurrencyController.CURRENCY_BASE_ENDPOINT)
@RequiredArgsConstructor
public class CurrencyController {
  public static final String CURRENCY_BASE_ENDPOINT = "api/v1/currency";

  private final CurrencyService currencyService;

  @GetMapping
  public ResponseEntity<ApiResponse<CurrenciesResponse>> getAll() {
    CurrenciesResponse response = currencyService.getAll();

    return ResponseEntity.ok(ApiResponse.success(GenericMessageKey.REQUEST_SUCCESS, response));
  }
}
