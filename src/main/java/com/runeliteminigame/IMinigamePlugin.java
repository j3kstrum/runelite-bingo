package com.runeliteminigame;

import com.runeliteminigame.tasks.IRunescapeTask;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;

public interface IMinigamePlugin {

    ItemManager getItemManager();
    SpriteManager getSpriteManager();

    void registerPlayerKilledNPCListener(IRunescapeTask task);
    void removePlayerKilledNPCListener(IRunescapeTask task);
}
