package com.mohdroid.algorithm.manager;

import com.mohdroid.algorithm.item.Item;
import com.mohdroid.algorithm.item.assets.StartAsset;
import com.mohdroid.algorithm.manager.common.AbsManager;

public class StartManager extends AbsManager {


    public static StartManager getINSTANCE() {
        return new StartManager();
    }

    private StartManager() {
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

    @Override
    public void execute() {
        for (Item item : items) {
            item.setSteppedOn(true);
        }
    }

    @Override
    public boolean isBlockStep(Item asset) {
        if (!(asset instanceof StartAsset))
            throw new IllegalArgumentException("wrong asset type" + asset);
         if (asset.isBlock()) {
             asset.setSteppedOn(true);
         }
         return asset.isBlock();
    }
}
