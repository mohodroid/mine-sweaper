package com.mohdroid.algorithm;


import com.mohdroid.algorithm.dto.AssetDetailResponse;
import com.mohdroid.algorithm.item.Item;

public final class ItemDetail {

    private final Item item;
    private final AssetDetailResponse.Position position;

    ItemDetail(Item item, AssetDetailResponse.Position position) {
        this.item = item;
        this.position = position;
    }

    public Item getItem() {
        return item;
    }

    public AssetDetailResponse.Position getPosition() {
        return position;
    }
}
