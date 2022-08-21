package com.mohdroid.algorithm.manager;


import com.mohdroid.algorithm.item.Item;
import com.mohdroid.algorithm.item.assets.KeyAsset;
import com.mohdroid.algorithm.manager.common.AbsManager;
import com.sun.tools.javac.util.Log;

public class KeyManager extends AbsManager {


    private int numberOfFoundKeys = 0;

    public static KeyManager getInstance() {
        return new KeyManager();
    }

    private KeyManager() {
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
        return numberOfFoundKeys == getTotalNumbers();
    }

    /**
     * @param asset
     * @return true if cell is block ana cant step on it, otherwise false
     */
    @Override
    public boolean isBlockStep(Item asset) {
        if (!(asset instanceof KeyAsset))
            throw new IllegalArgumentException("wrong asset type" + asset);
        if (asset.isSteppedOn()){
            return false;
        }
        numberOfFoundKeys++;
        asset.setSteppedOn(true);
        return false;
    }

    public int getNumberOfFoundKeys() {
        return numberOfFoundKeys;
    }
}
