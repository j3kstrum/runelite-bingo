package com.runeliteminigame.display;

import com.runeliteminigame.IMinigamePlugin;

import java.awt.image.BufferedImage;

public interface IDisplayableElement extends IDisplayableWithIcon {

    BufferedImage getMainImage(IMinigamePlugin plugin);
}
