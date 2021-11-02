package com.runeliteminigame.util;

import com.runeliteminigame.tasks.CombatTask;
import net.runelite.client.ui.FontManager;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Class containing static methods designed to simplify image-related transformation and utility functions.
 */
public class ImageUtils {

    private static final BufferedImage EMPTY_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

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
        Rectangle2D stringRectangle = fontMetrics.getStringBounds(text, alignedAgainst.getGraphics());
        int startY = alignedAgainst.getHeight() - (int)Math.ceil(stringRectangle.getHeight());
        int startX = alignedAgainst.getWidth() - (int)Math.ceil(stringRectangle.getWidth());
        return new Point(startX, startY);
    }

    /**
     * Tries to load a buffered image from the provided relative path in the resources folder.
     * If it fails, returns an empty image.
     * @param resourceFileName The relative filepath inside of the resources folder from which the image should be loaded.
     * @return The loaded image, or the empty image if the loaded image cannot be found.
     */
    public static BufferedImage loadOrReturnEmpty(String resourceFileName) {
        URL resourceURL = CombatTask.class.getClassLoader().getResource(resourceFileName);

        if (resourceURL == null) {
            return EMPTY_IMAGE;
        }
        else {
            try {
                return ImageIO.read(resourceURL);
            } catch (IOException ioe) {
                return EMPTY_IMAGE;
            }
        }
    }

    /**
     * Generates an image, with word wrap, using the selected text.
     * The output image will have a fixed width and will have an output height depending on the amount of text rendered.
     * @param textToRender The text to render on the image.
     * @param graphics The graphics object, containing the font used to render the text.
     * @param fixedWidth The width of the output image. The height will be variable.
     * @return The output image with variable height, consisting of the provided text.
     */
    public static BufferedImage getTextImageScrollVertical(String textToRender, Graphics graphics, int fixedWidth) {
        ArrayList<String> textRows = ImageUtils.wrappedText(textToRender, graphics, fixedWidth, textToRender.length());
        if (textRows.size() == 0) {
            return new BufferedImage(0, 0, BufferedImage.TYPE_INT_ARGB);
        }
        int sizePerRow = (int)Math.ceil(graphics.getFontMetrics().getStringBounds(textRows.get(0), graphics).getHeight());
        BufferedImage outputImage = new BufferedImage(fixedWidth, sizePerRow * (1 + textRows.size()), BufferedImage.TYPE_INT_ARGB);
        int offset = sizePerRow;
        outputImage.getGraphics().setFont(graphics.getFont());
        for(String line : textRows) {
            outputImage.getGraphics().drawString(line, 0, offset);
            offset += sizePerRow;
        }
        return outputImage;
    }

    private static ArrayList<String> wrappedText(String longText, Graphics graphics, int maximumWidth, int suggestedLength) {
        // Bounded search for the number of characters than can be rendered.
        // Find max length from the start of the string
        if (longText.isEmpty()) {
            return new ArrayList<>();
        }
        int max_chars = longText.length();
        int min_chars = 1;
        int current = Math.min(suggestedLength, longText.length());
        while (max_chars != min_chars) {
            Rectangle2D bounds = graphics.getFontMetrics().getStringBounds(longText.substring(0, current), graphics);
            int stringWidth = (int)Math.ceil(bounds.getWidth());
            if (stringWidth > maximumWidth) {
                // Must choose a smaller string.
                max_chars = current - 1;
                // Linear interpolation for next string size.
                float charsPerPixel = (float)current / stringWidth;
                current = Math.round(charsPerPixel * maximumWidth);
                // Make sure that we converge and stay within bounds.
                current = Math.max(Math.min(current, max_chars), min_chars);
            }
            else if (stringWidth == maximumWidth) {
                // Exact match.
                break;
            }
            else {
                // Should look for a larger string, where possible. But this one works too.
                min_chars = current;
                // Linear interpolation for next string size.
                float charsPerPixel = (float)current / stringWidth;
                int guess = Math.round(charsPerPixel * maximumWidth);
                // Make sure that we converge and stay within bounds.
                current = Math.min(max_chars, Math.max(current + 1, guess));
            }
        }

        String substring = longText.substring(0, current).trim();
        int adjustedIndex = current;
        if (substring.contains(" ")) {
            // We'll shrink the substring to prevent cutting off words halfway.
            int spaceLocation = substring.lastIndexOf(" ");
            adjustedIndex = spaceLocation + 1;
            substring = longText.substring(0, adjustedIndex).trim();
        }
        if (substring.contains("\n")) {
            // Hard cutoff; newlines should be rendered that way.
            int newLineLocation = substring.indexOf("\n");
            adjustedIndex = newLineLocation + 1;
            substring = longText.substring(0, adjustedIndex).trim();
        }
        String remaining = longText.substring(adjustedIndex);

        ArrayList<String> result = new ArrayList<>();
        result.add(substring);
        result.addAll(ImageUtils.wrappedText(remaining, graphics, maximumWidth, current));
        return result;
    }

}
