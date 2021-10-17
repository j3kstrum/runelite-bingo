package com.runeliteminigame.util;

import com.runeliteminigame.tasks.CombatTask;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class CommonImages {

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

    public static BufferedImage getTaskCompleteImage() {
        return TASK_COMPLETE_IMAGE;
    }
}
