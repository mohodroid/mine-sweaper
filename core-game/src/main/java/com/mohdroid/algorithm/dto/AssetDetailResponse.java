package com.mohdroid.algorithm.dto;

import com.mohdroid.algorithm.item.ItemType;

import java.util.List;

public final class AssetDetailResponse {
    private final ItemType type;
    private final int count;
    private final List<Position> positions;

    AssetDetailResponse(ItemType type, int count, List<Position> positions) {
        this.type = type;
        this.count = count;
        this.positions = positions;
    }

    public ItemType getType() {
        return type;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public int getCount() {
        return count;
    }


    public static class Position {
        private final int x;
        private final int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
