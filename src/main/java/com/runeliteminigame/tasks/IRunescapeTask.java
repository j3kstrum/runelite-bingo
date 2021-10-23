package com.runeliteminigame.tasks;

import com.runeliteminigame.IMinigamePlugin;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Dictionary;

public interface IRunescapeTask {

    String TASK_TYPE = "taskType";

    boolean isCompleted();
    BufferedImage getImage(IMinigamePlugin plugin, Dimension requestedDimension);
    String getDescriptionText();
    Dictionary<String, Object> serializedTask();

}
