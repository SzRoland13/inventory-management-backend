# Product secondary unit behavior

## Purpose

A product may have a main unit and one optional secondary unit. The secondary unit represents the contents or smaller measure of one main unit. This lets users maintain and transact inventory in either unit while the system keeps one stock quantity.

Example:

- Main unit: `BOX`
- Secondary unit: `PAIR`
- Conversion: `1 BOX = 100 PAIR`

The product's price is defined per main unit. Secondary-unit prices are derived from the conversion.

## Product fields

Add these fields to `products`:

| Field | Meaning |
|---|---|
| `secondary_unit_id` | Optional FK to `units.id`; the product's secondary unit |
| `secondary_units_per_main_unit` | Positive conversion amount; number of secondary units in one main unit |

The conversion is only present when a secondary unit is configured. The main and secondary units must be different. The database requires a positive conversion and matching nullability for the secondary unit and conversion fields.

This supports one secondary unit per product. Supporting multiple packaging levels (for example, pair → box → case) would need a separate unit-conversion model.

## Inventory representation

Keep one stock quantity per warehouse and product:

- If a product has a secondary unit, store quantity in that secondary unit.
- Otherwise, store quantity in the main unit.

For a product with `1 BOX = 100 PAIR`:

| Event or display | Stored quantity | Display |
|---|---:|---|
| Receive 3 boxes | 300 pairs | 3 boxes |
| Sell 50 pairs | 250 pairs | 2 boxes + 50 pairs |
| Sell 1 box | 150 pairs | 1 box + 50 pairs |

The display in boxes and remainder is calculated from the stored quantity and current conversion:

```text
full main units = floor(stock in secondary units / secondary units per main unit)
remainder = stock in secondary units % secondary units per main unit
```

Do not store both a box count and a pair count as independent stock quantities. They describe the same stock and could drift apart. If the UI needs a breakdown, derive it from the single stored quantity.

Quantities and conversion amounts should have enough decimal precision for fractional conversions and partial-unit stock. The product plan proposes `NUMERIC(24,8)` for price values; quantity and conversion precision should be selected to support the expected unit conversions as well.

## Document entry and stock movements

A document line can be entered in either the product's main or secondary unit. Store the entered quantity and unit on the line, then normalize it to the stock unit when creating the stock movement.

For `1 BOX = 100 PAIR`:

| User enters | Normalized stock quantity |
|---|---:|
| 2 boxes received | +200 pairs |
| 50 pairs sold | -50 pairs |

The sign depends on the document's inventory effect. The document line retains the entered value and unit; the stock movement records the normalized quantity in the product's stock unit.

## Pricing

Store product net price per main unit. Derive the price per secondary unit by dividing by the conversion amount:

```text
secondary-unit net price = main-unit net price / secondary units per main unit
```

For a box price of 100 and 100 pairs per box, the derived price is 1 per pair. A line entered as 2 pairs has a net line amount of 2 before tax.

Store sufficient decimal precision for derived and line prices. Round when displaying amounts or finalizing document totals according to the currency's rules. Gross price is derived using the VAT rate; it does not need a separate product price field if net price and VAT rate are stored.

## Document snapshots

Each document line should retain the values used when it was created, even if the product changes later. The line should snapshot:

- entered quantity;
- entered unit (main or secondary);
- conversion factor used for the line;
- unit price in the entered unit;
- unit code and display name as they were when the line was created;
- tax rate and any other existing price snapshots needed by the document.

This keeps historical documents stable if a product's price, conversion, or unit label is edited later. Stock movements use the normalized quantity calculated from the line's saved conversion, not a subsequently edited product conversion.

## Editing the conversion

The stock quantity is stored in the secondary unit, so changing the conversion does not change that stored quantity. It changes how the system expresses the quantity as main units and a remainder.

Example: stock is 300 pairs. With `100 pairs per box`, that displays as 3 boxes. If an administrator changes the conversion to `120 pairs per box`, the same stock displays as 2 boxes + 60 pairs. No inventory movement occurred; only the packaging interpretation changed.

Document snapshots preserve the conversion used by earlier transactions. The current stock display uses the current product conversion. Administrators should understand that editing the conversion can change the displayed number of full main units and, if price is stored per main unit, the derived price per secondary unit. Once stock movements exist, the main and secondary unit identities cannot be changed or removed; the conversion ratio can still be edited.

## Scope and constraints

- A product has zero or one secondary unit.
- The conversion describes the number of secondary units in exactly one main unit.
- Product net price is per main unit; secondary-unit price is derived.
- Stock is represented once, in the secondary unit when configured and otherwise in the main unit.
- Document lines preserve entered units while movements normalize to the stock unit.
- Historical document lines use snapshotted unit, conversion, and price values.
- Conversion edits affect current stock presentation and derived secondary pricing, but do not rewrite stock quantities or past document lines.

The backend implements this model through product fields, document-line snapshots, stock movement normalization, and per-warehouse product response data.
