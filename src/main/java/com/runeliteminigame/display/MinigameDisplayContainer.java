package com.runeliteminigame.display;

import com.runelitebingo.SinglePlayerBingoGame;
import com.runeliteminigame.IMinigamePlugin;
import com.runeliteminigame.util.ImageUtils;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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

    private static final int WIDGET_HEIGHT = 360;
    private static final int WIDGET_WIDTH = MinigameToolbar.getToolbarHeight() * MinigameToolbar.getTotalTiles();
    private static final int IMG_WIDTH = WIDGET_WIDTH;
    private static final int IMG_HEIGHT = MinigameToolbar.getToolbarHeight() + WIDGET_HEIGHT;

    private boolean redraw;
    private final Client client;
    private final IMinigamePlugin plugin;
    private final MinigameInputListener inputListener;
    private MenuManager menuManager;

    private final ArrayList<IDisplayableMinigame> loadedMinigames = new ArrayList<>();
    // current minigame index - index of the minigame being displayed right now.
    private int currentMinigameIndex = 0;
    // first tab index - index of the first tab on the screen.
    private int firstTabIndex = 0;
    private KeyManager keyManager;
    private MouseManager mouseManager;
    private final MinigameToolbar minigameToolbar;

    private volatile boolean showOverlay = false;
    private final BackgroundComponent backgroundComponent = new BackgroundComponent();

    private final WidgetMenuOption menuOption = new WidgetMenuOption("Show", "Bingo Board", WidgetInfo.MINIMAP_WORLDMAP_OPTIONS);

    private BufferedImage cachedImage = null;

    private Point previousRelativePoint = new Point(0, 0);

    public MinigameDisplayContainer(Client client, IMinigamePlugin plugin) {
        this.inputListener = new MinigameInputListener(this);
        this.client = client;
        this.plugin = plugin;
        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.TOP_CENTER);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        backgroundComponent.setFill(true);
        this.registerInputListener(plugin.getKeyManager(), plugin.getMouseManager());
        this.addCustomOptions(plugin.getMenuManager());
        this.minigameToolbar = new MinigameToolbar(plugin, this);
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

    void showSettings() {
        System.out.println("Minigame settings are not yet available.");
    }

    void addMinigame() {
        // TODO: To future-proof, would probably need to have a "BaseMinigame".
        // Its sole purpose would be to select a new minigame type from all candidates.
        // It would allow for the user to specify constraints for the new game.
        this.loadedMinigames.add(SinglePlayerBingoGame.createGame(null, this.plugin));
    }

    void trySetActive(IDisplayableMinigame game) {
        if (!this.loadedMinigames.contains(game)) {
            System.out.println("Error: could not find minigame in loaded games list.");
            return;
        }
        if (this.loadedMinigames.indexOf(game) != this.currentMinigameIndex) {
            this.currentMinigameIndex = this.loadedMinigames.indexOf(game);
            this.requestRedraw();
            // todo remove debug statement.
            System.out.println("current minigame index now set to " + this.currentMinigameIndex);
        }
    }

    IDisplayableMinigame getOffsetMinigame(int index) {
        if (index >= this.loadedMinigames.size()) {
            // If the index is longer than our minigame list, it's out of bounds.
            return null;
        }
        // However, it could be wrapped if it's within the proper range. Unwrap to absolute.
        int absoluteIndex = (this.currentMinigameIndex + index) % this.loadedMinigames.size();
        return this.loadedMinigames.get(absoluteIndex);
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

    private void openOverlay()
    {
        this.setDisplayOverlay(true);
        this.menuOption.setMenuOption("Hide");
    }

    void closeOverlay()
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
            BufferedImage minigameImage = this.loadedMinigames.get(this.currentMinigameIndex).getMainImage(this.plugin);
            minigameImage = ImageUtils.scale(minigameImage, WIDGET_WIDTH, WIDGET_HEIGHT);
            image.getGraphics().drawImage(minigameImage, 0, MinigameToolbar.getToolbarHeight(), null);

            BufferedImage toolbarImage = this.minigameToolbar.drawToolbar(
                    this.loadedMinigames, this.firstTabIndex, this.currentMinigameIndex
            );
            toolbarImage = ImageUtils.scale(toolbarImage, IMG_WIDTH, MinigameToolbar.getToolbarHeight());
            image.getGraphics().drawImage(toolbarImage, 0, 0, null);

            synchronized (this) {
                cachedImage = image;
            }

            redraw = false;
        }

        backgroundComponent.setRectangle(new Rectangle(-5, -5, cachedImage.getWidth() + 10, cachedImage.getHeight() + 10));
        backgroundComponent.render(graphics);
        graphics.drawImage(cachedImage, 0, 0, null);

        return new Dimension(cachedImage.getWidth() + 10, cachedImage.getHeight() + 10);
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

    private boolean overlayContains(final Point offsetPoint) {
        return offsetPoint.x < IMG_WIDTH && offsetPoint.y < IMG_HEIGHT;
    }

    void rotate(boolean toTheLeft) {
        if (toTheLeft) {
            this.firstTabIndex++;
        }
        else {
            this.firstTabIndex--;
            if (this.firstTabIndex < 0) {
                this.firstTabIndex += this.loadedMinigames.size();
            }
        }
        this.firstTabIndex = this.firstTabIndex % this.loadedMinigames.size();
        this.requestRedraw();
    }

    @Override
    public void keyTyped(KeyEvent event) {
        if (this.isOverlayShown()) {
            if (event.getKeyCode() == KeyEvent.VK_LEFT || event.getKeyCode() == KeyEvent.VK_RIGHT) {
                this.minigameToolbar.keyTyped(event);
            }
            else {
                // Default: pass to the minigame.
                this.loadedMinigames.get(this.currentMinigameIndex).keyTyped(event);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (this.isOverlayShown() && event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            this.closeOverlay();
            event.consume();
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {}

    @Override
    public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event, Point relativeOffset) {
        if (relativeOffset == null) {
            relativeOffset = this.getBounds().getLocation();
        }
        if (this.isOverlayShown()) {
            Point currentLocation = event.getPoint();
            Point offsetLocation = new Point(currentLocation.x - relativeOffset.x, currentLocation.y - relativeOffset.y);
            if (overlayContains(offsetLocation)) {
                RelativeMinigameComponentStruct passThroughCurrent = this.getSubComponentAtPoint(offsetLocation);
                if (passThroughCurrent.isValid()) {
                    event = passThroughCurrent.handler.mouseWheelMoved(event, passThroughCurrent.offset);
                }
            }
        }

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
        if (this.isOverlayShown()) {
            Point currentLocation = event.getPoint();
            Point offsetLocation = new Point(currentLocation.x - relativeOffset.x, currentLocation.y - relativeOffset.y);
            if (overlayContains(offsetLocation)) {
                RelativeMinigameComponentStruct passThroughCurrent = this.getSubComponentAtPoint(offsetLocation);
                if (passThroughCurrent.isValid()) {
                    event = passThroughCurrent.handler.mousePressed(event, passThroughCurrent.offset);
                }
            }
        }

        return event;
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
                    event = passThroughPrevious.handler.mouseMoved(event, passThroughCurrent.offset);
                }
                this.previousRelativePoint = offsetLocation;
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
        else
        {
            struct.handler = this.loadedMinigames.get(this.currentMinigameIndex);
            struct.offset = new Point(relativeOffset.x, relativeOffset.y - MinigameToolbar.getToolbarHeight());
        }
        return struct;
    }

}

