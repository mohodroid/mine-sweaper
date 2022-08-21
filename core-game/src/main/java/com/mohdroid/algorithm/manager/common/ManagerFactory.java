package com.mohdroid.algorithm.manager.common;


import com.mohdroid.algorithm.item.Item;
import com.mohdroid.algorithm.item.assets.BlockAsset;
import com.mohdroid.algorithm.item.assets.ExitAsset;
import com.mohdroid.algorithm.item.assets.KeyAsset;
import com.mohdroid.algorithm.item.assets.MineAsset;
import com.mohdroid.algorithm.item.assets.StartAsset;
import com.mohdroid.algorithm.item.assets.StepAsset;
import com.mohdroid.algorithm.manager.BlockManager;
import com.mohdroid.algorithm.manager.ExitManager;
import com.mohdroid.algorithm.manager.KeyManager;
import com.mohdroid.algorithm.manager.MineManager;
import com.mohdroid.algorithm.manager.StartManager;
import com.mohdroid.algorithm.manager.UnBlockManager;

import java.util.HashMap;

public final class ManagerFactory {
    private final HashMap<Class<? extends Item>, AbsManager> managers = new HashMap<>();

    public ManagerFactory() {
        putAsset(MineAsset.class, MineManager.getInstance());
        putAsset(KeyAsset.class, KeyManager.getInstance());
        putAsset(BlockAsset.class, BlockManager.getINSTANCE());
        putAsset(StepAsset.class, UnBlockManager.getINSTANCE());
        putAsset(ExitAsset.class, ExitManager.getInstance());
        putAsset(StartAsset.class, StartManager.getINSTANCE());
    }

    public void putAsset(Class<? extends Item> item, AbsManager manager) {
        managers.put(item, manager);
    }

    public AbsManager createManager(Item item) {
        AbsManager manager = managers.get(item.getClass());
        manager.increaseNumber();
        manager.setItem(item);
        return manager;
    }

    public AbsManager getManager(Item item) {
        return getManager(item.getClass());
    }

    public AbsManager getManager(Class<? extends Item> item) {
        return managers.get(item);
    }






}
