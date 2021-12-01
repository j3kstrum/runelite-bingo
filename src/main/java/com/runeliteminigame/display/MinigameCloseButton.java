package com.runeliteminigame.display;

import com.runeliteminigame.IMiniGamePlugin;
import com.runeliteminigame.IMinigameInputHandler;
import com.runeliteminigames.util.ImageUtils;
import net.runelite.client.game.SpriteManager;

import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import static net.runelite.api.SpriteID.WINDOW_CLOSE_BUTTON_RED_X;
import static net.runelite.api.SpriteID.WINDOW_CLOSE_BUTTON_RED_X_HOVERED;

class MinigameCloseButton implements IDisplayableWithIcon, IMinigameInputHandler {

    private final SpriteManager spriteManager;

    private boolean isCloseButtonHovered;
    private final MinigameDisplayContainer minigameDisplayContainer;

    MinigameCloseButton(MinigameDisplayContainer minigameDisplayContainer, SpriteManager manager) {
        this.minigameDisplayContainer = minigameDisplayContainer;
        this.spriteManager = manager;
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

    private boolean isWithinCloseButton(Point p) {
        int minimum = MinigameToolbar.getToolbarHeight() / 8;
        int maximum = MinigameToolbar.getToolbarHeight() - minimum;
        return p.x >= minimum && p.x <= maximum && p.y >= minimum && p.y <= maximum;
    }

    @Override
    public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event, Point relativeOffset) {
        return event;
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event, Point relativeOffset) {
        if (SwingUtilities.isLeftMouseButton(event) && isWithinCloseButton(relativeOffset)) {
            this.minigameDisplayContainer.closeOverlay();
            event.consume();
            return event;
        }

        return event;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event, Point relativeOffset) {
        return event;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event, Point relativeOffset) {
        this.isCloseButtonHovered = isWithinCloseButton(relativeOffset);
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

    @Override
    public BufferedImage getIcon(IMiniGamePlugin plugin) {
        if (isCloseButtonHovered) {
            return ImageUtils.scaleSquare(
                    this.spriteManager.getSprite(WINDOW_CLOSE_BUTTON_RED_X_HOVERED, 0),
                    MinigameToolbar.getToolbarHeight()
            );
        } else {
            return ImageUtils.scaleSquare(
                    this.spriteManager.getSprite(WINDOW_CLOSE_BUTTON_RED_X, 0),
                    MinigameToolbar.getToolbarHeight()
            );
        }
    }
}
