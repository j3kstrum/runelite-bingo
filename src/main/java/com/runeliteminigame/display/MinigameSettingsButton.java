package com.runeliteminigame.display;

import com.runeliteminigame.IMinigamePlugin;
import net.runelite.api.ItemID;

import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

public class MinigameSettingsButton implements IDisplayableWithIcon, IMinigameInputHandler {

    // Used (in the future) to tell the display container to render the settings pane.
    private final MinigameDisplayContainer minigameDisplayContainer;

    MinigameSettingsButton(MinigameDisplayContainer minigameDisplayContainer) {
        this.minigameDisplayContainer = minigameDisplayContainer;
    }

    @Override
    public BufferedImage getIcon(IMinigamePlugin plugin) {
        return plugin.getItemManager().getImage(ItemID.GEAR);
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
            this.minigameDisplayContainer.showSettings();
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
