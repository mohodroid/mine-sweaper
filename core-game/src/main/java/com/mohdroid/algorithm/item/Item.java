package com.mohdroid.algorithm.item;

import java.util.Objects;

public abstract class Item {

    protected boolean isSteppedOn = false;

    protected String name;

    protected char indicator;

    protected boolean block;

    public void setBlock(boolean block) {
        this.block = block;
    }

    public void setSteppedOn(boolean steppedOn) {this.isSteppedOn = true;}

    public boolean isSteppedOn() {return isSteppedOn;}

    protected boolean isChecked = false;

    protected boolean shouldAccessible;

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getName() {
        return name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public boolean isBlock() {
        return block;
    }

    public char getIndicator() {
        return indicator;
    }

    public void setIndicator(char indicator) {
        this.indicator = indicator;
    }

    public boolean isShouldAccessible() {
        return shouldAccessible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return isSteppedOn() == item.isSteppedOn() && getIndicator() == item.getIndicator() && isBlock() == item.isBlock() && isChecked() == item.isChecked() && isShouldAccessible() == item.isShouldAccessible() && getName().equals(item.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isSteppedOn(), getName(), getIndicator(), isBlock(), isChecked(), isShouldAccessible());
    }
}
