package com.runeliteminigame.display;

import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public abstract class BaseMinigameDirectionalButton implements IDisplayableWithIcon, IMinigameInputHandler {

    protected abstract int keyToToggleEvent();

    protected abstract void updateDisplayContainer();

    @Override
    public void keyTyped(KeyEvent event) {
        if (event.getKeyCode() == this.keyToToggleEvent()) {
            this.updateDisplayContainer();
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {

    }

    @Override
    public void keyReleased(KeyEvent event) {

    }

    @Override
    public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event, Point relativeOffset) {
        return event;
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event, Point relativeOffset) {
        if (SwingUtilities.isLeftMouseButton(event)) {
            this.updateDisplayContainer();
            event.consume();
        }
        return event;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event, Point relativeOffset) {
        return event;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event, Point relativeOffset) {
        return event;
    }

    @Override
    public RelativeMinigameComponentStruct getSubComponentAtPoint(Point relativeOffset) {
        return new RelativeMinigameComponentStruct();
    }
}
