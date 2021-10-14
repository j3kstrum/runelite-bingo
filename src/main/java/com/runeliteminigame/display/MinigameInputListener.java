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
    private final MinigameDisplayContainer overlay;

    MinigameInputListener(MinigameDisplayContainer overlay) {
        this.overlay = overlay;
    }

    private boolean isNotWithinOverlay(final Point point) {
        return !overlay.getBounds().contains(point);
    }

    private boolean isWithinCloseButton(final Point point) {
        Point overlayPoint = new Point(point.x - (int) overlay.getBounds().getX(),
                point.y - (int) overlay.getBounds().getY());

        return overlay.getCloseButtonBounds() != null
                && overlay.getCloseButtonBounds().contains(overlayPoint);
    }

    @Override
    public void keyTyped(KeyEvent event) {}

    @Override
    public void keyPressed(KeyEvent event) {
        if (!overlay.isOverlayShown()) {
            return;
        }

        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            overlay.closeOverlay();
            event.consume();
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {}

    @Override
    public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event) {
        return event;
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event) {
        if (!overlay.isOverlayShown() || isNotWithinOverlay(event.getPoint())) {
            return event;
        }

        event.consume();
        return event;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event) {
        if (!overlay.isOverlayShown() || isNotWithinOverlay(event.getPoint())) {
            return event;
        }

        if (SwingUtilities.isLeftMouseButton(event) && isWithinCloseButton(event.getPoint())) {
            overlay.closeOverlay();
        }

        event.consume();
        return event;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event) {
        if (overlay.isOverlayShown()) {
            overlay.setCloseButtonHovered(isWithinCloseButton(event.getPoint()));
        }

        return event;
    }
}
