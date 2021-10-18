package com.runeliteminigame;

import com.runeliteminigame.pluginlisteners.ICombatListener;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.menus.MenuManager;

public interface IMinigamePlugin {

    ItemManager getItemManager();
    SpriteManager getSpriteManager();

    void registerPlayerKilledNPCListener(ICombatListener task);
    void removePlayerKilledNPCListener(ICombatListener task);

    KeyManager getKeyManager();
    MouseManager getMouseManager();
    MenuManager getMenuManager();

    void requestRedraw();

    String name();
}
