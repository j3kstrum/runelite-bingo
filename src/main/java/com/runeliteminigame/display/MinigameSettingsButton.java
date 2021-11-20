package com.runeliteminigame.display;

import com.runeliteminigame.IMiniGamePlugin;
import com.runeliteminigame.IMinigameInputHandler;
import net.runelite.api.SpriteID;

import javax.swing.SwingUtilities;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
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
    public BufferedImage getIcon(IMiniGamePlugin plugin) {
        BufferedImage sprite = plugin.getSpriteManager().getSprite(SpriteID.TAB_OPTIONS, 0);
        // Temporarily gray out the icon, since settings aren't yet supported (Issue #24).
        assert sprite != null;
        BufferedImage output = new BufferedImage(sprite.getWidth(), sprite.getHeight(), BufferedImage.TYPE_INT_ARGB);
        AlphaComposite dimmer = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
        Graphics2D graphics = output.createGraphics();
        graphics.setComposite(dimmer);
        graphics.drawImage(sprite, 0, 0, null);
        return output;
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
    public MouseEvent mouseDragged(MouseEvent event, Point relativeOffset) {
        return this.mouseMoved(event, relativeOffset);
    }

    @Override
    public RelativeMinigameComponentStruct getSubComponentAtPoint(Point relativeOffset) {
        return new RelativeMinigameComponentStruct();
    }
}
