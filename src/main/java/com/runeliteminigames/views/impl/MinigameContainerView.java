package com.runeliteminigames.views.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.runeliteminigames.models.impl.MinigameContainer;
import com.runeliteminigames.models.interf.IModel;
import com.runeliteminigames.util.ImageUtils;
import com.runeliteminigames.views.interf.IMinigameView;
import com.runeliteminigames.views.interf.IView;
import com.runeliteminigames.views.interf.IViewWithIcon;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.BackgroundComponent;
import org.apache.commons.lang3.NotImplementedException;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


@Singleton
public class MinigameContainerView extends Overlay implements IView {

    @Inject
    private SpriteManager spriteManager;

    // Toolbar constants
    private static final int TOOLBAR_HEIGHT = 40;
    private static final int TOTAL_TILES = 12;
    // We have left arrow, right arrow, add new button, settings icon, and room for the close interface icon.
    private static final int MAX_RENDERABLE_TILES = TOTAL_TILES - 5;

    private static final int WIDGET_HEIGHT = 360;
    private static final int WIDGET_WIDTH = TOOLBAR_HEIGHT * TOTAL_TILES;
    private static final int IMG_WIDTH = WIDGET_WIDTH;
    private static final int IMG_HEIGHT = TOOLBAR_HEIGHT + WIDGET_HEIGHT;

    private static final int TOOLBAR_WIDTH = WIDGET_WIDTH;

    private final List<IMinigameView> minigameViews;
    private IView selectedView;

    private boolean shouldRender = false;
    private boolean shouldRedraw = true;

    private BufferedImage cachedImage;

    private final BackgroundComponent backgroundComponent = new BackgroundComponent();

    public MinigameContainerView() {
        this.minigameViews = new ArrayList<>();

        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.TOP_CENTER);
        setLayer(OverlayLayer.ABOVE_WIDGETS);

        backgroundComponent.setFill(true);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!shouldRender) {
            return null;
        }

        if (cachedImage == null) {
            BufferedImage image = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);

            // Draw the core image under the toolbar, if one is available.
            if (selectedView != null && selectedView.image() != null) {
                BufferedImage mainImage = selectedView.image();
                mainImage = ImageUtils.scale(mainImage, WIDGET_WIDTH, WIDGET_HEIGHT);
                image.getGraphics().drawImage(mainImage, 0, TOOLBAR_HEIGHT, null);
            }

            BufferedImage toolbarImage = this.drawToolbar();
            image.getGraphics().drawImage(toolbarImage, 0, 0, null);

            synchronized (this) {
                cachedImage = image;
            }
        }

        backgroundComponent.setRectangle(new Rectangle(-5, -5, cachedImage.getWidth() + 10, cachedImage.getHeight() + 10));
        backgroundComponent.render(graphics);
        graphics.drawImage(cachedImage, 0, 0, null);

        return new Dimension(cachedImage.getWidth() + 10, cachedImage.getHeight() + 10);
    }

    /**
     * Renders an image of the tile against the background for the specified displayable object.
     * @param displayable The object whose tile should be rendered.
     * @param isSelected Whether or not the specified object is currently selected.
     * @param isHovered Whether or not the specified object is currently hovered over.
     * @return An image with the displayable's icon placed over a background bank tab image.
     */
    private BufferedImage drawTile(IViewWithIcon displayable, boolean isSelected, boolean isHovered) {
        throw new NotImplementedException("Have not yet translated to new interface.");
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

    private BufferedImage drawToolbar() {

        throw new NotImplementedException("Have not yet translated to new interface.");
        BufferedImage image = new BufferedImage(TOOLBAR_WIDTH, TOOLBAR_HEIGHT, BufferedImage.TYPE_INT_ARGB);

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
    public void update(IModel usingModel) {
        if (usingModel instanceof MinigameContainer) {
            throw new NotImplementedException("Not yet implemented.");
        }
        else {
            throw new IllegalArgumentException("Bad model type provided to MinigameContainerView.");
        }
    }
}
