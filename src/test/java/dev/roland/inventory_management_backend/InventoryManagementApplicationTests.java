package dev.roland.inventory_management_backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InventoryManagementApplicationTests {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void contextLoadsWithH2SchemaAvailable() {
    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM document_sequences", Long.class);
    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM product_attribute_options", Long.class);
  }
}
