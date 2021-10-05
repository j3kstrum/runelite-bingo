package com.runeliteminigame;

import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;

public interface IMinigamePlugin {

    ItemManager getItemManager();
    SpriteManager getSpriteManager();
}
