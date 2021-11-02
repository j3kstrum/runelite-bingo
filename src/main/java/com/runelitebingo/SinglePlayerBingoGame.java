package com.runelitebingo;

import com.runeliteminigame.IMinigamePlugin;
import com.runeliteminigame.display.IDisplayableMinigame;
import com.runeliteminigame.display.RelativeMinigameComponentStruct;
import com.runeliteminigame.tasks.CombatTask;
import com.runeliteminigame.tasks.IRunescapeTask;
import com.runeliteminigame.util.CommonImages;
import com.runeliteminigame.util.ImageUtils;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.components.BackgroundComponent;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

public class SinglePlayerBingoGame implements IDisplayableMinigame {

    private static final int NUM_TILES = 5;
    private static final int RECOMMENDED_IMAGE_MINIMUM_SIZE = 40;
    private static final BufferedImage BINGO_IMAGE;
    private static final int MINIMUM_BOARD_WIDTH = NUM_TILES * RECOMMENDED_IMAGE_MINIMUM_SIZE;
    private static final int MINIMUM_DESCRIPTION_WIDTH = 200;
    private static final int MINIMUM_IMAGE_WIDTH = MINIMUM_BOARD_WIDTH + MINIMUM_DESCRIPTION_WIDTH;

    static {
        // Sets the bingo image.
        URL bingoGameIconURL = CombatTask.class.getClassLoader().getResource("bingo_game_icon.png");

        if (bingoGameIconURL == null) {
            BINGO_IMAGE = null;
        }
        else {
            BufferedImage bingoImage = null;
            try {
                bingoImage = ImageIO.read(bingoGameIconURL);
            } catch (IOException ioe) {
                System.out.println("SinglePlayerBingoGame failed to load bingo game icon from local resources.");
            } finally {
                BINGO_IMAGE = bingoImage;
            }
        }
    }

    // Restrict constructor access to this class; users need to call createGame or loadGameFrom.
    private SinglePlayerBingoGame(IMinigamePlugin plugin) {
        this.backgroundComponent = new BackgroundComponent();
        this.backgroundComponent.setFill(false);
        this.plugin = plugin;
        this.spriteManager = plugin.getSpriteManager();
    }

    private boolean cancelled = false;
    private Point selectedTask = null;
    private Point hoveredTask = null;
    private Point previousRelativePoint = new Point(-1, -1);
    private final IRunescapeTask[][] tasks = new IRunescapeTask[5][5];
    private Rectangle boardRectangle = null;
    private Rectangle descriptionRectangle = null;
    private final BackgroundComponent backgroundComponent;
    private final IMinigamePlugin plugin;
    private final SpriteManager spriteManager;
    private BufferedImage BANK_TAB_EMPTY;
    private BufferedImage BANK_TAB_HOVERED;
    private BufferedImage BANK_TAB_SELECTED;

    public static SinglePlayerBingoGame createGame(BingoConstraint constraint, IMinigamePlugin plugin) {
        SinglePlayerBingoGame game = new SinglePlayerBingoGame(plugin);
        IRunescapeTask[][] tasks;
        if (constraint == null) {
            tasks = BingoConstraint.randomTasks(plugin);
        } else {
            tasks = constraint.createTasks(plugin);
        }
        for (int row = 0; row < tasks.length; row++) {
            System.arraycopy(tasks[row], 0, game.tasks[row], 0, tasks[row].length);
        }
        return game;
    }

    public static SinglePlayerBingoGame loadGameFrom(Dictionary<String, Object> config, IMinigamePlugin plugin) {
        boolean cancelledGame = (boolean)config.get("cancelled");
        Dictionary<String, Object>[][] taskSpecs = (Dictionary<String, Object>[][]) config.get("tasks");
        SinglePlayerBingoGame game = new SinglePlayerBingoGame(plugin);
        game.cancelled = cancelledGame;
        for (int row = 0; row < taskSpecs.length; row++) {
            for (int col = 0; col < taskSpecs[row].length; col++) {
                Dictionary<String, Object> taskSpec = taskSpecs[row][col];
                IRunescapeTask task;
                switch ((String)taskSpec.get(IRunescapeTask.TASK_TYPE)) {
                    case CombatTask.COMBAT_MINI_GAME_TASK:
                        task = CombatTask.loadFrom(taskSpec, plugin);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid taskType from configuration: " + taskSpec.get(IRunescapeTask.TASK_TYPE));
                }
                game.tasks[row][col] = task;
            }
        }
        return game;
    }

