# Product database schema

This document describes the product table and the tables that participate in the product catalog and inventory flows. It reflects the Liquibase migrations currently in the repository, especially `001-init-schema.xml`, `011-refactor-product-related-tables.xml`, `012-refactor-documents-related-tables.xml`, `013-add-stock-movement-table.xml`, and `014-products-secondary-units.xml`.

## Products

| Column | Type | Null? | Meaning / relationship |
|---|---|---:|---|
| `id` | `SERIAL` | No | Primary key |
| `sku` | `VARCHAR(100)` | No | Unique product SKU |
| `ean` | `VARCHAR(20)` | Yes | Barcode |
| `name` | `VARCHAR(255)` | No | Product name |
| `description` | `TEXT` | Yes | Product description |
| `brand` | `VARCHAR(100)` | Yes | Brand |
| `status` | `VARCHAR(50)` | No | Product status; defaults to `ACTIVE` |
| `unit_id` | `INTEGER` | No | FK to `units.id` |
| `net_price` | `NUMERIC(24,8)` | No | Net selling price per main unit |
| `cost_price` | `NUMERIC(24,8)` | Yes | Cost price per main unit |
| `vat_rate` | `NUMERIC(5,3)` | No | VAT rate |
| `currency_id` | `INTEGER` | Yes | FK to `currencies.id` |
| `company_id` | `INTEGER` | No | FK to `companies.id` |
| `secondary_unit_id` | `INTEGER` | Yes | Optional FK to `units.id` |
| `secondary_units_per_main_unit` | `NUMERIC(24,8)` | Yes | Number of secondary units contained in one main unit |
| `weight` | `NUMERIC(15,3)` | Yes | Product weight |
| `width` | `NUMERIC(15,3)` | Yes | Product width |
| `height` | `NUMERIC(15,3)` | Yes | Product height |
| `depth` | `NUMERIC(15,3)` | Yes | Product depth |
| `created_at` | `TIMESTAMP` | Yes | Creation timestamp |
| `updated_at` | `TIMESTAMP` | Yes | Last update timestamp |
| `deleted_at` | `TIMESTAMP` | Yes | Soft-delete/archive time |

`sku` is unique per company, including archived products. A product requires a company and main unit; currency and secondary unit are optional. `secondary_unit_id` and `secondary_units_per_main_unit` must either both be null or define a positive conversion, and the two unit IDs must differ.

## Product catalog tables

### `units`

Defines units used by products. System units can be shared; company units are scoped to a company. The migrations insert `PIECE`, `KG`, `BOX`, and `PAIR` as system units.

| Column | Type | Null? | Meaning / relationship |
|---|---|---:|---|
| `id` | `SERIAL` | No | Primary key |
| `code` | `VARCHAR(50)` | No | Unit code |
| `name` | `VARCHAR(100)` | No | Display name |
| `symbol` | `VARCHAR(20)` | No | Short unit symbol |
| `is_system` | `BOOLEAN` | No | Whether it is a shared system unit; defaults to false |
| `company_id` | `INTEGER` | No for company units; null for system units | FK to `companies.id`; company deletion cascades |
| `created_at`, `updated_at` | `TIMESTAMP` | Yes | Audit timestamps |

Company-unit codes are unique within a company. System-unit codes are globally unique, and system units have no company; company units require a company. Referenced by `products.unit_id` and optionally `products.secondary_unit_id`.

### `product_categories`

Stores company-specific product categories, including parent-child hierarchy.

| Column | Type | Null? | Meaning / relationship |
|---|---|---:|---|
| `id` | `SERIAL` | No | Primary key |
| `company_id` | `INTEGER` | No | FK to `companies.id`; company deletion cascades |
| `parent_id` | `INTEGER` | Yes | Self-reference to `product_categories.id`; parent deletion sets null |
| `code` | `VARCHAR(100)` | No | Category code |
| `name` | `VARCHAR(150)` | No | Category name |
| `description` | `TEXT` | Yes | Category description |
| `sort_order` | `INTEGER` | No | Display order; defaults to 0 |
| `created_at`, `updated_at` | `TIMESTAMP` | Yes | Audit timestamps |

Unique constraint: `(company_id, code)`.

### `product_category_assignments`

Join table for the many-to-many relationship between products and categories. Both foreign keys form the composite primary key; deleting either referenced row cascades to the assignment.

| Column | Type | Null? | Meaning / relationship |
|---|---|---:|---|
| `product_id` | `INTEGER` | No | FK to `products.id` |
| `category_id` | `INTEGER` | No | FK to `product_categories.id` |
| `created_at` | `TIMESTAMP` | Yes | Assignment timestamp |

### `product_attribute_definitions`

Defines configurable attributes scoped to a company. `value_type` determines which value storage column or option is used.

