package com.runeliteminigame.pluginlisteners;

import net.runelite.api.NPC;

public interface ICombatListener {

    void onPlayerKilledNPC(NPC killed, int damageDealt);
}
