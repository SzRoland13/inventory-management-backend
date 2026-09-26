package dev.roland.inventory_management_backend.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/** Restricts access to users with the MANAGER or SALES role. */
@PreAuthorize("hasAnyRole('MANAGER', 'SALES')")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ManagerOrSales {}
