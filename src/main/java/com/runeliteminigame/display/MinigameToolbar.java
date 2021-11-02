package com.runeliteminigame.display;

import com.runeliteminigame.IMinigamePlugin;
import com.runeliteminigame.util.ImageUtils;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class MinigameToolbar implements IMinigameInputHandler {

    private static final int TOOLBAR_HEIGHT = 40;
    private static final int TOTAL_TILES = 12;
    // We have left arrow, right arrow, add new button, settings icon, and room for the close interface icon.
    private static final int MAX_RENDERABLE_TILES = TOTAL_TILES - 5;

    private final SpriteManager spriteManager;

    private final IMinigamePlugin plugin;
    private final MinigameDisplayContainer displayContainer;

    private final MinigameLeftButton leftButton;
    private final MinigameRightButton rightButton;
    private final MinigameCloseButton closeButton;
    private final MinigameSettingsButton settingsButton;
    private final MinigameAddButton addButton;

    private Point previousRelativePoint = new Point(-1, -1);
    private int hoveredTile;

    public MinigameToolbar(IMinigamePlugin plugin, MinigameDisplayContainer displayContainer) {
        this.displayContainer = displayContainer;
        this.spriteManager = plugin.getSpriteManager();
        this.plugin = plugin;
        this.leftButton = new MinigameLeftButton(displayContainer);
        this.rightButton = new MinigameRightButton(displayContainer);
        this.closeButton = new MinigameCloseButton(displayContainer, plugin.getSpriteManager());
        this.settingsButton = new MinigameSettingsButton(displayContainer);
        this.addButton = new MinigameAddButton(displayContainer);

        this.hoveredTile = -1;
    }

    static int getToolbarHeight() {
        return TOOLBAR_HEIGHT;
    }

    static int getTotalTiles() {
        return TOTAL_TILES;
    }

    /**
     * Renders an image of the tile against the background for the specified displayable object.
     * @param displayable The object whose tile should be rendered.
     * @param isSelected Whether or not the specified object is currently selected.
     * @param isHovered Whether or not the specified object is currently hovered over.
     * @return An image with the displayable's icon placed over a background bank tab image.
     */
    private BufferedImage drawTile(IDisplayableWithIcon displayable, boolean isSelected, boolean isHovered) {
        if (displayable == null) {
            return ImageUtils.scaleSquare(
                    spriteManager.getSprite(SpriteID.BANK_TAB_EMPTY, 0),
                    TOOLBAR_HEIGHT
            );
        }
        @SuppressWarnings("SuspiciousNameCombination") BufferedImage image = new BufferedImage(TOOLBAR_HEIGHT, TOOLBAR_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        if (isSelected) {
            image.getGraphics().drawImage(
                    ImageUtils.scaleSquare(spriteManager.getSprite(SpriteID.BANK_TAB_SELECTED, 0), TOOLBAR_HEIGHT),
                    0,
                    0,
                    null
            );
        } else if (isHovered) {
            image.getGraphics().drawImage(
                    ImageUtils.scaleSquare(
                            spriteManager.getSprite(SpriteID.BANK_TAB_HOVERED, 0),
                            TOOLBAR_HEIGHT
                    ),
                    0,
                    0,
                    null
            );
        } else  {
            image.getGraphics().drawImage(
                    ImageUtils.scaleSquare(spriteManager.getSprite(SpriteID.BANK_TAB_EMPTY, 0), TOOLBAR_HEIGHT),
                    0,
                    0,
                    null
            );
        }
        BufferedImage tile = displayable.getIcon(this.plugin);
        tile = ImageUtils.scaleSquare(tile, TOOLBAR_HEIGHT * 3 / 4);
        image.getGraphics().drawImage(tile, TOOLBAR_HEIGHT / 8, TOOLBAR_HEIGHT / 8, null);
        return image;
    }

    BufferedImage drawToolbar(List<IDisplayableMinigame> loadedMinigames, int beginRenderAtPosition, int currentlySelectedMinigamePosition) {

        BufferedImage image = new BufferedImage(TOOLBAR_HEIGHT * TOTAL_TILES, TOOLBAR_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        // Draw the left arrow.
        BufferedImage leftArrow = this.drawTile(leftButton, false, hoveredTile == 0);
        image.getGraphics().drawImage(leftArrow, 0, 0, null);

        for (int tilePosition = 0; tilePosition < MAX_RENDERABLE_TILES; tilePosition += 1) {
            // We start at the first tab index, then wrap around the array until we exhaust it OR get all MAX_RENDERABLE_TILES drawn.
            BufferedImage tile;
            if (tilePosition < loadedMinigames.size()) {
                IDisplayableMinigame minigame = loadedMinigames.get((tilePosition + beginRenderAtPosition) % loadedMinigames.size());
                tile = this.drawTile(
                        minigame,
                        // We're selected if the selected minigame position in the list is equal to this minigame's overall position.
                        ((tilePosition + beginRenderAtPosition) % loadedMinigames.size()) == currentlySelectedMinigamePosition,
                        // We're starting from position 1 in the iteration.
                        tilePosition + 1 == this.hoveredTile
                );
            }
            else {
                // Placeholder tiles, in case we have more spaces available than we do tiles to fill spaces with.
                tile = this.drawTile(null, false, false);
            }
            image.getGraphics().drawImage(tile, (tilePosition + 1) * TOOLBAR_HEIGHT, 0, null);
        }

        int tilePosition = MAX_RENDERABLE_TILES + 1;
        // Draw the right arrow.
        BufferedImage rightArrow = this.drawTile(rightButton, false, hoveredTile == tilePosition);
        image.getGraphics().drawImage(rightArrow, tilePosition * TOOLBAR_HEIGHT, 0, null);

        tilePosition += 1;
        // Draw add button.
        BufferedImage addImage = this.drawTile(addButton, false, hoveredTile == tilePosition);
        image.getGraphics().drawImage(addImage, tilePosition * TOOLBAR_HEIGHT, 0, null);

        tilePosition += 1;
        // Draw the settings.
        // TODO: Enable the settings button and allow it to be selected.
        BufferedImage settingsImage = this.drawTile(settingsButton, false, hoveredTile == tilePosition);
        image.getGraphics().drawImage(settingsImage, tilePosition * TOOLBAR_HEIGHT, 0, null);

        tilePosition += 1;
        // Draw the close button.
        // We never hover over the back tab of the closeButton tile, only over the X.
        BufferedImage closeImage = this.drawTile(closeButton, false, false);
        image.getGraphics().drawImage(closeImage, tilePosition * TOOLBAR_HEIGHT, 0, null);

        return image;
    }

    @Override
    public void keyTyped(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_LEFT) {
            this.leftButton.keyTyped(event);
        } else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
            this.rightButton.keyTyped(event);
        }
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
