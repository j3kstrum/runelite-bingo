package com.runeliteminigame.tasks;

import com.runeliteminigame.IMinigamePlugin;
import com.runeliteminigame.pluginlisteners.ICombatListener;
import com.runeliteminigame.util.ImageUtils;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.components.TextComponent;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class CombatTask implements IRunescapeTask, ICombatListener {

    public static final String COMBAT_MINI_GAME_TASK = "combat";

    private static final float DEFAULT_MIN_FRACTION_DAMAGE = 0.5f;
    private static final BufferedImage TASK_COMPLETE_IMAGE;

    static {
        // Sets the "task complete" image.
        BufferedImage completionImage;
        URL taskCompleteURL = CombatTask.class.getClassLoader().getResource("checkmark.png");

        BufferedImage backupImage = new BufferedImage(1, 1, TYPE_INT_ARGB);
        Graphics g = backupImage.getGraphics();
        g.setColor(Color.GREEN);
        g.drawRect(0, 0, 1, 1);

        if (taskCompleteURL == null) {
            completionImage = backupImage;
        }
        else {
            try {
                completionImage = ImageIO.read(taskCompleteURL);
            } catch (IOException ioe) {
                completionImage = backupImage;
            }
        }

        TASK_COMPLETE_IMAGE = completionImage;
    }

    private final CombatTaskElement target;
    private final int amount;
    private int progress;

    private BufferedImage taskImage;

    private final float minFractionDamage;
    private IMinigamePlugin pluginSubscribedTo;

    public CombatTask(String targetName, int quantity, IMinigamePlugin plugin) {
        this(targetName, quantity, plugin, DEFAULT_MIN_FRACTION_DAMAGE);
    }

    public CombatTask(String targetName, int quantity, IMinigamePlugin plugin, float minFractionDamage) {
        this.amount = quantity;
        this.progress = 0;
        this.target = CombatTaskElement.TASK_FROM_ID.get(targetName);
        this.minFractionDamage = minFractionDamage;
        this.pluginSubscribedTo = plugin;
        plugin.registerPlayerKilledNPCListener(this);
    }

    private void requestRedraw() {
        this.pluginSubscribedTo.requestRedraw();
    }

    public void onPlayerKilledNPC(NPC killed, int damageDealt) {
        if (isCompleted()) {
            return;
        }
        if (
                this.target.isTaskNPC((killed).getId()) &&
                (float)damageDealt / this.target.getTotalHealth() >= this.minFractionDamage
        ) {
            ++this.progress;
            this.requestRedraw();
        }
    }

    @Override
    public BufferedImage getImage(IMinigamePlugin plugin) {
        if (taskImage == null) {
            // Get the base image.
            BufferedImage baseImage = this.target.getImage(plugin.getItemManager());
            // Create scaled image.
            BufferedImage scaledBaseImage = ImageUtils.scale(baseImage, baseImage.getWidth() * 2, baseImage.getHeight() * 2);
            // Get combat overlay image. TODO: Choose a better image.
            BufferedImage combatOverlay = plugin.getItemManager().getImage(ItemID.BRONZE_SWORD);
            taskImage = new BufferedImage(scaledBaseImage.getWidth(), scaledBaseImage.getHeight(), TYPE_INT_ARGB);
            taskImage.getGraphics().drawImage(scaledBaseImage, 0, 0, null);
            taskImage.getGraphics().drawImage(combatOverlay, 0, 0, null);
        }

        BufferedImage retrievedImage = new BufferedImage(taskImage.getWidth(), taskImage.getHeight(), TYPE_INT_ARGB);
        retrievedImage.getGraphics().drawImage(taskImage, 0, 0, null);

        if (isCompleted()) {
            BufferedImage completedImage = ImageUtils.scale(TASK_COMPLETE_IMAGE, taskImage.getWidth(), taskImage.getHeight());
            retrievedImage.getGraphics().drawImage(completedImage, 0, 0, null);
        } else {
            // Add image for the quantity.
            String amountLeftText = String.valueOf(this.amount - this.progress);
            TextComponent amountLeft = new TextComponent();
            amountLeft.setText(amountLeftText);

            // Location is bottom-right corner.
            Point startCoordinates = ImageUtils.bottomRightAlignedPoints(amountLeftText, retrievedImage);

            if (startCoordinates.x < 0 || startCoordinates.y < 0) {
                // Don't draw if the start y or x are invalid - just return the original.
                return retrievedImage;
            }

            amountLeft.setPosition(startCoordinates);
            amountLeft.render(retrievedImage.createGraphics());
        }
        return retrievedImage;
    }

    @Override
    public boolean isCompleted() {
        boolean completed = amount == progress;
        if (completed && pluginSubscribedTo != null) {
            // Completely disassociate from the listener.
            pluginSubscribedTo.removePlayerKilledNPCListener(this);
            pluginSubscribedTo = null;
            // Final redraw at total completion.
            this.requestRedraw();
        }
        return completed;
    }

    @Override
    public Dictionary<String, Object> serializedTask() {
        Dictionary<String, Object> ret = new Hashtable<>();
        ret.put(TASK_TYPE, COMBAT_MINI_GAME_TASK);
        ret.put("targetName", this.target.getName());
        ret.put("amount", this.amount);
        ret.put("progress", this.progress);
        return ret;
    }

    public static CombatTask loadFrom(Dictionary<String, Object> serialized, IMinigamePlugin plugin) {
        assert serialized.get(TASK_TYPE).equals(COMBAT_MINI_GAME_TASK);
        CombatTask task = new CombatTask((String)serialized.get("targetName"), (int)serialized.get("amount"), plugin);
        task.progress = (int)serialized.get("progress");
        return task;
    }
}
