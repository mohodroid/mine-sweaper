package com.mohdroid.algorithm.manager;


import com.mohdroid.algorithm.item.Item;
import com.mohdroid.algorithm.manager.common.AbsManager;

public class FlashlightManager extends AbsManager {


    public static FlashlightManager getINSTANCE() {
        return new FlashlightManager();
    }

    private FlashlightManager() {

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
        return false;
    }
}
