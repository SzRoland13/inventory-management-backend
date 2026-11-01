package dev.roland.inventory_management_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import dev.roland.inventory_management_backend.features.document.enumeration.DocumentType;

@SpringBootTest
@ActiveProfiles("test")
class InventoryManagementApplicationTests {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void contextLoadsWithH2SchemaAvailable() {
    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM document_sequences", Long.class);
    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM product_attribute_options", Long.class);
    assertEquals(1L, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM companies", Long.class));
    assertEquals(
        1L,
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND status = 'SETUP_REQUIRED'",
            Long.class));
    assertEquals(
        DocumentType.values().length,
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM document_prefixes", Integer.class));
  }
}
