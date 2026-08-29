package dev.roland.inventory_management_backend.features.currency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.roland.inventory_management_backend.features.currency.Currency;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {}
