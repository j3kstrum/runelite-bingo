package com.runeliteminigames;

import com.google.inject.Provides;
import com.runeliteminigames.controllers.impl.MainController;
import com.runeliteminigames.controllers.interf.IController;
import com.runeliteminigames.enums.ControlAction;
import com.runeliteminigames.enums.ShowPluginEvent;
import com.runeliteminigames.models.impl.MinigameContainer;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.WidgetMenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;

@Slf4j
@PluginDescriptor(
        name = "MiniGame Plugin",
        description = "Manages a collection of mini-games related to runescape-oriented tasks."
)
public class MinigamePlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private SpriteManager spriteManager;
    @Inject
    private ItemManager itemManager;
    @Inject
    private MinigameConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MenuManager menuManager;
    @Inject
    private KeyManager keyManager;
    @Inject
    private MouseManager mouseManager;

    private MinigameContainer containerModel;
    private IController controller;

    private final WidgetMenuOption[] menuOptions = new WidgetMenuOption[] {
            new WidgetMenuOption("Show", "Bingo Board", WidgetInfo.FIXED_VIEWPORT_QUESTS_TAB),
            new WidgetMenuOption("Show", "Bingo Board", WidgetInfo.RESIZABLE_VIEWPORT_QUESTS_TAB),
            // TODO: Can't currently find widget info for RESIZABLE_VIEWPORT_BOTTOM_LINE_QUEST_TAB. Maybe in the future?
            new WidgetMenuOption("Show", "Bingo Board", WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB)
    };

    public MinigamePlugin() {
        this.controller = new MainController();
    }

    @Provides
    MinigameConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(MinigameConfig.class);
    }

    @Override
    protected void startUp()
    {
        // Load or generate our container model.
        try {
            containerModel = this.load();
        }
        catch (IOException ioe) {
            containerModel = null;
        }
        finally {
            if (containerModel == null) {
                containerModel = new MinigameContainer();
            }
        }
        overlayManager.add(containerModel.getOverlay());

        // Add menu options.
        for (WidgetMenuOption menuOption : menuOptions) {
            menuManager.addManagedCustomMenu(menuOption);
        }

        // Register listeners.
        this.keyManager.registerKeyListener(controller);
        this.mouseManager.registerMouseListener(controller);
    }

    @Override
    protected void shutDown()
    {
        // Exit the model and save.
        containerModel.actionReceived(ControlAction.EXIT_DISPLAY);
        this.save();
        // Permit garbage collection in case plugin is not released by caller.
        containerModel = null;

        // Safely remove model from overlay manager.
        overlayManager.remove(containerModel.getOverlay());

        // Remove menu options.
        for (WidgetMenuOption menuOption : menuOptions) {
            this.menuManager.removeManagedCustomMenu(menuOption);
        }

        // Unregister listeners
        this.keyManager.unregisterKeyListener(controller);
        this.mouseManager.unregisterMouseListener(controller);
    }

    @Nullable
    private MinigameContainer load() throws IOException {
        throw new NotImplementedException("Not yet implemented");
    }

    private void save() {
        throw new NotImplementedException("Not yet implemented.");
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGING_IN) {
            this.controller.resetTransientState();
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath actorDeath) {
        this.controller.onGenericEvent(actorDeath);
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        this.controller.onGenericEvent(npcDespawned);
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
        this.controller.onGenericEvent(hitsplatApplied);
    }

    private boolean clickedOptionEquals(WidgetMenuOptionClicked event, WidgetMenuOption widgetMenuOption)
    {
        return event.getMenuOption().equals(widgetMenuOption.getMenuOption()) && event.getMenuTarget().equals(widgetMenuOption.getMenuTarget());
    }

    @Subscribe
    public void onWidgetMenuOptionClicked(WidgetMenuOptionClicked event) {
        boolean shouldShow = false;
        for (WidgetMenuOption menuOption : this.menuOptions) {
            if (clickedOptionEquals(event, menuOption)) {
                if (menuOption.getMenuOption().contains("Show")) {
                    shouldShow = true;
                }
                else if (menuOption.getMenuOption().contains("Hide")) {
                    shouldShow = false;
                }
            }
        }
        if (shouldShow) {
            this.controller.onGenericEvent(ShowPluginEvent.SHOW_PLUGIN);
            for (WidgetMenuOption menuOption : menuOptions){
                menuOption.setMenuOption("Hide");
            }
        }
        else {
            this.controller.onGenericEvent(ShowPluginEvent.HIDE_PLUGIN);
            for (WidgetMenuOption menuOption : menuOptions){
                menuOption.setMenuOption("Show");
            }
        }
    }
}
