package com.runeliteminigame.display;

import com.runelitebingo.SinglePlayerBingoGame;
import com.runeliteminigame.IDisplayableMinigame;
import com.runeliteminigame.IMinigamePlugin;
import com.runeliteminigame.util.ImageUtils;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.SpriteID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetMenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.BackgroundComponent;

import javax.inject.Singleton;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
public class MinigameDisplayContainer extends Overlay {

    private static final int TOOLBAR_HEIGHT = 40;
    private static final int WIDGET_HEIGHT = 360;
    private static final int TOTAL_TILES = 12;
    // We have left arrow, right arrow, add new button, settings icon, and room for the close interface icon.
    private static final int MAX_RENDERABLE_TILES = TOTAL_TILES - 5;
    private static final int WIDGET_WIDTH = TOOLBAR_HEIGHT * TOTAL_TILES;
    private static final int IMG_WIDTH = WIDGET_WIDTH;
    private static final int IMG_HEIGHT = TOOLBAR_HEIGHT + WIDGET_HEIGHT;

    private static final BufferedImage LEFT_ARROW_IMAGE;
    private static final BufferedImage RIGHT_ARROW_IMAGE;

    static {
        LEFT_ARROW_IMAGE = ImageUtils.scaleSquare(
                ImageUtils.loadOrReturnEmpty("leftarrow.png"),
                TOOLBAR_HEIGHT * 3 / 4
        );
        RIGHT_ARROW_IMAGE = ImageUtils.scaleSquare(
                ImageUtils.loadOrReturnEmpty("rightarrow.png"),
                TOOLBAR_HEIGHT * 3 / 4
        );
    }

    private boolean redraw;
    private final Client client;
    private final IMinigamePlugin plugin;
    private final MinigameCloseButtonHandler closeButtonHandler;
    private final MinigameInputListener inputListener;
    private MenuManager menuManager;

    private final ArrayList<IDisplayableMinigame> loadedMinigames = new ArrayList<>();
    // current minigame index - index of the minigame being displayed right now.
    private int currentMinigameIndex = 0;
    // first tab index - index of the first tab on the screen.
    private int firstTabIndex = 0;
    private KeyManager keyManager;
    private MouseManager mouseManager;

    private volatile boolean showOverlay = false;
    private final BackgroundComponent backgroundComponent = new BackgroundComponent();

    private final WidgetMenuOption menuOption = new WidgetMenuOption("Show", "Bingo Board", WidgetInfo.MINIMAP_WORLDMAP_OPTIONS);

    private BufferedImage cachedImage = null;

    @Setter
    private boolean isCloseButtonHovered;

    @Getter
    private Rectangle closeButtonBounds;

