package com.mohdroid.algorithm.dto;


public final class LevelDetailResponse {

    private final int rows;
    private final int cols;
    private final AssetDetailResponse[] assetDetails;

    public LevelDetailResponse(int boardRows, int boardCols, AssetDetailResponse[] assetDetails) {
        this.assetDetails = assetDetails;
        this.cols = boardCols;
        this.rows = boardRows;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }


    public AssetDetailResponse[] getAssetDetails() {
        return assetDetails;
    }
}
