package com.runeliteminigame;

import java.util.Dictionary;

public interface IMinigame {

    Dictionary<String, Object> serializedGame();
    boolean isCompleted();
    boolean isCancelled();
    boolean isMultiPlayer();

}
