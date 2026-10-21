# Warehouse types and transfer flows

## Status and scope

This document records warehouse behavior and future design direction for the inventory-management application. Company-scoped warehouse configuration, types, address ownership, archiving, and type-based posting restrictions are implemented by the warehouse feature. The multi-stage via-transit workflow and bin-level stock locations remain future work.

The initial stock model is warehouse-level: balances and movements identify a warehouse, but not shelves, bins, aisles, or other internal stock locations. Bin-level tracking may be added later by introducing stock locations beneath a warehouse and making balances and movements location-aware.

## Warehouse identity and lifecycle

A warehouse belongs to one company. Warehouse names and codes are unique within that company, rather than globally. The warehouse code is a user-editable identifier and may change; documents must retain a snapshot of the code used when each document was created so that historical documents remain understandable.

A warehouse may have an optional address. Warehouses can be active or archived. Archiving is allowed only when every product balance at that warehouse is exactly zero. Any nonzero balance, positive or negative, prevents archiving. Existing zero-balance rows do not prevent archiving.

Archived warehouses remain visible in historical documents, stock movements, and reports. They cannot be selected for new stock-affecting operations or receive new stock. Archiving does not delete the warehouse or its history.

## Warehouse types

Companies may have multiple warehouses of each type. `STANDARD` and `VIRTUAL` currently support the same stock workflows. `TRANSIT` is restricted to transfer receipt and transfer delivery documents; the end-to-end dispatch-to-receipt transit workflow is not yet implemented.

### `STANDARD`

A normal stock-holding warehouse, usually associated with a physical site. It can be used for ordinary receiving, shipping, adjustments, and as a source or destination in transfers, subject to document rules.

### `TRANSIT`

A logical stock-holding warehouse representing inventory dispatched from one warehouse but not yet received at its destination. A company can configure multiple transit warehouses to distinguish routes or processes, such as `Transit A-B` and `Transit B-C`.

Transit warehouses are restricted to transfer workflows. They are not available for ordinary purchase receipts, sales deliveries, or routine manual stock adjustments. A deliberate correction workflow is needed for mistakes, loss, or damage; corrections must be auditable and must not erase the original movements.

Transit warehouses should not be archived while they have any nonzero product balance or an outstanding dispatched quantity. They remain visible in balances and history like other warehouses.

### `VIRTUAL`

A logical warehouse type reserved for future document workflows. Its precise purpose and allowed operations will be defined when the documents feature that uses it is designed. Until then, it should not be treated as interchangeable with `TRANSIT`: transit has the specific dispatch/receipt restrictions described here, while virtual warehouse behavior is intentionally unspecified.

## Transfer options

When a user creates a transfer from warehouse A to warehouse B, the workflow can offer:

1. **Direct transfer:** move stock from A to B without an intermediate transit balance. This is appropriate when dispatch and receipt are treated as one completed operation.
2. **Transfer via transit:** select an active `TRANSIT` warehouse configured for the route or process. Dispatch and receipt are recorded separately, so stock remains visible while travelling.

The selected transit warehouse is part of the transfer record and must be the same for its dispatch and receipt. Users must not substitute a different transit warehouse at receipt time. The source, destination, and transit warehouse must belong to the same company.

## Transfer via transit: lifecycle

Example: warehouse A dispatches 10 chairs to warehouse B using `Transit A-B`.

### 1. Create the transfer

The transfer records warehouse A as the source, warehouse B as the final destination, and `Transit A-B` as the chosen transit warehouse. The transfer lines record the requested products and quantities. Creating or confirming the request alone does not change stock balances.

### 2. Dispatch from A

When dispatch is posted, each dispatched quantity creates two stock movements:

- A: negative quantity
- `Transit A-B`: positive quantity

For a dispatch of 10 chairs, A decreases by 10 and `Transit A-B` increases by 10. This is the point at which stock leaves A and becomes in transit. The dispatch document records the warehouse codes as they appeared when that document was created.

### 3. Receive at B

When B records receipt, each received quantity creates two movements:

- `Transit A-B`: negative quantity
- B: positive quantity

Receiving all 10 chairs reduces transit stock by 10 and increases B by 10. The receipt records the same transit warehouse and snapshots the relevant warehouse codes for that document.

### 4. Partial receipt

Partial receipt is supported by the transfer lifecycle. If 6 of the 10 chairs arrive, receipt moves 6 from `Transit A-B` to B. The remaining 4 stay in transit and the transfer remains outstanding or partially completed. A later receipt can receive some or all of the remainder.

The system must reject a receipt quantity greater than the outstanding dispatched quantity for that transfer line. It must also prevent the same dispatched quantity from being received more than once, including when requests are retried or processed concurrently.

### 5. Completion and exceptions

