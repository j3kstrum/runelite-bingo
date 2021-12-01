package com.runeliteminigame.display;

import com.runeliteminigame.IMiniGamePlugin;
import com.runeliteminigames.util.ImageUtils;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class MinigameRightButton extends BaseMinigameDirectionalButton {

    private final MinigameDisplayContainer minigameDisplayContainer;
    private static final BufferedImage RIGHT_ARROW_IMAGE;

    static {
        RIGHT_ARROW_IMAGE = ImageUtils.scaleSquare(
                ImageUtils.loadOrReturnEmpty("rightarrow.png"),
                MinigameToolbar.getToolbarHeight() * 3 / 4
        );
    }

    MinigameRightButton(MinigameDisplayContainer displayContainer) {
        this.minigameDisplayContainer = displayContainer;
    }

    @Override
    protected int keyToToggleEvent() {
        return KeyEvent.VK_RIGHT;
    }

    @Override
    protected void updateDisplayContainer() {
        this.minigameDisplayContainer.rotate(false);
    }

    @Override
    public BufferedImage getIcon(IMiniGamePlugin plugin) {
        return RIGHT_ARROW_IMAGE;
    }
}
