package com.runeliteminigame.display;

import com.runeliteminigame.IMiniGamePlugin;
import com.runeliteminigame.IMinigameInputHandler;
import net.runelite.api.SpriteID;

import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

public class MinigameAddButton implements IDisplayableWithIcon, IMinigameInputHandler {

    private final MinigameDisplayContainer minigameDisplayContainer;
    private boolean shouldAdd = true;

    MinigameAddButton(MinigameDisplayContainer minigameDisplayContainer) {
        this.minigameDisplayContainer = minigameDisplayContainer;
    }

    @Override
    public BufferedImage getIcon(IMiniGamePlugin plugin) {
        if (this.shouldAdd) {
            return plugin.getSpriteManager().getSprite(SpriteID.BANK_ADD_TAB_ICON, 0);
        }
        else {
            return plugin.getSpriteManager().getSprite(SpriteID.WINDOW_CLOSE_BUTTON_BROWN_X, 0);
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {

    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_SHIFT && this.shouldAdd) {
            shouldAdd = false;
            this.minigameDisplayContainer.requestRedraw();
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_SHIFT && !this.shouldAdd) {
            shouldAdd = true;
            this.minigameDisplayContainer.requestRedraw();
        }
    }

    @Override
    public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event, Point relativeOffset) {
        return event;
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event, Point relativeOffset) {
        if (SwingUtilities.isLeftMouseButton(event)) {
            if (this.shouldAdd) {
                this.minigameDisplayContainer.addMinigame();
            }
            else {
                this.minigameDisplayContainer.promptDelete();
            }
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
    public MouseEvent mouseDragged(MouseEvent event, Point relativeOffset) {
        return this.mouseMoved(event, relativeOffset);
    }

    @Override
    public RelativeMinigameComponentStruct getSubComponentAtPoint(Point relativeOffset) {
        return new RelativeMinigameComponentStruct();
    }
}
