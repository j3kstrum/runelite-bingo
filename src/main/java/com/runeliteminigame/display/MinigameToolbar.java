package com.runeliteminigame.display;

import com.runeliteminigame.IMinigameInputHandler;
import com.runeliteminigames.util.ImageUtils;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class MinigameToolbar implements IMinigameInputHandler {


    private final SpriteManager spriteManager;

    private final MinigameDisplayContainer displayContainer;

    private final MinigameLeftButton leftButton;
    private final MinigameRightButton rightButton;
    private final MinigameCloseButton closeButton;
    private final MinigameSettingsButton settingsButton;
    private final MinigameAddButton addButton;

    private Point previousRelativePoint = new Point(-1, -1);
    private int hoveredTile;

    public MinigameToolbar(SpriteManager spriteManager, MinigameDisplayContainer displayContainer) {
        this.displayContainer = displayContainer;
        this.spriteManager = spriteManager;
        this.leftButton = new MinigameLeftButton(displayContainer);
        this.rightButton = new MinigameRightButton(displayContainer);
        this.closeButton = new MinigameCloseButton(displayContainer, spriteManager);
        this.settingsButton = new MinigameSettingsButton(displayContainer);
        this.addButton = new MinigameAddButton(displayContainer);

        this.hoveredTile = -1;
    }

    @Override
    public void keyTyped(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            this.leftButton.keyTyped(event);
            event.consume();
        } else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            this.rightButton.keyTyped(event);
            event.consume();
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        this.addButton.keyPressed(event);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        this.addButton.keyReleased(event);
    }

    private boolean overlayContains(Point offsetLocation) {
        return offsetLocation.x >= 0 && offsetLocation.x < TOOLBAR_HEIGHT * TOTAL_TILES && offsetLocation.y >= 0 && offsetLocation.y < TOOLBAR_HEIGHT;
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event, Point relativeOffset) {
        if (overlayContains(relativeOffset)) {
            // If we click on a minigame tile, set it to the active tile.
            RelativeMinigameComponentStruct passThroughCurrent = this.getSubComponentAtPoint(relativeOffset);
            if (passThroughCurrent.isValid()) {
                event = passThroughCurrent.handler.mouseClicked(event, passThroughCurrent.offset);
                if (passThroughCurrent.handler instanceof IDisplayableMinigame) {
                    if (this.displayContainer.trySetActive((IDisplayableMinigame) passThroughCurrent.handler)) {
                        // If we were able to set the minigame as the active one, consume the event.
                        event.consume();
                    }
                }
                this.plugin.requestRedraw();
            }
        }
        return event;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event, Point relativeOffset) {
        return event;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event, Point relativeOffset) {
        if (overlayContains(relativeOffset) || overlayContains(previousRelativePoint)) {
            if (overlayContains(relativeOffset)) {
                this.hoveredTile = relativeOffset.x / TOOLBAR_HEIGHT;
            } else {
                this.hoveredTile = -1;
            }
            RelativeMinigameComponentStruct passThroughCurrent = this.getSubComponentAtPoint(relativeOffset);
            RelativeMinigameComponentStruct passThroughPrevious = this.getSubComponentAtPoint(previousRelativePoint);
            if (passThroughCurrent.isValid()) {
                event = passThroughCurrent.handler.mouseMoved(event, passThroughCurrent.offset);
                this.plugin.requestRedraw();
            }
            if (passThroughPrevious.isValid()) {

                if (passThroughPrevious.handler == passThroughCurrent.handler) {
                    event = passThroughPrevious.handler.mouseMoved(event, passThroughCurrent.offset);
                }
                else {
                    // If the handlers are different, indicate that we moved away.
                    // The current pass through struct won't help us, because the offset is relative to that component
                    // and we don't have a reference to the previous component or the new offset for the old component.
                    event = passThroughPrevious.handler.mouseMoved(event, new Point(-1, -1));
                }
                this.plugin.requestRedraw();
            }
            this.previousRelativePoint = relativeOffset;
        }
        return event;
    }

    @Override
    public MouseEvent mouseDragged(MouseEvent event, Point relativeOffset) {
        // TODO: In the future, we may want to consider re-ordering elements with click and drag.
        return this.mouseMoved(event, relativeOffset);
    }

    @Override
    public RelativeMinigameComponentStruct getSubComponentAtPoint(Point relativeOffset) {
        RelativeMinigameComponentStruct result = new RelativeMinigameComponentStruct();
        int index = relativeOffset.x / TOOLBAR_HEIGHT;
        result.offset = new Point(relativeOffset.x % TOOLBAR_HEIGHT, relativeOffset.y);
        IMinigameInputHandler internalHandler;
        switch (index) {
            case 0:
                internalHandler = leftButton;
                break;
            case TOTAL_TILES - 1:
                internalHandler = closeButton;
                break;
            case TOTAL_TILES - 2:
                internalHandler = settingsButton;
                break;
            case TOTAL_TILES - 3:
                internalHandler = addButton;
                break;
            case TOTAL_TILES - 4:
                internalHandler = rightButton;
                break;
            default:
                internalHandler = this.displayContainer.getDisplayedMinigame(index - 1);
                break;
        }
        result.handler = internalHandler;
        return result;
    }
}
