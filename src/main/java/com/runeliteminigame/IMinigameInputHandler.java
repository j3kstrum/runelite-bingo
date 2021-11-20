package com.runeliteminigame;

import com.runeliteminigame.display.RelativeMinigameComponentStruct;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public interface IMinigameInputHandler {

    void keyTyped(KeyEvent event);

    void keyPressed(KeyEvent event);

    void keyReleased(KeyEvent event);

    MouseWheelEvent mouseWheelMoved(MouseWheelEvent event, Point relativeOffset);

    MouseEvent mouseClicked(MouseEvent event, Point relativeOffset);

    MouseEvent mousePressed(MouseEvent event, Point relativeOffset);

    MouseEvent mouseMoved(MouseEvent event, Point relativeOffset);

    MouseEvent mouseDragged(MouseEvent event, Point relativeOffset);

    RelativeMinigameComponentStruct getSubComponentAtPoint(Point relativeOffset);
}

