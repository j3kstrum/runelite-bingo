package com.runeliteminigame.util;

import net.runelite.client.ui.FontManager;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
 * Class containing static methods designed to simplify image-related transformation and utility functions.
 */
public class ImageUtils {

    /**
     * Scales the provided image, using a Bilinear affine transformation, to the target width and height.
     * No aspect ratio preservation is performed.
     * @param original The original image which should be scaled.
     * @param newWidth The target width of the output image.
     * @param newHeight The target height of the output image.
     * @return A new <code>BufferedImage</code> whose content is similar to <code>original</code>, scaled to the
     *      new height and width.
     */
    public static BufferedImage scale(BufferedImage original, int newWidth, int newHeight) {
        // Create new scaler and filter old image into upsampled image.
        AffineTransform scaleTransform = new AffineTransform();
        scaleTransform.scale((float)newWidth / original.getWidth(), (float)newHeight / original.getHeight());
        return new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR).filter(original, null);
    }

    /**
     * Scales the provided image, using a Bilinear affine transformation, so that its width and height
     * are equal to <code>newSize</code>.
     * @param original The original image which should be scaled.
     * @param newSize The target width and target height of the output image.
     * @return A new <code>BufferedImage</code> whose content is similar to <code>original</code>, scaled to the
     *      new height and width.
     */
    public static BufferedImage scaleSquare(BufferedImage original, int newSize) {
        return scale(original, newSize, newSize);
    }

    /**
     * Constructs the start position for drawing text, aligned to the bottom-right of a target image.
     * This defaults to the Runescape default font, but may change at any time.
     * @param text The text that will be drawn on the target image, aligned to the bottom-right corner.
     * @param alignedAgainst The image against which alignment should be performed.
     * @return The <code>Point</code> object representing the start position to be used when drawing the text
     * against the bottom-right corner of the image.
     */
    public static Point bottomRightAlignedPoints(String text, BufferedImage alignedAgainst) {
        return ImageUtils.bottomRightAlignedPoints(text, alignedAgainst, FontManager.getRunescapeFont());
    }

    /**
     * Constructs the start position for drawing text, aligned to the bottom-right of a target image.
     * @param text The text that will be drawn on the target image, aligned to the bottom-right corner.
     * @param alignedAgainst The image against which alignment should occur.
     * @param font The font that will be used to draw the text onto the image.
     * @return A <code>Point</code> object containing the start position for drawing the text aligned against
     * the bottom-right corner of the image.
     */
    public static Point bottomRightAlignedPoints(String text, BufferedImage alignedAgainst, Font font) {
        FontMetrics fontMetrics = alignedAgainst.getGraphics().getFontMetrics(font);
        int startY = alignedAgainst.getHeight() - fontMetrics.getHeight();
        int startX = alignedAgainst.getWidth() - fontMetrics.stringWidth(text) - fontMetrics.stringWidth(" ");
        return new Point(startX, startY);
    }

}
