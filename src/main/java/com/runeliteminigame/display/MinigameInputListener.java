package com.runeliteminigame.display;

import net.runelite.client.input.KeyListener;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseWheelListener;

import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

class MinigameInputListener extends MouseAdapter implements KeyListener, MouseWheelListener {
    private final MinigameDisplayContainer minigameDisplayContainer;

    MinigameInputListener(MinigameDisplayContainer minigameDisplayContainer) {
        this.minigameDisplayContainer = minigameDisplayContainer;
    }

    @Override
    public void keyTyped(KeyEvent event) {
        minigameDisplayContainer.keyTyped(event);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        minigameDisplayContainer.keyPressed(event);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        minigameDisplayContainer.keyReleased(event);
    }

    @Override
    public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event) {
        return minigameDisplayContainer.mouseWheelMoved(event, null);
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event) {
        return minigameDisplayContainer.mouseClicked(event, null);
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event) {
        return minigameDisplayContainer.mousePressed(event, null);
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event) {
        return minigameDisplayContainer.mouseMoved(event, null);
    }
}
