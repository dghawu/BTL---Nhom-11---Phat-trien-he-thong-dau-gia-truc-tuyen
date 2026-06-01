package com.example.factory;

import com.example.model.item.Item;

public interface ItemFactory {
    Item createItem(String sellerId, String name, String id,
                    String description, double startingPrice,
                    Item.ItemStatus status);
}
