package com.runeliteminigame;

import com.runelitebingo.SinglePlayerBingoGame;
import com.runeliteminigame.util.ImageUtils;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.WidgetMenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.*;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.BackgroundComponent;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static net.runelite.api.SpriteID.WINDOW_CLOSE_BUTTON_RED_X;
import static net.runelite.api.SpriteID.WINDOW_CLOSE_BUTTON_RED_X_HOVERED;

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
    private static final int WIDGET_WIDTH = TOOLBAR_HEIGHT * 12;
    private static final int IMG_WIDTH = WIDGET_WIDTH;
    private static final int IMG_HEIGHT = TOOLBAR_HEIGHT + WIDGET_HEIGHT;

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
    private static final int MAX_RENDERABLE_TILES = 9;
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
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        backgroundComponent.setFill(true);
    }

    public void registerInputListener(KeyManager keyManager, MouseManager mouseManager) {
        this.unregisterInputListeners();
        keyManager.registerKeyListener(inputListener);
        mouseManager.registerMouseListener(inputListener);
        mouseManager.registerMouseWheelListener(inputListener);
        this.keyManager = keyManager;
        this.mouseManager = mouseManager;
    }

    public void unregisterInputListeners() {
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

    public void requestRedraw() {
        this.redraw = true;
    }

    public boolean isOverlayShown() {
        return showOverlay;
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

    public void addCustomOptions(MenuManager menuManager)
    {
        menuManager.addManagedCustomMenu(this.menuOption);
        this.menuManager = menuManager;
    }

    public void removeCustomOptions()
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
            // TODO: Draw the left arrow.
            int renderTiles = Math.min(MAX_RENDERABLE_TILES, this.loadedMinigames.size());
            for (int tilePosition = 0; tilePosition < renderTiles; tilePosition += 1) {
                // We start at the first tab index, then wrap around the array until we exhaust it OR get all MAX_RENDERABLE_TILES drawn.
                BufferedImage minigameTile = this.loadedMinigames.get((tilePosition + this.firstTabIndex) % this.loadedMinigames.size()).getIcon(this.plugin);
                minigameTile = ImageUtils.scaleSquare(minigameTile, TOOLBAR_HEIGHT);
                image.getGraphics().drawImage(minigameTile, TOOLBAR_HEIGHT * (1 + tilePosition), 0, null);
            }
            // TODO: Draw the right arrow.
            // TODO: Draw the settings.
            synchronized (this) {
                cachedImage = image;
            }

            redraw = false;
        }

        BufferedImage closeButton = closeButtonHandler.getCloseButtonImage();
        BufferedImage closeButtonHover = closeButtonHandler.getCloseButtonHoveredImage();
        if (closeButton != null && closeButtonBounds == null) {
            closeButtonBounds = new Rectangle(cachedImage.getWidth() - closeButton.getWidth() - 5, 6,
                    closeButton.getWidth(), closeButton.getHeight());
        }

        backgroundComponent.setRectangle(new Rectangle(0, 0, cachedImage.getWidth(), cachedImage.getHeight()));
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

class MinigameCloseButtonHandler {

    private BufferedImage closeButtonImage;
    private BufferedImage closeButtonHoveredImage;
    private final SpriteManager spriteManager;

    MinigameCloseButtonHandler(SpriteManager manager) {
        this.spriteManager = manager;
    }

    @Nullable
    BufferedImage getCloseButtonImage() {
        if (closeButtonImage == null) {
            closeButtonImage = spriteManager.getSprite(WINDOW_CLOSE_BUTTON_RED_X, 0);
        }
        return closeButtonImage;
    }

    @Nullable
    BufferedImage getCloseButtonHoveredImage() {
        if (closeButtonHoveredImage == null) {
            closeButtonHoveredImage = spriteManager.getSprite(WINDOW_CLOSE_BUTTON_RED_X_HOVERED, 0);
        }
        return closeButtonHoveredImage;
    }
}

class MinigameInputListener extends MouseAdapter implements KeyListener, MouseWheelListener
{
    private final MinigameDisplayContainer overlay;

    MinigameInputListener(MinigameDisplayContainer overlay) {
        this.overlay = overlay;
    }

    @Override
    public void keyTyped(KeyEvent event)
    {

    }

    @Override
    public void keyPressed(KeyEvent event)
    {
        if (!overlay.isOverlayShown())
        {
            return;
        }

        if (event.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
            overlay.closeOverlay();
            event.consume();
        }
    }

    @Override
    public void keyReleased(KeyEvent event)
    {

    }

    @Override
    public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event)
    {
        return event;
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event)
    {
        if (!overlay.isOverlayShown() || isNotWithinOverlay(event.getPoint()))
        {
            return event;
        }

        event.consume();
        return event;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event)
    {
        if (!overlay.isOverlayShown() || isNotWithinOverlay(event.getPoint()))
        {
            return event;
        }

        if (SwingUtilities.isLeftMouseButton(event) && isWithinCloseButton(event.getPoint()))
        {
            overlay.closeOverlay();
        }

        event.consume();
        return event;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event)
    {
        if (overlay.isOverlayShown())
        {
            overlay.setCloseButtonHovered(isWithinCloseButton(event.getPoint()));
        }

        return event;
    }

    private boolean isNotWithinOverlay(final Point point)
    {
        return !overlay.getBounds().contains(point);
    }

    private boolean isWithinCloseButton(final Point point)
    {
        Point overlayPoint = new Point(point.x - (int) overlay.getBounds().getX(),
                point.y - (int) overlay.getBounds().getY());

        return overlay.getCloseButtonBounds() != null
                && overlay.getCloseButtonBounds().contains(overlayPoint);
    }
}