package com.runeliteminigame.display;

import com.runeliteminigame.IMinigamePlugin;
import net.runelite.api.SpriteID;

import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

public class MinigameAddButton implements IDisplayableWithIcon, IMinigameInputHandler {

    private final MinigameDisplayContainer minigameDisplayContainer;

    MinigameAddButton(MinigameDisplayContainer minigameDisplayContainer) {
        this.minigameDisplayContainer = minigameDisplayContainer;
    }

    @Override
    public BufferedImage getIcon(IMinigamePlugin plugin) {
        return plugin.getSpriteManager().getSprite(SpriteID.BANK_ADD_TAB_ICON, 0);
    }

    @Override
    public void keyTyped(KeyEvent event) {

    }

    @Override
    public void keyPressed(KeyEvent event) {

    }

    @Override
    public void keyReleased(KeyEvent event) {

    }

    @Override
    public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event, Point relativeOffset) {
        return event;
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event, Point relativeOffset) {
        if (SwingUtilities.isLeftMouseButton(event)) {
            this.minigameDisplayContainer.addMinigame();
            event.consume();
        }
        return event;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event, Point relativeOffset) {
        return event;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event, Point relativeOffset) {
        return event;
    }

    @Override
    public RelativeMinigameComponentStruct getSubComponentAtPoint(Point relativeOffset) {
        return new RelativeMinigameComponentStruct();
    }
}