| Column | Type | Null? | Meaning / relationship |
|---|---|---:|---|
| `id` | `SERIAL` | No | Primary key |
| `company_id` | `INTEGER` | No | FK to `companies.id`; company deletion cascades |
| `code` | `VARCHAR(100)` | No | Attribute code |
| `name` | `VARCHAR(150)` | No | Attribute display name |
| `value_type` | `VARCHAR(50)` | No | Attribute value type (enum stored as text) |
| `is_required` | `BOOLEAN` | No | Whether the attribute is required; defaults to false |
| `created_at`, `updated_at` | `TIMESTAMP` | Yes | Audit timestamps |

Unique constraint: `(company_id, code)`.

### `product_attribute_options`

Stores selectable choices for a fixed-option attribute. Deleting the definition cascades to its options.

| Column | Type | Null? | Meaning / relationship |
|---|---|---:|---|
| `id` | `SERIAL` | No | Primary key |
| `definition_id` | `INTEGER` | No | FK to `product_attribute_definitions.id` |
| `value` | `VARCHAR(150)` | No | Option value |
| `sort_order` | `INTEGER` | No | Display order; defaults to 0 |
| `created_at` | `TIMESTAMP` | Yes | Creation timestamp |

Unique constraint: `(definition_id, value)`.

### `product_attribute_values`

Stores at most one value per product and attribute definition. Deleting the product or definition cascades to its value row. The schema requires exactly one value column; application validation also checks that it matches the definition type. An option in use cannot be deleted.

| Column | Type | Null? | Meaning / relationship |
|---|---|---:|---|
| `id` | `SERIAL` | No | Primary key |
| `product_id` | `INTEGER` | No | FK to `products.id` |
| `definition_id` | `INTEGER` | No | FK to `product_attribute_definitions.id` |
| `option_id` | `INTEGER` | Yes | Optional FK to `product_attribute_options.id` |
| `text_value` | `TEXT` | Yes | Text value |
| `number_value` | `NUMERIC(15,3)` | Yes | Numeric value |
| `date_value` | `DATE` | Yes | Date value |
| `boolean_value` | `BOOLEAN` | Yes | Boolean value |
| `created_at`, `updated_at` | `TIMESTAMP` | Yes | Audit timestamps |

Unique constraint: `(product_id, definition_id)`; check constraint: exactly one of the five value columns is non-null.

## Inventory and document tables

### `stock_balances`

Current aggregate quantity for one product at one warehouse. The warehouse-product pair is unique. Product and warehouse deletion cascade to the balance.

| Column | Type | Null? | Meaning / relationship |
|---|---|---:|---|
| `id` | `SERIAL` | No | Primary key |
| `warehouse_id` | `INTEGER` | No | FK to `warehouses.id` |
| `product_id` | `INTEGER` | No | FK to `products.id` |
| `quantity` | `NUMERIC(24,8)` | Yes | On-hand quantity; stored in the secondary unit when configured, otherwise the main unit; defaults to 0 |
| `updated_at` | `TIMESTAMP` | Yes | Last update timestamp |

### `stock_movements`

Inventory movement ledger. A movement always references a warehouse and product, and may be associated with a document, document line, and user.

| Column | Type | Null? | Meaning / relationship |
|---|---|---:|---|
| `id` | `SERIAL` | No | Primary key |
| `document_id` | `INTEGER` | Yes | FK to `documents.id` |
| `document_line_id` | `INTEGER` | Yes | FK to `document_lines.id` |
| `warehouse_id` | `INTEGER` | No | FK to `warehouses.id` |
| `product_id` | `INTEGER` | No | FK to `products.id` |
| `quantity_change` | `NUMERIC(24,8)` | No | Signed quantity change, normalized to the product stock unit |
| `movement_type` | `VARCHAR(50)` | No | Movement type |
| `created_at` | `TIMESTAMP` | Yes | Creation timestamp |
| `created_by_user_id` | `INTEGER` | Yes | FK to `users.id` |

Indexes cover `document_id`, `document_line_id`, and `(warehouse_id, product_id)`.

### `document_lines`

Document item rows reference a product. They also keep product name, SKU, and VAT snapshots so existing documents can retain historical product details. Document line columns and constraints are defined in migration `012-refactor-documents-related-tables.xml`.

