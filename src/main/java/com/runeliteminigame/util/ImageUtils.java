package com.runeliteminigame.util;

import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.components.TextComponent;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class ImageUtils {

    public static BufferedImage scale(BufferedImage original, int newWidth, int newHeight) {
        // Create new scaler and filter old image into upsampled image.
        AffineTransform scaleTransform = new AffineTransform();
        scaleTransform.scale((float)newWidth / original.getWidth(), (float)newHeight / original.getHeight());
        return new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR).filter(original, null);
    }

    public static BufferedImage scaleSquare(BufferedImage original, int newSize) {
        return scale(original, newSize, newSize);
    }

    public static Point bottomRightAlignedPoints(String text, BufferedImage alignedAgainst) {
        return ImageUtils.bottomRightAlignedPoints(text, alignedAgainst, FontManager.getRunescapeFont());
    }

    public static Point bottomRightAlignedPoints(String text, BufferedImage alignedAgainst, Font font) {
        FontMetrics fontMetrics = alignedAgainst.getGraphics().getFontMetrics(font);
        int startY = alignedAgainst.getHeight() - fontMetrics.getHeight();
        int startX = alignedAgainst.getWidth() - fontMetrics.stringWidth(text) - fontMetrics.stringWidth(" ");
        return new Point(startX, startY);
    }

}
