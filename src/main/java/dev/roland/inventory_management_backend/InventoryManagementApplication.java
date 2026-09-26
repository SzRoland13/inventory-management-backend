package dev.roland.inventory_management_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Bootstraps the Spring Boot inventory management API. */
@SpringBootApplication
public class InventoryManagementApplication {

  /**
   * Starts the Spring Boot application.
   *
   * @param args args supplied to this method
   */
  public static void main(String[] args) {
    SpringApplication.run(InventoryManagementApplication.class, args);
  }
}
