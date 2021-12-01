package com.runeliteminigame.display;

import com.runelitebingo.SinglePlayerBingoGame;
import com.runeliteminigame.IMinigame;
import com.runeliteminigame.IMinigameInputHandler;
import com.runeliteminigames.util.ImageUtils;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.ui.overlay.Overlay;

import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Class designed to hold the graphics for sub minigames.
 *
 * Also holds a top-level bar that allows for creating new minigames, configuring settings, and
 * removing/archiving games from the bar.
 *
 * (Archiving detaches a game from the display until it's re-added).
 */
@Singleton
public class MinigameDisplayContainer extends Overlay implements IMinigameInputHandler {

    private boolean redraw;

    private final ArrayList<IDisplayableMinigame> loadedMinigames = new ArrayList<>();
    // current minigame index - index of the minigame being displayed right now.
    private int currentMinigameIndex = 0;
    // first tab index - index of the first tab on the screen.
    private int firstTabIndex = 0;
    private KeyManager keyManager;
    private MouseManager mouseManager;
    private final MinigameToolbar minigameToolbar;

    private volatile boolean showOverlay = false;

    private BufferedImage cachedImage = null;

    private Point previousRelativePoint = new Point(0, 0);

    public MinigameDisplayContainer() {
        this.minigameToolbar = new MinigameToolbar(plugin, this);
    }

    boolean isOverlayShown() {
        return showOverlay;
    }

    public void requestRedraw() {
        this.redraw = true;
    }

    void showSettings() {
        System.out.println("Minigame settings are not yet available.");
    }

    void addMinigame() {
        // TODO: To future-proof, would probably need to have a "BaseMinigame".
        // Its sole purpose would be to select a new minigame type from all candidates.
        // It would allow for the user to specify constraints for the new game.
        this.loadedMinigames.add(SinglePlayerBingoGame.createGame(null));
    }

    void promptDelete() {
        if (this.loadedMinigames.size() == 0) {
            return;
        }
        this.loadedMinigames.get(this.currentMinigameIndex).promptDelete();
    }

    boolean trySetActive(IDisplayableMinigame game) {
        if (!this.loadedMinigames.contains(game)) {
            System.out.println("Error: could not find minigame in loaded games list.");
            return false;
        }
        if (this.loadedMinigames.indexOf(game) != this.currentMinigameIndex) {
            this.currentMinigameIndex = this.loadedMinigames.indexOf(game);
            this.requestRedraw();
            return true;
        }
        return true;
    }

    IDisplayableMinigame getDisplayedMinigame(int offset) {
        if (offset >= this.loadedMinigames.size()) {
            // If the index is longer than our minigame list, it's out of bounds.
            return null;
        }
        // However, it could be wrapped if it's within the proper range. Unwrap to absolute.
        int absoluteIndex = (this.firstTabIndex + offset) % this.loadedMinigames.size();
        return this.loadedMinigames.get(absoluteIndex);
    }

    private boolean overlayContains(final Point offsetPoint) {
        return offsetPoint.x < IMG_WIDTH && offsetPoint.y < IMG_HEIGHT;
    }

    private void removeDeletedMinigames() {
        this.loadedMinigames.removeIf(IMinigame::shouldDelete);
        // Rewind first and current minigame indices to ensure they're in bounds (or at the defaults if empty).
        this.firstTabIndex = Math.max(0, Math.min(this.firstTabIndex, loadedMinigames.size() - 1));
        this.currentMinigameIndex = Math.max(0, Math.min(this.currentMinigameIndex, loadedMinigames.size() - 1));
    }

    @Override
    public void keyTyped(KeyEvent event) {
        if (this.isOverlayShown()) {
            this.minigameToolbar.keyTyped(event);
            if (this.loadedMinigames.size() > 0) {
                this.loadedMinigames.get(currentMinigameIndex).keyTyped(event);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (this.isOverlayShown()) {
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                this.closeOverlay();
                event.consume();
            }
            else {
                this.minigameToolbar.keyPressed(event);
                if (this.loadedMinigames.size() > 0) {
                    this.loadedMinigames.get(currentMinigameIndex).keyPressed(event);
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        if (this.isOverlayShown()) {
            this.minigameToolbar.keyReleased(event);
            if (this.loadedMinigames.size() > 0) {
                this.loadedMinigames.get(currentMinigameIndex).keyReleased(event);
            }

            if (this.loadedMinigames.size() > 0 && event.getKeyCode() == KeyEvent.VK_DELETE) {
                this.removeDeletedMinigames();
            }
        }
    }

    @Override
    public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event, Point relativeOffset) {
        // TODO: In the future, we may want to capture the mouse wheel for zooming in.
        // But for now, just pass it to the underlying UI.
        return event;
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event, Point relativeOffset) {
        if (relativeOffset == null) {
            relativeOffset = this.getBounds().getLocation();
        }
        if (this.isOverlayShown()) {
            Point currentLocation = event.getPoint();
            Point offsetLocation = new Point(currentLocation.x - relativeOffset.x, currentLocation.y - relativeOffset.y);
            if (overlayContains(offsetLocation)) {
                event.consume();
                RelativeMinigameComponentStruct passThroughCurrent = this.getSubComponentAtPoint(offsetLocation);
                if (passThroughCurrent.isValid()) {
                    event = passThroughCurrent.handler.mouseClicked(event, passThroughCurrent.offset);
                }
            }
        }

        return event;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event, Point relativeOffset) {
        if (relativeOffset == null) {
            relativeOffset = this.getBounds().getLocation();
        }
        if (this.isOverlayShown() && !SwingUtilities.isMiddleMouseButton(event)) {
            Point currentLocation = event.getPoint();
            Point offsetLocation = new Point(currentLocation.x - relativeOffset.x, currentLocation.y - relativeOffset.y);
            if (overlayContains(offsetLocation)) {
                event.consume();
                RelativeMinigameComponentStruct passThroughCurrent = this.getSubComponentAtPoint(offsetLocation);
                if (passThroughCurrent.isValid()) {
                    event = passThroughCurrent.handler.mousePressed(event, passThroughCurrent.offset);
                }
            }
        }

        return event;
    }

    @Override
    public MouseEvent mouseDragged(MouseEvent event, Point relativeOffset) {
        // If middle mouse is clicked, we still want the UI to be able to move/rotate.
        if (SwingUtilities.isMiddleMouseButton(event)) {
            // We clone the event so that we don't consume the original and can pass it to the main game.
            this.mouseMoved(
                    new MouseEvent((Component)event.getSource(), event.getID(), event.getWhen(), event.getModifiersEx(), event.getX(), event.getY(), event.getClickCount(), event.isPopupTrigger(), event.getButton()),
                    relativeOffset
            );
            return event;
        }

        // TODO: If we ever decide to do any fancy reordering, this would be the place.
        // For now, a "drag" is just a fancy mouse movement so that we don't disrupt the UI's graphics.
        return this.mouseMoved(event, relativeOffset);
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event, Point relativeOffset) {
        if (relativeOffset == null) {
            relativeOffset = this.getBounds().getLocation();
        }
        if (this.isOverlayShown()) {
            Point currentLocation = event.getPoint();
            Point offsetLocation = new Point(currentLocation.x - relativeOffset.x, currentLocation.y - relativeOffset.y);
            if (overlayContains(offsetLocation) || overlayContains(previousRelativePoint)) {
                RelativeMinigameComponentStruct passThroughCurrent = this.getSubComponentAtPoint(offsetLocation);
                RelativeMinigameComponentStruct passThroughPrevious = this.getSubComponentAtPoint(previousRelativePoint);
                if (passThroughCurrent.isValid()) {
                    event = passThroughCurrent.handler.mouseMoved(event, passThroughCurrent.offset);
                }
                if (passThroughPrevious.isValid() && passThroughCurrent.offset != null) {
                    if (passThroughPrevious.handler == passThroughCurrent.handler) {
                        event = passThroughPrevious.handler.mouseMoved(event, passThroughCurrent.offset);
                    }
                    else {
                        // If the handlers are different, indicate that we moved away.
                        // The current pass through struct won't help us, because the offset is relative to that component
                        // and we don't have a reference to the previous component or the new offset for the old component.
                        event = passThroughPrevious.handler.mouseMoved(event, new Point(-1, -1));
                    }
                }
                this.previousRelativePoint = offsetLocation;
            }
            if (overlayContains(offsetLocation)) {
                event.consume();
            }
        }

        return event;
    }

    @Override
    public RelativeMinigameComponentStruct getSubComponentAtPoint(Point relativeOffset) {
        // The minigame display container consists of the toolbar and the game itself.
        RelativeMinigameComponentStruct struct = new RelativeMinigameComponentStruct();
        if (relativeOffset.x > this.getBounds().width) {
            return struct;
        }
        if (relativeOffset.y < MinigameToolbar.getToolbarHeight()) {
            struct.handler = this.minigameToolbar;
            struct.offset = relativeOffset;
        }
        else {
            // Inside of the minigame box.
            if (this.loadedMinigames.size() > 0)
            {
                struct.handler = this.loadedMinigames.get(this.currentMinigameIndex);
                struct.offset = new Point(relativeOffset.x, relativeOffset.y - MinigameToolbar.getToolbarHeight());
            }
            else {
                struct.offset = new Point(relativeOffset.x, relativeOffset.y - MinigameToolbar.getToolbarHeight());
            }
        }
        return struct;
    }

}

