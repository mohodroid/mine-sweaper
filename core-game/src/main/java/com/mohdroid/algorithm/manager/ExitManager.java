package com.mohdroid.algorithm.manager;


import com.mohdroid.algorithm.item.Item;
import com.mohdroid.algorithm.item.assets.ExitAsset;
import com.mohdroid.algorithm.manager.common.AbsManager;

public class ExitManager extends AbsManager {

    private boolean isWin = false;


    public static ExitManager getInstance() {
        return new ExitManager();
    }

    private ExitManager() {
    }

    @Override
    public boolean isMissionDone() {
        return true;
    }

    @Override
    public boolean isWin() {
        return isWin;
    }

    @Override
    public boolean isLoose() {
        return true;
    }

    @Override
    public void execute() {
        for (Item item : items) {
            item.setIndicator(']');
            item.setBlock(false);
        }

    }

    @Override
    public boolean isBlockStep(Item asset) {
        if (!(asset instanceof ExitAsset))
            throw new IllegalArgumentException("wrong asset type" + asset);
        isWin = !asset.isBlock();
        return asset.isBlock();
    }
}