    @Override
    public Dictionary<String, Object> serializedGame() {
        Dictionary<String, Object> serialized = new Hashtable<>();
        serialized.put("cancelled", cancelled);

        Object[][] taskSerialization = new Object[tasks.length][tasks[0].length];
        for (int row = 0; row < taskSerialization.length; row++) {
            for (int col = 0; col < taskSerialization[row].length; col++) {
                taskSerialization[row][col] = tasks[row][col].serializedTask();
            }
        }
        serialized.put("tasks", taskSerialization);
        return serialized;
    }

    @Override
    public boolean isCompleted() {
        // Check row-wise.
        for (IRunescapeTask[] taskList: tasks) {
            boolean completedRow = true;
            for (IRunescapeTask task: taskList) {
                if (!task.isCompleted()) {
                    completedRow = false;
                    break;
                }
            }
            if (completedRow) {
                return true;
            }
        }

        // Check column-wise.
        for (int i = 0; i < tasks[0].length; i++) {
            boolean completedCol = true;
            for (IRunescapeTask[] row: tasks) {
                if (!row[i].isCompleted()) {
                    completedCol = false;
                    break;
                }
            }
            if (completedCol) {
                return true;
            }
        }

        // Check crosses.
        boolean completedCrossTopLeft = true;
        for (int i = 0; i < tasks.length; i++) {
            if (!tasks[i][i].isCompleted()) {
                completedCrossTopLeft = false;
                break;
            }
        }
        if (completedCrossTopLeft) {
            return true;
        }

        boolean completedCrossBottomLeft = true;
        for (int i = 0; i < tasks.length; i++) {
            if (!tasks[tasks.length - i - 1][i].isCompleted()) {
                completedCrossBottomLeft = false;
                break;
            }
        }
        return completedCrossBottomLeft;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isMultiPlayer() {
        return false;
    }

    @Override
    public BufferedImage getIcon(IMinigamePlugin plugin) {
        BufferedImage bingoImage;
        if (BINGO_IMAGE != null) {
            bingoImage = BINGO_IMAGE;
        }
        else {
            bingoImage = plugin.getSpriteManager().getSprite(SpriteID.MAP_ICON_MINIGAME, 0);
            assert bingoImage != null;
        }
        BufferedImage result = new BufferedImage(bingoImage.getWidth(), bingoImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        result.getGraphics().drawImage(bingoImage, 0, 0, null);
        if (this.isCompleted()) {
            BufferedImage taskCompleteImage = CommonImages.getTaskCompleteImage();
            taskCompleteImage = ImageUtils.scale(taskCompleteImage, result.getWidth() * 2 / 3, result.getHeight() * 2 / 3);
            result.getGraphics().drawImage(taskCompleteImage, result.getWidth() / 3, result.getHeight() / 3, null);
        }
        return result;
    }

    private BufferedImage getBoardImage(IMinigamePlugin plugin, Dimension requestedDimension) {
        if (requestedDimension == null) {
            requestedDimension = new Dimension(RECOMMENDED_IMAGE_MINIMUM_SIZE * NUM_TILES, RECOMMENDED_IMAGE_MINIMUM_SIZE * NUM_TILES);
        }
        Dimension tileDimension = new Dimension(
                requestedDimension.width / this.tasks[0].length,
                requestedDimension.height / this.tasks.length
        );

        if (BANK_TAB_EMPTY == null) {
            this.BANK_TAB_EMPTY = spriteManager.getSprite(SpriteID.BANK_TAB_EMPTY, 0);
        }

        BufferedImage[][] images = new BufferedImage[tasks.length][tasks[0].length];
        BufferedImage[][] backgrounds = new BufferedImage[tasks.length][tasks[0].length];
        for (int row = 0; row < images.length; row++) {
            for (int col = 0; col < images[row].length; col++) {
                images[row][col] = tasks[row][col].getImage(plugin, tileDimension);
                backgrounds[row][col] = ImageUtils.scale(BANK_TAB_EMPTY, tileDimension.width, tileDimension.height);
            }
        }

        if (BANK_TAB_HOVERED == null) {
            this.BANK_TAB_HOVERED = spriteManager.getSprite(SpriteID.BANK_TAB_HOVERED, 0);
        }
        else if (hoveredTask != null) {
            backgrounds[hoveredTask.y][hoveredTask.x] = ImageUtils.scale(BANK_TAB_HOVERED, tileDimension.width, tileDimension.height);
        }
        if (BANK_TAB_SELECTED == null) {
            this.BANK_TAB_SELECTED = spriteManager.getSprite(SpriteID.BANK_TAB_SELECTED, 0);
        }
        else if (selectedTask != null) {
            backgrounds[selectedTask.y][selectedTask.x] = ImageUtils.scale(BANK_TAB_SELECTED, tileDimension.width, tileDimension.height);
        }

        BufferedImage outputImage = new BufferedImage(requestedDimension.width, requestedDimension.height, BufferedImage.TYPE_INT_ARGB);
        // Run left to right, top to bottom.
        int currentPosZero = 0;
        for (int row = 0; row < images.length; row++) {
            int currentPosOne = 0;
            for (int col = 0; col < images[row].length; col++) {
                BufferedImage image = images[row][col];
                BufferedImage background = backgrounds[row][col];
                outputImage.getGraphics().drawImage(
                        ImageUtils.scale(background, tileDimension.width, tileDimension.height),
                        currentPosOne,
                        currentPosZero,
                        null
                );
                outputImage.getGraphics().drawImage(
                        ImageUtils.scale(image, tileDimension.width, tileDimension.height),
                        currentPosOne,
                        currentPosZero,
                        null
                );
                currentPosOne += tileDimension.width;
            }
            currentPosZero += tileDimension.height;
        }

        return ImageUtils.scale(outputImage, requestedDimension.width, requestedDimension.height);
    }

    private String getDescriptionText() {
        if (this.selectedTask == null) {
            String text = "Bingo minigame: Complete tasks in " +
                   "any order that you like. You win when " +
                   "you complete five tasks that form a row, a column, or a diagonal line. " +
                   "Note that four corners are not supported at this time.";
            if (isCompleted()) {
                text += "\n\nThis game has been completed!";
            }
            return text;
        }
        String text = this.tasks[selectedTask.y][selectedTask.x].getDescriptionText();
        if (this.tasks[selectedTask.y][selectedTask.x].isCompleted()) {
            text += "\n\nThis task has been completed!";
        }
        return text;
    }

    private BufferedImage getDescriptionImage(Dimension requestedDimension) {

        int framePadding = 5;

        BufferedImage outputImage = new BufferedImage(requestedDimension.width, requestedDimension.height, BufferedImage.TYPE_INT_ARGB);
        outputImage.getGraphics().setFont(FontManager.getRunescapeFont());

        this.backgroundComponent.render(outputImage.createGraphics());
        this.backgroundComponent.setRectangle(new Rectangle(0, 0, requestedDimension.width, requestedDimension.height));
        String descriptionText = this.getDescriptionText();
        // TODO: Cache this when the text doesn't change.
        BufferedImage descriptionImage = ImageUtils.getTextImageScrollVertical(descriptionText, outputImage.getGraphics(), Math.max(0, outputImage.getWidth() - 2 * framePadding));
        outputImage.getGraphics().drawImage(descriptionImage, framePadding, framePadding, null);

        return ImageUtils.scale(outputImage, requestedDimension.width, requestedDimension.height);
    }

    @Override
    public BufferedImage getMainImage(IMinigamePlugin plugin, Dimension requestedDimension) {
        // We will always draw the image on the right side of the plugin (for now), scaling the board image
        // to the remaining size.
        Dimension descriptionDimension;
        Dimension boardDimension;
        if (requestedDimension.getWidth() < MINIMUM_IMAGE_WIDTH) {
            // Don't draw the panel.
            descriptionDimension = new Dimension(0, 0);
            boardDimension = requestedDimension;
        } else {
            descriptionDimension = new Dimension(MINIMUM_DESCRIPTION_WIDTH, requestedDimension.height);
            boardDimension = new Dimension(requestedDimension.width - MINIMUM_DESCRIPTION_WIDTH, requestedDimension.height);
        }
        // We want a square board, so it'll be centered with whitespace on the top and bottom.
        // The size will be the minimum of the allowed width/height.
        int minimumDimension = Math.min(boardDimension.height, boardDimension.width);
        boardDimension = new Dimension(minimumDimension, minimumDimension);

        BufferedImage boardImage = this.getBoardImage(plugin, boardDimension);
        BufferedImage descriptionImage = null;
        if (descriptionDimension.width > 0 && descriptionDimension.height > 0) {
            descriptionImage = this.getDescriptionImage(descriptionDimension);
        }

        BufferedImage output = new BufferedImage(descriptionDimension.width + boardDimension.width, requestedDimension.height, BufferedImage.TYPE_INT_ARGB);
        // Board image is centered on the left.
        int boardPosY = (requestedDimension.height - boardImage.getHeight()) / 2;
        int descriptionPosX = boardImage.getWidth();

        this.boardRectangle = new Rectangle(0, boardPosY, boardImage.getWidth(), boardImage.getHeight());
        if (descriptionImage == null) {
            this.descriptionRectangle = null;
        } else {
            this.descriptionRectangle = new Rectangle(descriptionPosX, 0, descriptionImage.getWidth(), descriptionImage.getHeight());
        }

        output.getGraphics().drawImage(boardImage, boardRectangle.x, boardRectangle.y, null);
        if (descriptionImage != null) {
            output.getGraphics().drawImage(descriptionImage, descriptionRectangle.x, descriptionRectangle.y, null);
        }

        return output;
    }

    private Point boardContains(Point relativeOffset) {
        if (boardRectangle == null || relativeOffset == null) {
            return null;
        }
        if (
                relativeOffset.x >= boardRectangle.x && relativeOffset.x < boardRectangle.width + boardRectangle.x &&
                relativeOffset.y >= boardRectangle.y && relativeOffset.y < boardRectangle.height + boardRectangle.y
        ) {
            int x_offset = relativeOffset.x - boardRectangle.x;
            int y_offset = relativeOffset.y - boardRectangle.y;
            return new Point((tasks[0].length * x_offset) / boardRectangle.width, (tasks.length * y_offset) / boardRectangle.height);
        }
        else {
            return null;
        }
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
        Point boardLocation = boardContains(relativeOffset);
        if (boardLocation != null) {
            // If we click on a task tile, set it to the active tile.
            // If the task's details are already showing, de-highlight the tab and show the game's details instead.
            if (selectedTask != null && boardLocation.x == selectedTask.x && boardLocation.y == selectedTask.y) {
                // De-select.
                selectedTask = null;
            } else {
                selectedTask = boardLocation;
            }
            this.plugin.requestRedraw();
        }
        return event;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event, Point relativeOffset) {
        return event;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event, Point relativeOffset) {
        // When hovering over a task, change task bank tab to highlighted.
        Point previousBoardPoint = boardContains(previousRelativePoint);
        Point currentBoardPoint = boardContains(relativeOffset);
        if (currentBoardPoint != null || previousBoardPoint != null) {
            this.hoveredTask = currentBoardPoint;
            this.plugin.requestRedraw();
            this.previousRelativePoint = relativeOffset;
        }
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
