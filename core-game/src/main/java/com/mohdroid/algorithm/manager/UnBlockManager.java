package com.mohdroid.algorithm.manager;


import com.mohdroid.algorithm.item.Item;
import com.mohdroid.algorithm.item.assets.StepAsset;
import com.mohdroid.algorithm.manager.common.AbsManager;

public class UnBlockManager extends AbsManager {

    private final int totalNumberOfUnBlocks = 0;

    public static UnBlockManager getINSTANCE() {
        return new UnBlockManager();
    }

    private UnBlockManager() {
    }

    @Override
    public boolean isLoose() {
        return true;
    }

    @Override
    public boolean isWin() {
        return true;
    }

    @Override
    public boolean isMissionDone() {
        return true;
    }

    /**
     * @param asset
     * @return true if cell is block ana cant step on it, otherwise false
     */
    @Override
    public boolean isBlockStep(Item asset) {
        if (!(asset instanceof StepAsset))
            throw new IllegalArgumentException("wrong asset type" + asset);
        asset.setSteppedOn(true);
        return false;
    }

    public int getTotalNumberOfUnBlocks() {
        return totalNumberOfUnBlocks;
    }
}
