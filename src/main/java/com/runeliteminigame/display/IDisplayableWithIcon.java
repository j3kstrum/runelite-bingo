package com.runeliteminigame.display;

import com.runeliteminigame.IMinigamePlugin;

import java.awt.image.BufferedImage;

public interface IDisplayableWithIcon {

    BufferedImage getIcon(IMinigamePlugin plugin);
}
