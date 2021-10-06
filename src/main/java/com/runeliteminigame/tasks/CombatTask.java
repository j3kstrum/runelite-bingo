package com.runeliteminigame.tasks;

import com.runeliteminigame.IMinigamePlugin;
import com.runeliteminigame.util.ImageUtils;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Dictionary;
import java.util.Hashtable;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class CombatTask implements IRunescapeTask {

    private final CombatTaskElement target;
    private final int amount;
    private int progress;
    private BufferedImage taskImage;
    private static final BufferedImage TASK_COMPLETE_IMAGE;

    static {
        TASK_COMPLETE_IMAGE = new BufferedImage(1, 1, TYPE_INT_RGB);
        Graphics g = TASK_COMPLETE_IMAGE.getGraphics();
        g.setColor(Color.GREEN);
        g.drawRect(0, 0, 1, 1);
    }

    public static final String COMBAT_MINIGAME_TASK = "combat";
    private final float minFractionDamage;
    private static final float DEFAULT_MIN_FRACTION_DAMAGE = 0.5f;
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

    public void onPlayerKilledNPC(NPC killed, int damageDealt) {
        if (isCompleted()) {
            return;
        }
        if (
                this.target.isTaskNPC((killed).getId()) &&
                (float)damageDealt / this.target.getTotalHealth() >= this.minFractionDamage
        ) {
            ++this.progress;
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
            taskImage = new BufferedImage(scaledBaseImage.getWidth(), scaledBaseImage.getHeight(), TYPE_INT_RGB);
            taskImage.getGraphics().drawImage(scaledBaseImage, 0, 0, null);
            taskImage.getGraphics().drawImage(combatOverlay, 0, 0, null);
        }
        // TODO: Add image for the quantity.
        if (isCompleted()) {
            BufferedImage completedImage = ImageUtils.scale(TASK_COMPLETE_IMAGE, taskImage.getWidth(), taskImage.getHeight());
            taskImage.getGraphics().drawImage(completedImage, 0, 0, null);
        }
        return taskImage;
    }

    @Override
    public boolean isCompleted() {
        boolean completed = amount == progress;
        if (completed && pluginSubscribedTo != null) {
            // Completely disassociate from the listener.
            pluginSubscribedTo.removePlayerKilledNPCListener(this);
            pluginSubscribedTo = null;
        }
        return completed;
    }

    @Override
    public Dictionary<String, Object> serializedTask() {
        Dictionary<String, Object> ret = new Hashtable<>();
        ret.put(TASK_TYPE, COMBAT_MINIGAME_TASK);
        ret.put("targetName", this.target.getName());
        ret.put("amount", this.amount);
        ret.put("progress", this.progress);
        return ret;
    }

    public static CombatTask loadFrom(Dictionary<String, Object> serialized, IMinigamePlugin plugin) {
        assert serialized.get(TASK_TYPE).equals(COMBAT_MINIGAME_TASK);
        CombatTask task = new CombatTask((String)serialized.get("targetName"), (int)serialized.get("amount"), plugin);
        task.progress = (int)serialized.get("progress");
        return task;
    }
}
