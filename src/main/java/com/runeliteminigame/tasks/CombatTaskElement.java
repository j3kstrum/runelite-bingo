package com.runeliteminigame.tasks;

import java.awt.image.BufferedImage;
import java.util.Hashtable;

import net.runelite.api.ItemID;
import net.runelite.api.NpcID;
import net.runelite.client.game.ItemManager;

import java.util.*;

public enum CombatTaskElement {
    CHICKEN("Chicken", ItemID.CHICKEN, 2, new HashSet<>(Arrays.asList(NpcID.CHICKEN, NpcID.CHICKEN_1174, NpcID.CHICKEN_2804, NpcID.CHICKEN_2805, NpcID.CHICKEN_2806, NpcID.CHICKEN_3316, NpcID.CHICKEN_3661, NpcID.CHICKEN_3662, NpcID.CHICKEN_9488, NpcID.CHICKEN_10494, NpcID.CHICKEN_10495, NpcID.CHICKEN_10496, NpcID.CHICKEN_10497, NpcID.CHICKEN_10498, NpcID.CHICKEN_10499, NpcID.CHICKEN_10556))),
    COW("Cow", ItemID.COWHIDE, 2, new HashSet<>(Arrays.asList(NpcID.COW, NpcID.COW_2791, NpcID.COW_2793, NpcID.COW_2795, NpcID.COW_5842, NpcID.COW_CALF, NpcID.COW_6401, NpcID.COW_10598, NpcID.COW_CALF_2794, NpcID.COW_CALF_2801, NpcID.PLAGUE_COW_4190, NpcID.PLAGUE_COW_4191, NpcID.UNDEAD_COW_4421)));

    static final Dictionary<String, CombatTaskElement> TASK_FROM_ID;

    private final String name;
    private final int itemID;
    private final int maxAmount;
    private final Set<Integer> npcIDs;

    CombatTaskElement(String name, int itemID, int maxAmount, Set<Integer> npcIDs) {
        this.name = name;
        this.itemID = itemID;
        this.maxAmount = maxAmount;
        this.npcIDs = npcIDs;
    }

    public String getName() {
        return this.name;
    }

    public int maxQuantity() {
        return this.maxAmount;
    }

    BufferedImage getImage(ItemManager itemManager) {
        return itemManager.getImage(this.itemID);
    }

    boolean isTaskNPC(int npcID) {
        return this.npcIDs.contains(npcID);
    }

    static {
        TASK_FROM_ID = new Hashtable<>();
        int[] arr = new int[]{1, 2, 3};
        for (CombatTaskElement elem : CombatTaskElement.values()) {
            TASK_FROM_ID.put(elem.name, elem);
        }
    }
}
