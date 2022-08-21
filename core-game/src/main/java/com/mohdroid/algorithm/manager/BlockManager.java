package com.mohdroid.algorithm.manager;


import com.mohdroid.algorithm.item.Item;
import com.mohdroid.algorithm.item.assets.BlockAsset;
import com.mohdroid.algorithm.manager.common.AbsManager;

public class BlockManager extends AbsManager {


    public static BlockManager getINSTANCE() {
       return new BlockManager();
    }

    private BlockManager() {
    }

    @Override
    public boolean isMissionDone() {
        return true;
    }

    @Override
    public boolean isWin() {
        return true;
    }

    @Override
    public boolean isLoose() {
        return true;
    }

    /**
     * @param asset
     * @return true if cell is block ana cant step on it, otherwise false
     */
    @Override
    public boolean isBlockStep(Item asset) {
        if (!(asset instanceof BlockAsset))
            throw new IllegalArgumentException("wrong asset type" + asset);
        return true;
    }
}
