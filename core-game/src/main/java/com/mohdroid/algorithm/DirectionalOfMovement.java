package com.mohdroid.algorithm;

public enum DirectionalOfMovement {
    NORTH(0), SOUTH(1), EAST(2), WEST(3);
    private final int value;

    DirectionalOfMovement(int value) {
        this.value = value;
    }

    public int getLevelCode() {
        return this.value;
    }

}