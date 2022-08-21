package com.mohdroid.algorithm;


import com.mohdroid.algorithm.dto.AssetDetailResponse;
import com.mohdroid.algorithm.dto.LevelDetailResponse;
import com.mohdroid.algorithm.item.ItemCreator;
import com.mohdroid.algorithm.item.assets.StartAsset;
import com.mohdroid.algorithm.manager.KeyManager;
import com.mohdroid.algorithm.manager.StartManager;
import com.mohdroid.algorithm.manager.common.AbsManager;
import com.mohdroid.algorithm.manager.common.ManagerFactory;

import java.util.ArrayList;
import java.util.List;


public class MissionCoordinator {

    private final MineSweeper mineSweeper;

    private int numberOfMovement;

    private final List<AbsManager> managers = new ArrayList<>();

    private List<ItemDetail> items;

    ManagerFactory managerFactory = new ManagerFactory();

    public int getTotalNumberOfKeys() {
        for (AbsManager manager : managers) {
            if (manager instanceof KeyManager) {
                return manager.getTotalNumbers();
            }
        }
        return 0;
    }

    public int getNumberOfFoundKeys() {
        for (AbsManager manager : managers) {
            if (manager instanceof KeyManager) {
                return ((KeyManager) manager).getNumberOfFoundKeys();
            }
        }
        return 0;
    }

    public int getNumberOfMines() {
        int count = 0;
        for (ItemDetail detail : items) {
            char indicator = detail.getItem().getIndicator();
            if (indicator == '*') {
                count++;
            }
        }
        return count;
    }

    public int getNumberOfMovements() {
        return numberOfMovement;
    }


    public MissionCoordinator(LevelDetailResponse levelDetailResponse) {
        int row = levelDetailResponse.getRows();
        int col = levelDetailResponse.getCols();
        items = new ArrayList<>(row * col);
        initBoardData(levelDetailResponse);
        for (int i = 0; i < items.size(); i++) {
            managers.add(managerFactory.createManager(items.get(i).getItem()));
        }
        mineSweeper = new MineSweeper(row, col, new ArrayList<>(items));
    }

    private void initBoardData(LevelDetailResponse levelDetailResponse) {
        for (AssetDetailResponse assetDetailResponse : levelDetailResponse.getAssetDetails()) {
            List<AssetDetailResponse.Position> positions = assetDetailResponse.getPositions();
            for (int i = 0; i < assetDetailResponse.getCount(); i++) {
                if (positions.isEmpty())
                    items.add(new ItemDetail(ItemCreator.createItem(assetDetailResponse.getType()), null));
                else
                    items.add(new ItemDetail(ItemCreator.createItem(assetDetailResponse.getType()), positions.get(i)));
            }
        }
    }

    private void execute() {
        for (AbsManager absManager : managers)
            absManager.execute();
    }

    private boolean checkWin() {
        for (AbsManager absManager : managers) {
            if (!absManager.isWin()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkLoose() {
        for (AbsManager absManager : managers) {
            if (!absManager.isLoose()) {
                return false;
            }
        }
        return true;
    }

    private boolean checkMissionDone() {
        for (AbsManager absManager : managers) {
            if (!absManager.isMissionDone()) {
                return false;
            }
        }
        return true;
    }

    public MineSweeper getMineSweeper() {
        return mineSweeper;
    }

    public PlayerState play(DirectionalOfMovement directionalOfMovement) {
        MineSweeper.Node node = mineSweeper.checkToPlay(directionalOfMovement);
        if (node == null) return PlayerState.CONTINUE;
        AbsManager manager = managerFactory.getManager(node.item);
        if (manager.isBlockStep(node.item)) {
            return PlayerState.BLOCK;
        }
        numberOfMovement++;
        if (numberOfMovement == 1) {
            managerFactory.getManager(StartAsset.class).execute();
        }
        mineSweeper.play(node);
        if (checkWin()) {
            return PlayerState.WIN;
        }
        if (checkLoose()) {
            return PlayerState.LOOSE;
        }
        if (checkMissionDone()) {
            execute();
            return PlayerState.MISSION_DONE;
        }
        return PlayerState.CONTINUE;
    }

    public enum PlayerState {
        WIN, LOOSE, CONTINUE, BLOCK, MISSION_DONE
    }
}
