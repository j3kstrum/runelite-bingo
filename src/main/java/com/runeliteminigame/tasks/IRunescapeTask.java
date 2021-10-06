package com.runeliteminigame.tasks;

import com.runeliteminigame.IMinigamePlugin;
import net.runelite.api.NPC;
import net.runelite.api.events.ActorDeath;

import java.awt.image.BufferedImage;
import java.util.Dictionary;

public interface IRunescapeTask {

    String TASK_TYPE = "taskType";

    boolean isCompleted();
    BufferedImage getImage(IMinigamePlugin plugin);
    Dictionary<String, Object> serializedTask();

    void onPlayerKilledNPC(NPC killed, int damageDealt);

}
