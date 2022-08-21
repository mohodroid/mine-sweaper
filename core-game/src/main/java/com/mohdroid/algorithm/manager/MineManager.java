package com.mohdroid.algorithm.manager;

import com.mohdroid.algorithm.item.Item;
import com.mohdroid.algorithm.item.assets.MineAsset;
import com.mohdroid.algorithm.manager.common.AbsManager;

public class MineManager extends AbsManager {


    public static MineManager getInstance() {
        return new MineManager();
    }

    @Override
    public boolean isMissionDone() {
        return true;
    }

    private int numberOfStepMines = 0;

    private MineManager() {
    }

    @Override
    public boolean isLoose() {
        return numberOfStepMines > 0;
    }

    @Override
    public boolean isWin() {
        return true;
    }

    @Override
    public boolean isBlockStep(Item asset) {
        if (!(asset instanceof MineAsset))
            throw new IllegalArgumentException("wrong asset type" + asset);
        numberOfStepMines++;
        return false;
    }

    public int getNumberOfStepMines() {
        return numberOfStepMines;
    }

}
