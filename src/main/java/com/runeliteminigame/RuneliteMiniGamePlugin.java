package com.runeliteminigame;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.runeliteminigame.display.MinigameDisplayContainer;
import com.runeliteminigame.pluginlisteners.ICombatListener;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.WidgetMenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Hashtable;

@Slf4j
@PluginDescriptor(
	name = "MiniGame Plugin",
	description = "Manages a collection of mini-games related to runescape-oriented tasks."
)
public class RuneliteMiniGamePlugin extends Plugin implements IMiniGamePlugin
{
	@Inject
	private Client client;
	@Inject
	private SpriteManager spriteManager;
	@Inject
	private ItemManager itemManager;
	@Inject
	private RuneliteMiniGameConfig config;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private MenuManager menuManager;
	@Inject
	private KeyManager keyManager;
	@Inject
	private MouseManager mouseManager;

	@Provides
	RuneliteMiniGameConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RuneliteMiniGameConfig.class);
	}

	private MinigameDisplayContainer bingoOverlay;

	// We'll have to iterate over them all with each death, and remove will be infrequent.
	// Arraylist is therefore fine.
	private final ArrayList<ICombatListener> playerKilledNPCListeners = new ArrayList<>();

	private final Hashtable<NPC, Integer> playerDamageDealt = new Hashtable<>();

	public void requestRedraw() {
		this.bingoOverlay.requestRedraw();
	}

	@Override
	public MouseManager getMouseManager() {
		return this.mouseManager;
	}

	@Override
	public KeyManager getKeyManager() {
		return this.keyManager;
	}

	@Override
	public MenuManager getMenuManager() {
		return this.menuManager;
	}

	@Override
	public void registerPlayerKilledNPCListener(ICombatListener listener) {
		this.playerKilledNPCListeners.add(listener);
	}

	@Override
	public void removePlayerKilledNPCListener(ICombatListener listener) {
		this.playerKilledNPCListeners.remove(listener);
	}

	@Override
	public void configure(Binder binder)
	{
//		binder.bind(MinigameDisplayContainer.class);
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
	protected void startUp()
	{
		bingoOverlay = new MinigameDisplayContainer();
		overlayManager.add(bingoOverlay);
	}

	@Override
	protected void shutDown()
	{
		bingoOverlay.shutDown();
		overlayManager.remove(bingoOverlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGING_IN)
		{
			this.playerDamageDealt.clear();
		}
		bingoOverlay.onGameStateChanged();
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath) {
		Actor killed = actorDeath.getActor();
		if (!(killed instanceof NPC)) {
			return;
		}
		NPC killedNPC = (NPC) killed;
		if (this.playerDamageDealt.containsKey(killedNPC)) {
			for (ICombatListener listener : this.playerKilledNPCListeners) {
				listener.onPlayerKilledNPC((NPC)(actorDeath.getActor()), playerDamageDealt.get(killedNPC));
			}
		}

	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned) {
		this.playerDamageDealt.remove(npcDespawned.getNpc());
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
		// Based off of DPS counter plugin.
		Actor target = hitsplatApplied.getActor();
		if (!(target instanceof NPC)) {
			return;
		}
		NPC npc = (NPC) target;
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();

		if (!hitsplat.isMine()) {
			return;
		}

		if (!this.playerDamageDealt.containsKey(npc)) {
			this.playerDamageDealt.put(npc, 0);
		}

		this.playerDamageDealt.put(npc, hitsplat.getAmount() + this.playerDamageDealt.get(npc));
	}

	@Subscribe
	public void onWidgetMenuOptionClicked(WidgetMenuOptionClicked event)
	{
		this.bingoOverlay.onWidgetMenuOptionClicked(event);
	}
}