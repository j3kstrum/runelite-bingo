package com.runeliteminigame.tasks;

import com.runeliteminigame.IMinigamePlugin;
import com.runeliteminigame.pluginlisteners.ICombatListener;
import com.runeliteminigame.util.CommonImages;
import com.runeliteminigame.util.ImageUtils;
import net.runelite.api.NPC;
import net.runelite.api.SpriteID;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.components.BackgroundComponent;
import net.runelite.client.ui.overlay.components.TextComponent;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Dictionary;
import java.util.Hashtable;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class CombatTask implements IRunescapeTask, ICombatListener {

    public static final String COMBAT_MINI_GAME_TASK = "combat";

    private static final float DEFAULT_MIN_FRACTION_DAMAGE = 0.5f;

    private final CombatTaskElement target;
    private final int amount;
    private int progress;

    private BufferedImage targetImage;
    private BufferedImage taskIndicatorImage;
    private final BackgroundComponent backgroundComponent;

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
        this.backgroundComponent = new BackgroundComponent();
        backgroundComponent.setFill(false);
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
    public BufferedImage getImage(IMinigamePlugin plugin, Dimension requestedDimension) {
        if (requestedDimension == null) {
            requestedDimension = new Dimension(64, 64);
        }
        if (this.targetImage == null) {
            this.targetImage = this.target.getImage(plugin.getItemManager());
        }
        if (this.taskIndicatorImage == null ) {
            // Get combat overlay image.
            this.taskIndicatorImage = plugin.getSpriteManager().getSprite(SpriteID.TAB_COMBAT, 0);
        }

        // The output image will be larger than the target image; the target will be 3/4 of the size.
        Dimension targetImageDimension = new Dimension(requestedDimension.width * 3 / 4, requestedDimension.height * 3 / 4);
        BufferedImage coreImage = new BufferedImage(targetImageDimension.width, targetImageDimension.height, TYPE_INT_ARGB);
        coreImage.getGraphics().drawImage(
                ImageUtils.scale(this.targetImage, coreImage.getWidth(), coreImage.getHeight()),
                0,
                0,
                null
        );
        coreImage.getGraphics().drawImage(
                ImageUtils.scale(this.taskIndicatorImage, coreImage.getWidth() / 2, coreImage.getHeight() / 2),
                0,
                0,
                null
        );

        if (isCompleted()) {
            BufferedImage completedImage = ImageUtils.scale(CommonImages.getTaskCompleteImage(), coreImage.getWidth(), coreImage.getHeight());
            coreImage.getGraphics().drawImage(completedImage, 0, 0, null);
        } else {
            // Add image for the quantity.
            String amountLeftText = String.valueOf(this.amount - this.progress);
            TextComponent amountLeft = new TextComponent();
            amountLeft.setFont(FontManager.getRunescapeFont());
            amountLeft.setText(amountLeftText);

            // Location is bottom-right corner.
            Point startCoordinates = ImageUtils.bottomRightAlignedPoints(amountLeftText, coreImage, FontManager.getRunescapeFont());

            if (startCoordinates.x >= 0 && startCoordinates.y >= 0) {
                // Don't draw if the start y or x are invalid - just return the original.
                amountLeft.setPosition(startCoordinates);
                amountLeft.render(coreImage.createGraphics());
            }

        }

        // Now, put the background image in and then render the core image on top.
        BufferedImage outputImage = new BufferedImage(requestedDimension.width, requestedDimension.height, TYPE_INT_ARGB);

        backgroundComponent.setRectangle(new Rectangle(0, 0, outputImage.getWidth(), outputImage.getHeight()));
        backgroundComponent.render(outputImage.createGraphics());

        outputImage.getGraphics().drawImage(coreImage, outputImage.getWidth() / 8, outputImage.getHeight() / 8, null);

        return outputImage;
    }

    @Override
    public boolean isCompleted() {
        boolean completed = amount == progress;
        if (completed && pluginSubscribedTo != null) {
            // Completely disassociate from the listener.
            pluginSubscribedTo.removePlayerKilledNPCListener(this);
            // Final redraw at total completion.
            this.requestRedraw();
            pluginSubscribedTo = null;
        }
        return completed;
    }

    @Override
    public String getDescriptionText() {
        return String.format(
                "Kill %d %s. Current progress: %d of %d (%.1f%%)",
                this.amount,
                this.target.getNamePlural(),
                this.progress,
                this.amount,
                Math.floor(100 * ((float)this.progress / this.amount))
        );
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
