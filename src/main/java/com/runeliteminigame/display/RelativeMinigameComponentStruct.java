package com.runeliteminigame.display;

import com.runeliteminigame.IMinigameInputHandler;

import java.awt.Point;

public class RelativeMinigameComponentStruct {
    public IMinigameInputHandler handler;
    public Point offset;

    public boolean isValid() {
        return handler != null && offset != null && offset.x >= 0 && offset.y >= 0;
    }
}