    public MinigameDisplayContainer(Client client, IMinigamePlugin plugin) {
        this.inputListener = new MinigameInputListener(this);
        this.client = client;
        this.plugin = plugin;
        this.closeButtonHandler = new MinigameCloseButtonHandler(plugin.getSpriteManager());
        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.TOP_CENTER);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        backgroundComponent.setFill(true);
        this.registerInputListener(plugin.getKeyManager(), plugin.getMouseManager());
        this.addCustomOptions(plugin.getMenuManager());
    }

    private void registerInputListener(KeyManager keyManager, MouseManager mouseManager) {
        this.unregisterInputListeners();
        keyManager.registerKeyListener(inputListener);
        mouseManager.registerMouseListener(inputListener);
        mouseManager.registerMouseWheelListener(inputListener);
        this.keyManager = keyManager;
        this.mouseManager = mouseManager;
    }

    private void unregisterInputListeners() {
        if (this.keyManager != null) {
            this.keyManager.unregisterKeyListener(inputListener);
            this.keyManager = null;
        }
        if (this.mouseManager != null) {
            this.mouseManager.unregisterMouseListener(inputListener);
            this.mouseManager.unregisterMouseWheelListener(inputListener);
            this.mouseManager = null;
        }
    }

    private void addCustomOptions(MenuManager menuManager)
    {
        menuManager.addManagedCustomMenu(this.menuOption);
        this.menuManager = menuManager;
    }

    private void removeCustomOptions()
    {
        if (this.menuManager != null) {
            this.menuManager.removeManagedCustomMenu(this.menuOption);
        }
        this.menuManager = null;
    }

    private boolean clickedOptionEquals(WidgetMenuOptionClicked event, WidgetMenuOption widgetMenuOption)
    {
        return event.getMenuOption().equals(widgetMenuOption.getMenuOption()) && event.getMenuTarget().equals(widgetMenuOption.getMenuTarget());
    }

    /**
     * Setter for showing the map. When the map is set to show, the map is
     * re-rendered
     *
     * @param show Whether or not the map should be shown.
     */
    private synchronized void setDisplayOverlay(boolean show) {
        showOverlay = show;
        redraw = true;
    }

    boolean isOverlayShown() {
        return showOverlay;
    }

    public void shutDown() {
        this.closeOverlay();
        this.unregisterInputListeners();
        this.removeCustomOptions();
    }

    public void requestRedraw() {
        this.redraw = true;
    }

    public void onWidgetMenuOptionClicked(WidgetMenuOptionClicked event)
    {
        if (event.getWidget() != WidgetInfo.MINIMAP_WORLDMAP_OPTIONS)
        {
            return;
        }

        if (clickedOptionEquals(event, this.menuOption))
        {
            if (this.isOverlayShown())
            {
                closeOverlay();
            }
            else
            {
                openOverlay();
            }
        }
    }

    public void openOverlay()
    {
        this.setDisplayOverlay(true);
        this.menuOption.setMenuOption("Hide");
    }

    public void closeOverlay()
    {
        this.setDisplayOverlay(false);
        this.menuOption.setMenuOption("Show");
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!showOverlay) {
            return null;
        }

        if (redraw || cachedImage == null) {
            BufferedImage image = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            BufferedImage minigameImage = this.loadedMinigames.get(this.currentMinigameIndex).getGameImage(this.plugin);
            minigameImage = ImageUtils.scale(minigameImage, WIDGET_WIDTH, WIDGET_HEIGHT);
            image.getGraphics().drawImage(minigameImage, 0, TOOLBAR_HEIGHT, null);

            BufferedImage imageWithToolbar = this.drawToolbar(image);

            synchronized (this) {
                cachedImage = imageWithToolbar;
            }

            redraw = false;
        }

        BufferedImage closeButton = closeButtonHandler.getCloseButtonImage();
        BufferedImage closeButtonHover = closeButtonHandler.getCloseButtonHoveredImage();
        if (closeButton != null && closeButtonBounds == null) {
            closeButtonBounds = new Rectangle(cachedImage.getWidth() - closeButton.getWidth() - 5, 6,
                    closeButton.getWidth(), closeButton.getHeight());
        }

        backgroundComponent.setRectangle(new Rectangle(-5, -5, cachedImage.getWidth() + 10, cachedImage.getHeight() + 10));
        backgroundComponent.render(graphics);
        graphics.drawImage(cachedImage, 0, 0, null);

        if (isCloseButtonHovered) {
            closeButton = closeButtonHover;
        }

        if (closeButton != null) {
            graphics.drawImage(closeButton, (int) closeButtonBounds.getX(), (int) closeButtonBounds.getY(), null);
        }

        return new Dimension(cachedImage.getWidth(), cachedImage.getHeight());
    }

    private BufferedImage drawToolbar(BufferedImage image) {

        BufferedImage bankTabSelectedImage = this.plugin.getSpriteManager().getSprite(SpriteID.BANK_TAB_SELECTED, 0);
        BufferedImage bankTabEmptyImage = this.plugin.getSpriteManager().getSprite(SpriteID.BANK_TAB_EMPTY, 0);

        // Draw the left arrow.
        image.getGraphics().drawImage(ImageUtils.scaleSquare(bankTabEmptyImage, TOOLBAR_HEIGHT), 0, 0, null);
        image.getGraphics().drawImage(LEFT_ARROW_IMAGE, TOOLBAR_HEIGHT / 8, TOOLBAR_HEIGHT / 8, null);

        int renderTiles = Math.min(MAX_RENDERABLE_TILES, this.loadedMinigames.size());
        for (int tilePosition = 0; tilePosition < renderTiles; tilePosition += 1) {
            // We start at the first tab index, then wrap around the array until we exhaust it OR get all MAX_RENDERABLE_TILES drawn.

            BufferedImage backgroundImage;
            if (tilePosition == this.currentMinigameIndex) {
                // Draw the "selected" background.
                backgroundImage = bankTabSelectedImage;
            }
            else {
                // Draw the "empty" background.
                // TODO: The "hovered" background.
                backgroundImage = bankTabEmptyImage;
            }
            backgroundImage = ImageUtils.scaleSquare(backgroundImage, TOOLBAR_HEIGHT);
            image.getGraphics().drawImage(backgroundImage, TOOLBAR_HEIGHT * (1 + tilePosition), 0, null);

            BufferedImage minigameTile = this.loadedMinigames.get((tilePosition + this.firstTabIndex) % this.loadedMinigames.size()).getIcon(this.plugin);
            minigameTile = ImageUtils.scaleSquare(minigameTile, TOOLBAR_HEIGHT * 3 / 4);
            image.getGraphics().drawImage(minigameTile, TOOLBAR_HEIGHT * (1 + tilePosition) + TOOLBAR_HEIGHT / 8, TOOLBAR_HEIGHT / 8, null);
        }

        // Draw the backgrounds for all tabs, regardless of whether or not they're selected.
        BufferedImage scaledBankTabEmptyImage = ImageUtils.scaleSquare(bankTabEmptyImage, TOOLBAR_HEIGHT);
        for (int tilePosition = renderTiles + 1; tilePosition < MAX_RENDERABLE_TILES + 1; tilePosition += 1) {
            image.getGraphics().drawImage(scaledBankTabEmptyImage, TOOLBAR_HEIGHT * tilePosition, 0, null);
        }

        int tilePosition = MAX_RENDERABLE_TILES + 1;
        // Draw the right arrow.
        image.getGraphics().drawImage(ImageUtils.scaleSquare(bankTabEmptyImage, TOOLBAR_HEIGHT), TOOLBAR_HEIGHT * tilePosition, 0, null);
        image.getGraphics().drawImage(RIGHT_ARROW_IMAGE, TOOLBAR_HEIGHT * tilePosition + TOOLBAR_HEIGHT / 8, TOOLBAR_HEIGHT / 8, null);

        tilePosition += 1;
        // Draw add button.
        image.getGraphics().drawImage(ImageUtils.scaleSquare(bankTabEmptyImage, TOOLBAR_HEIGHT), TOOLBAR_HEIGHT * tilePosition, 0, null);
        image.getGraphics().drawImage(
                ImageUtils.scaleSquare(
                        this.plugin.getSpriteManager().getSprite(SpriteID.BANK_ADD_TAB_ICON, 0),
                        TOOLBAR_HEIGHT * 3 / 4
                ),
                TOOLBAR_HEIGHT * tilePosition + TOOLBAR_HEIGHT / 8,
                TOOLBAR_HEIGHT / 8,
                null
        );

        tilePosition += 1;
        // Draw the settings.
        image.getGraphics().drawImage(ImageUtils.scaleSquare(bankTabEmptyImage, TOOLBAR_HEIGHT), TOOLBAR_HEIGHT * tilePosition, 0, null);
        image.getGraphics().drawImage(
                ImageUtils.scaleSquare(
                        this.plugin.getSpriteManager().getSprite(SpriteID.BANK_SHOW_MENU_ICON, 0),
                        TOOLBAR_HEIGHT * 3 / 4
                ),
                TOOLBAR_HEIGHT * tilePosition + TOOLBAR_HEIGHT / 8,
                TOOLBAR_HEIGHT / 8,
                null
        );

        return image;
    }

    /**
     * Handles game state changes and re-draws the map
     */
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        redraw = true;
        if (gameStateChanged.getGameState().equals(GameState.LOGGED_IN)) {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Initializing example bingo game.", null);
            loadedMinigames.add(SinglePlayerBingoGame.createGame(null, plugin));
            currentMinigameIndex = 0;
        }
    }

}

