package com.runeliteminigame;

import java.awt.image.BufferedImage;

public interface IDisplayableMinigame extends IMinigame {

    BufferedImage getIcon(IMinigamePlugin plugin);
    BufferedImage getGameImage(IMinigamePlugin plugin);

}
