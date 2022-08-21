package com.mohdroid.algorithm.manager.common;


import com.mohdroid.algorithm.item.Item;

import java.util.ArrayList;

public abstract class AbsManager {

    protected ArrayList<Item> items = new ArrayList<>();

    private int totalNumbers = 0;

    public int getTotalNumbers() {
        return totalNumbers;
    }

    public void increaseNumber() {
        totalNumbers++;
    }

    public void setItem(Item item) {
        items.add(item);
    }
    public abstract boolean isLoose();

    public abstract boolean isWin();

    public abstract boolean isMissionDone();

    public  void execute() {

    }

    /**
     * @return true if cell is block ana cant step on it, otherwise false
     */
    public abstract boolean isBlockStep(Item asset);

}
