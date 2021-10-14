package com.runeliteminigame.display;

import net.runelite.client.game.SpriteManager;

import java.awt.image.BufferedImage;

import static net.runelite.api.SpriteID.WINDOW_CLOSE_BUTTON_RED_X;
import static net.runelite.api.SpriteID.WINDOW_CLOSE_BUTTON_RED_X_HOVERED;

class MinigameCloseButtonHandler {

    private BufferedImage closeButtonImage;
    private BufferedImage closeButtonHoveredImage;
    private final SpriteManager spriteManager;

    MinigameCloseButtonHandler(SpriteManager manager) {
        this.spriteManager = manager;
    }

    BufferedImage getCloseButtonImage() {
        if (closeButtonImage == null) {
            closeButtonImage = spriteManager.getSprite(WINDOW_CLOSE_BUTTON_RED_X, 0);
        }
        return closeButtonImage;
    }

    BufferedImage getCloseButtonHoveredImage() {
        if (closeButtonHoveredImage == null) {
            closeButtonHoveredImage = spriteManager.getSprite(WINDOW_CLOSE_BUTTON_RED_X_HOVERED, 0);
        }
        return closeButtonHoveredImage;
    }
}
