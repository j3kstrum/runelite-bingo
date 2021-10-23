package com.runeliteminigame.display;

import com.runeliteminigame.IMinigamePlugin;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

public interface IDisplayableElement extends IDisplayableWithIcon {

    /**
     * Creates and returns the main image for the provided displayable element.
     * This is separate from the element's icon.
     * @param plugin The IMinigamePlugin that can provided supplemental information for use when drawing.
     *               For example, the sprite and item managers can be accessed through the plugin.
     * @param requestedDimension The requested output dimension. This does not have to be respected by the
     *                           IDisplayableElement, but the expectation is that the caller will scale the output
     *                           of this function to the specified target. Should be null if no scaling will be
     *                           performed. Typically used by the IDisplayableElement to prevent image distortion
     *                           by the caller.
     * @return The BufferedImage representing the main image representing the element's current state.
     */
    BufferedImage getMainImage(IMinigamePlugin plugin, Dimension requestedDimension);
}
