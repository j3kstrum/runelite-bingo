package com.runeliteminigames.controllers.impl;

import com.runeliteminigame.pluginlisteners.ICombatListener;
import com.runeliteminigames.controllers.interf.IController;
import net.runelite.api.Actor;
import net.runelite.api.Hitsplat;
import net.runelite.api.NPC;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;

import java.util.ArrayList;
import java.util.Hashtable;

public class CombatController implements IController {

    private final Hashtable<NPC, ArrayList<ICombatListener>> combatListeners = new Hashtable<>();
    private final Hashtable<NPC, Integer> playerDamageDealt = new Hashtable<>();

    private void onActorDeath(ActorDeath actorDeath) {
        Actor killed = actorDeath.getActor();
        if (!(killed instanceof NPC)) {
            return;
        }
        NPC killedNPC = (NPC) killed;
        for (ICombatListener combatListener : this.combatListeners.getOrDefault(killedNPC, new ArrayList<>())) {
            combatListener.onNPCKilled(playerDamageDealt.getOrDefault(killedNPC, 0));
        }
    }

    private void onNpcDespawned(NpcDespawned npcDespawned) {
        this.playerDamageDealt.remove(npcDespawned.getNpc());
    }

    private void onHitsplatApplied(HitsplatApplied hitsplatApplied) {
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

    @Override
    public void onGenericEvent(Object event) {
        if (event instanceof ActorDeath) {
            this.onActorDeath((ActorDeath) event);
        }
        else if (event instanceof NpcDespawned) {
            this.onNpcDespawned((NpcDespawned) event);
        }
        else if (event instanceof HitsplatApplied) {
            this.onHitsplatApplied((HitsplatApplied) event);
        }
    }

    @Override
    public void resetTransientState() {
        this.playerDamageDealt.clear();
        this.combatListeners.clear();
    }
}
