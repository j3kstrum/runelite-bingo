package com.runelitebingo;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.runeliteminigame.IMinigamePlugin;
import com.runeliteminigame.MinigameDisplayContainer;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
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
import net.runelite.client.plugins.instancemap.InstanceMapInputListener;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

import static net.runelite.api.widgets.WidgetInfo.INVENTORY;

@Slf4j
@PluginDescriptor(
	name = "Bingo Plugin",
	description = "Adds an overlay (based on the InstanceMapPlugin) to play Bingo."
)
public class RuneliteBingoPlugin extends Plugin implements IMinigamePlugin
{
	@Inject
	private Client client;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private RuneliteBingoConfig config;

	@Provides
	RuneliteBingoConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RuneliteBingoConfig.class);
	}

	private final WidgetMenuOption openBingoOption = new WidgetMenuOption("Show", "Bingo Board", WidgetInfo.MINIMAP_WORLDMAP_OPTIONS);

	@Inject
	private InstanceMapInputListener inputListener;

	@Inject
	private OverlayManager overlayManager;

	private MinigameDisplayContainer bingoOverlay;

	@Inject
	private MenuManager menuManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private MouseManager mouseManager;

	@Override
	public void configure(Binder binder)
	{
		binder.bind(InstanceMapInputListener.class);
	}

	private void addCustomOptions()
	{
		menuManager.addManagedCustomMenu(openBingoOption);
	}

	private void removeCustomOptions()
	{
		menuManager.removeManagedCustomMenu(openBingoOption);
	}

	@Override
	public ItemManager getItemManager() {
		return this.itemManager;
	}

	@Override
	public SpriteManager getSpriteManager() {
		return this.spriteManager;
	}

	@Override
	protected void startUp() throws Exception
	{
		bingoOverlay = new MinigameDisplayContainer(this.client, this);
		overlayManager.add(bingoOverlay);
		addCustomOptions();
		keyManager.registerKeyListener(inputListener);
		mouseManager.registerMouseListener(inputListener);
		mouseManager.registerMouseWheelListener(inputListener);
	}

	@Override
	protected void shutDown() throws Exception
	{
		bingoOverlay.setShowBingo(false);
		overlayManager.remove(bingoOverlay);
		removeCustomOptions();
		keyManager.unregisterKeyListener(inputListener);
		mouseManager.unregisterMouseListener(inputListener);
		mouseManager.unregisterMouseWheelListener(inputListener);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
		bingoOverlay.onGameStateChanged(event);
	}

	private boolean clickedOptionEquals(WidgetMenuOptionClicked event, WidgetMenuOption widgetMenuOption)
	{
		return event.getMenuOption().equals(widgetMenuOption.getMenuOption()) && event.getMenuTarget().equals(widgetMenuOption.getMenuTarget());
	}

	@Subscribe
	public void onWidgetMenuOptionClicked(WidgetMenuOptionClicked event)
	{
		if (event.getWidget() != WidgetInfo.MINIMAP_WORLDMAP_OPTIONS)
		{
			return;
		}

		if (clickedOptionEquals(event, openBingoOption))
		{
			if (bingoOverlay.isOverlayShown())
			{
				closeOverlay();
			}
			else
			{
				openOverlay();
			}
		}
	}

	public void openOverlay()
	{
		bingoOverlay.setShowBingo(true);
		openBingoOption.setMenuOption("Hide");
	}

	public void closeOverlay()
	{
		bingoOverlay.setShowBingo(false);
		openBingoOption.setMenuOption("Show");
	}
}