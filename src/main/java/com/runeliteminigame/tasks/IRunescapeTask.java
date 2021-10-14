package com.runeliteminigame.tasks;

import com.runeliteminigame.IMinigamePlugin;

import java.awt.image.BufferedImage;
import java.util.Dictionary;

public interface IRunescapeTask {

    String TASK_TYPE = "taskType";

    boolean isCompleted();
    BufferedImage getImage(IMinigamePlugin plugin);
    Dictionary<String, Object> serializedTask();

}