| Column | Type | Null? | Meaning / relationship |
|---|---|---:|---|
| `id` | `SERIAL` | No | Primary key |
| `document_id` | `INTEGER` | No | FK to `documents.id` |
| `product_id` | `INTEGER` | No | FK to `products.id` |
| `quantity` | `NUMERIC(24,8)` | No | Entered document quantity |
| `fulfilled_quantity` | `NUMERIC(24,8)` | No | Fulfilled quantity; defaults to 0 |
| `unit_price` | `NUMERIC(24,8)` | No | Price for one entered unit |
| `total_price` | `NUMERIC(24,8)` | No | Line total before currency rounding |
| `unit_id_snapshot` | `INTEGER` | No | FK to `units.id` for the entered unit |
| `unit_code_snapshot` | `VARCHAR(50)` | No | Unit code as it was when the line was created |
| `unit_name_snapshot` | `VARCHAR(100)` | No | Unit name as it was when the line was created |
| `conversion_factor_snapshot` | `NUMERIC(24,8)` | No | Stock units represented by one entered unit |
| `currency_id` | `INTEGER` | Yes | Optional FK to `currencies.id` |
| `product_name_snapshot` | `VARCHAR` | Yes | Name as recorded on the document |
| `product_sku_snapshot` | `VARCHAR(100)` | Yes | SKU as recorded on the document |
| `vat_rate_snapshot` | `NUMERIC(5,3)` | Yes | VAT rate as recorded on the document |
| `created_at`, `updated_at` | `TIMESTAMP` | Yes | Audit timestamps |

## Media tables

Product media is linked indirectly through `media_usages`: `entity_type` identifies the entity kind and `entity_id` holds its ID. This is polymorphic, so there is no foreign key from `media_usages.entity_id` to `products.id`.

### `media_assets`

| Column | Type | Null? | Meaning / relationship |
|---|---|---:|---|
| `id` | `SERIAL` | No | Primary key |
| `object_path` | `VARCHAR(500)` | No | Object storage path |
| `filename` | `VARCHAR(255)` | No | Original/display filename |
| `mime_type` | `VARCHAR(100)` | No | Content type |
| `file_size` | `BIGINT` | No | Size in bytes |
| `uploaded_by_user_id` | `INTEGER` | Yes | FK to `users.id`; set null if user is deleted |
| `created_at` | `TIMESTAMP` | No | Creation timestamp |

### `media_usages`

| Column | Type | Null? | Meaning / relationship |
|---|---|---:|---|
| `id` | `SERIAL` | No | Primary key |
| `media_asset_id` | `INTEGER` | No | FK to `media_assets.id`; asset deletion cascades |
| `entity_type` | `VARCHAR(80)` | No | Target type, such as product |
| `entity_id` | `INTEGER` | No | ID of the target entity; polymorphic, no FK |
| `usage_type` | `VARCHAR(80)` | No | Role of the media, such as product image |
| `sort_order` | `INTEGER` | Yes | Display order; defaults to 1 |
| `created_at` | `TIMESTAMP` | No | Creation timestamp |

Index: `(entity_type, entity_id)`.

## Relationship overview

```text
products ── many-to-one ── units
products ── many-to-one ── companies, currencies
products ── many-to-many ── product_categories (via product_category_assignments)
products ── one-to-many ── product_attribute_values ── product_attribute_definitions
product_attribute_definitions ── one-to-many ── product_attribute_options
products ── one-to-many ── stock_balances ── warehouses
products ── one-to-many ── stock_movements
products ── one-to-many ── document_lines ── documents
products ── polymorphic association ── media_usages ── media_assets
```

## Product API

- Product operations are under `api/v1/products`. Reads are available to active users; create, update, archive, and restore are available to admins and managers.
- Product settings are under `api/v1/product-settings` for units, categories, attribute definitions, and fixed-value options. Reads are available to active users; changes are admin-only.
- Requests use the current company context rather than accepting `company_id` from the client.
- Product lists are paginated in the database. `GET /api/v1/products` and `GET /api/v1/products/archived` accept `page` (default `0`), `size` (default `25`, maximum `100`), `search`, `brand`, `status`, `categoryId`, `unitId`, `sortBy`, and `sortDirection` query parameters. Search checks SKU, name, EAN, brand, and description. Sort fields are `SKU`, `NAME`, `BRAND`, `STATUS`, `NET_PRICE`, `VAT_RATE`, `CREATED_AT`, and `UPDATED_AT`; direction is `ASC` or `DESC`.
- Example: `GET /api/v1/products?page=0&size=25&search=glove&status=ACTIVE&sortBy=NAME&sortDirection=ASC`. The payload contains `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, and `last`.
- Archived products are hidden from the normal list. The archived list is available to admins and managers; `POST /api/v1/products/{id}/restore` restores the original row and SKU.
- A product response includes per-warehouse stock in the product's stock unit and a derived main-unit/remainder breakdown when a secondary unit is configured.

## Implementation references

- Product entity: `src/main/java/dev/roland/inventory_management_backend/features/product/Product.java`
- Product/unit/category/attribute migrations: `src/main/resources/db/changelog/changes/011-refactor-product-related-tables.xml`
- Initial product and stock balance migrations: `src/main/resources/db/changelog/changes/001-init-schema.xml`
- Document line migration: `src/main/resources/db/changelog/changes/012-refactor-documents-related-tables.xml`
- Stock movement migration: `src/main/resources/db/changelog/changes/013-add-stock-movement-table.xml`
- Media migrations: `src/main/resources/db/changelog/changes/007-create-media-assets-table.xml` and `008-create-media-usages-table.xml`
