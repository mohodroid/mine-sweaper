package com.mohdroid.algorithm.item;


import static com.mohdroid.algorithm.item.ItemType.*;

import com.mohdroid.algorithm.item.assets.BlockAsset;
import com.mohdroid.algorithm.item.assets.ExitAsset;
import com.mohdroid.algorithm.item.assets.KeyAsset;
import com.mohdroid.algorithm.item.assets.MineAsset;
import com.mohdroid.algorithm.item.assets.SoldierAsset;
import com.mohdroid.algorithm.item.assets.StartAsset;
import com.mohdroid.algorithm.item.assets.StepAsset;

/**
 * For apply dependency inversion principle, Item is abstraction, then Main class depends on abstraction
 * and low level components assets and booster depends on abstraction
 *
 * @author Mohsen Mirtalebi
 */
public final class ItemCreator {

    /**
     * In order to avoid instantiation.
     */
    private ItemCreator() {
    }

    public static Item createItem(ItemType type) {
        Item asset = null;
        if (type == BLOCK) {
            asset = new BlockAsset();
        } else if (type == EXIT) {
            asset = new ExitAsset();
        } else if (type == MINE) {
            asset = new MineAsset();
        } else if (type == KEY) {
            asset = new KeyAsset();
        } else if (type == SOLDIER) {
            asset = new SoldierAsset();
        } else if (type == STEP) {
            asset = new StepAsset();
        } else if (type == START) {
            asset = new StartAsset();
        }
        return asset;
    }
}
