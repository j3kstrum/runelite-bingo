package com.runelitebingo;

import com.runeliteminigame.IDisplayableMinigame;
import com.runeliteminigame.IMinigamePlugin;
import com.runeliteminigame.tasks.CombatTask;
import com.runeliteminigame.tasks.IRunescapeTask;
import net.runelite.api.ItemID;

import java.awt.image.BufferedImage;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.stream.IntStream;

public class SinglePlayerBingoGame implements IDisplayableMinigame {

    private boolean cancelled = false;
    private final IRunescapeTask[][] tasks = {
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null},
            {null, null, null, null, null}
    };

    public static SinglePlayerBingoGame createGame(BingoConstraint constraint, IMinigamePlugin plugin) {
        SinglePlayerBingoGame game = new SinglePlayerBingoGame();
        IRunescapeTask[][] tasks;
        if (constraint == null) {
            tasks = BingoConstraint.randomTasks(plugin);
        } else {
            tasks = constraint.createTasks(plugin);
        }
        for (int row = 0; row < tasks.length; row++) {
            for (int col = 0; col < tasks[row].length; col++) {
                game.tasks[row][col] = tasks[row][col];
            }
        }
        return game;
    }

    public static SinglePlayerBingoGame loadGameFrom(Dictionary<String, Object> config, IMinigamePlugin plugin) {
        boolean cancelledGame = (boolean)config.get("cancelled");
        Dictionary<String, Object>[][] taskSpecs = (Dictionary<String, Object>[][]) config.get("tasks");
        SinglePlayerBingoGame game = new SinglePlayerBingoGame();
        game.cancelled = cancelledGame;
        for (int row = 0; row < taskSpecs.length; row++) {
            for (int col = 0; col < taskSpecs[row].length; col++) {
                Dictionary<String, Object> taskSpec = taskSpecs[row][col];
                IRunescapeTask task;
                switch ((String)taskSpec.get(IRunescapeTask.TASK_TYPE)) {
                    case CombatTask.COMBAT_MINIGAME_TASK:
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
        return plugin.getItemManager().getImage(ItemID.A_RED_CIRCLE);
    }

    @Override
    public BufferedImage getGameImage(IMinigamePlugin plugin) {
        BufferedImage[][] images = new BufferedImage[tasks.length][tasks[0].length];
        for (int row = 0; row < images.length; row++) {
            for (int col = 0; col < images[row].length; col++) {
                images[row][col] = tasks[row][col].getImage(plugin);
            }
        }
        int[] colWidths = new int[tasks[0].length];
        for (int i = 0; i < tasks[0].length; i++) {
            colWidths[i] = 0;
        }
        for (BufferedImage[] row : images) {
            for(int col = 0; col < tasks[0].length; col++) {
                colWidths[col] = Math.max(colWidths[col], row[col].getWidth());
            }
        }

        int[] rowHeights = new int[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            rowHeights[i] = 0;
            for (BufferedImage img : images[i]) {
                rowHeights[i] = Math.max(rowHeights[i], img.getHeight());
            }
        }

        int totalWidth = IntStream.of(colWidths).sum();
        int totalHeight = IntStream.of(rowHeights).sum();

        BufferedImage outputImage = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
        // Run left to right, top to bottom.
        int currentPosZero = 0;
        for (int i = 0; i < images.length; i++) {
            int currentPosOne = 0;
            for (int j = 0; j < images.length; j++) {
                outputImage.getGraphics().drawImage(images[i][j], currentPosOne, currentPosZero, null);
                currentPosOne += colWidths[j];
            }
            currentPosZero += rowHeights[i];
        }

        return outputImage;
    }
}