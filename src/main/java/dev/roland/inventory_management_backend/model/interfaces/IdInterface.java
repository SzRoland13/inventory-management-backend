package dev.roland.inventory_management_backend.model.interfaces;

import java.io.Serializable;

public interface IdInterface<ID extends Serializable> {
    ID getId();
}