The transfer is complete when all dispatched quantities have been received or explicitly resolved through an authorized correction workflow. Lost, damaged, or incorrectly dispatched goods require traceable corrective movements and a recorded reason. Cancelling an undispatched transfer does not affect stock; a transfer with posted dispatch movements cannot simply be cancelled as though nothing moved.

## Document and movement relationships

The transfer is the business process connecting the original source and final destination. Dispatch and receipt documents are stages of that same process, not unrelated transfers. They must be linked so users can navigate from the transfer to its dispatches, receipts, outstanding quantities, and stock movements.

The existing document model has `TRANSFER_REQUEST`, `TRANSFER_DELIVERY`, and `TRANSFER_RECEIPT` types, source and target warehouse references, document relations, and stock movements. The future implementation should decide whether the request is the persistent transfer header with one or more delivery/receipt documents, or whether a dedicated transfer entity is warranted. Whichever option is chosen must preserve the same invariants:

- dispatch movements are A to the selected transit warehouse;
- receipt movements are from that transit warehouse to B;
- the final destination and transit warehouse remain tied to the same transfer;
- receipts cannot exceed the unreceived dispatched amount;
- historical warehouse codes are preserved on documents;
- old documents and movements remain readable when a warehouse is archived.

## Example balances

Starting balances: A has 20 chairs, `Transit A-B` has 0, and B has 3.

After dispatching 10 chairs:

| Warehouse | Balance |
|---|---:|
| A | 10 |
| Transit A-B | 10 |
| B | 3 |

After receiving 6 chairs at B:

| Warehouse | Balance |
|---|---:|
| A | 10 |
| Transit A-B | 4 |
| B | 9 |

After receiving the remaining 4 chairs:

| Warehouse | Balance |
|---|---:|
| A | 10 |
| Transit A-B | 0 |
| B | 13 |

The transit balance makes dispatched-but-not-yet-received inventory visible without requiring bin-level tracking.

## Future: internal stock locations and bin-level tracking

The first implementation can track stock only at warehouse level. If users later need to know the exact area, shelf, or bin holding a product, add internal stock locations beneath each warehouse. This is a future extension, not a requirement for the initial warehouse and transfer features.

### Example hierarchy

An internal location belongs to one warehouse and may optionally have a parent location, allowing a hierarchy such as:

```text
Central (warehouse)
└── Storage (location)
    └── Aisle 3 (location)
        └── Shelf B (location)
```

Locations should have a recognizable code or name, unique within their warehouse. A location can also have a role or type when operations need to distinguish areas such as receiving, storage, packing, or shipping. The exact supported types and rules should be decided with the workflows that need them; the warehouse type (`STANDARD`, `TRANSIT`, or `VIRTUAL`) remains a separate concept from an internal location type.

### Effect on stock data

With bin-level tracking, a balance is no longer identified only by product and warehouse. It must also identify the internal stock location (and any other inventory dimensions introduced later). Movements likewise record the location from which stock leaves or into which stock arrives. Database constraints must prevent duplicate balances for the same product and stock dimensions.

For example, if Central has 25 chairs, the system could report:

| Warehouse location | Chairs |
|---|---:|
| Receiving | 10 |
| Aisle 3 / Shelf B | 15 |
| **Central total** | **25** |

The warehouse total is the sum of its locations. It should be derived from location balances or maintained as a projection that can be reconciled; it must not become an independently editable quantity that can disagree with those balances.

### Effect on warehouse workflows

Receiving could first place goods into a receiving location, then record a put-away movement into a storage bin. Picking could select stock from a specific bin and move it to packing or shipping. A stock count could target a bin rather than only the whole warehouse. These are internal movements: the total quantity in the warehouse stays the same, while quantities in the affected locations change.

Transfers between warehouses can remain at warehouse level initially. If operational needs later require tracking the exact source and destination bins, dispatch and receipt can include those locations. For a transfer through a transit warehouse, the dispatch would move stock from a source bin into the transit warehouse's designated stock area, and receipt would move it from transit into a destination bin. Transit tracking and bin tracking are separable features; implementing one does not require implementing the other.

### Decisions for a later implementation

Before adding internal locations, decide:

- whether every stock movement must name a location, or whether some workflows may operate at warehouse level;
- whether receiving, shipping, packing, damaged stock, and in-transit stock use dedicated locations or other representations;
- whether locations can be nested and which levels may hold stock;
- how location codes are generated and kept unique;
- whether products may be mixed in a location and whether capacity limits are needed;
- how existing warehouse-level balances and movement history migrate into locations;
- how warehouse totals are queried and reconciled from location-level stock.

The core invariant should be that the sum of stock across a warehouse's locations agrees with the warehouse-level total shown to users. Location-level balances and movements should preserve the same auditability and quantity rules as warehouse-level inventory.
