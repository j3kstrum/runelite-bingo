package com.runeliteminigame.display;

import com.runeliteminigame.IMiniGamePlugin;
import com.runeliteminigame.util.ImageUtils;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class MinigameLeftButton extends BaseMinigameDirectionalButton {

    private final MinigameDisplayContainer minigameDisplayContainer;
    private static final BufferedImage LEFT_ARROW_IMAGE;

    static {
        LEFT_ARROW_IMAGE = ImageUtils.scaleSquare(
                ImageUtils.loadOrReturnEmpty("leftarrow.png"),
                MinigameToolbar.getToolbarHeight() * 3 / 4
        );
    }

    MinigameLeftButton(MinigameDisplayContainer displayContainer) {
        this.minigameDisplayContainer = displayContainer;
    }

    @Override
    protected int keyToToggleEvent() {
        return KeyEvent.VK_LEFT;
    }

    @Override
    protected void updateDisplayContainer() {
        this.minigameDisplayContainer.rotate(true);
    }

    @Override
    public BufferedImage getIcon(IMiniGamePlugin plugin) {
        return LEFT_ARROW_IMAGE;
    }
}
