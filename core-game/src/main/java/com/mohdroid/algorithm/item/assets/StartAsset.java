package com.mohdroid.algorithm.item.assets;


import com.mohdroid.algorithm.item.Item;

public class StartAsset extends Item {

    public StartAsset() {
        name = "start";
        indicator = 'S';
        shouldAccessible = true;
        block = true;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
