package com.runeliteminigames.controllers.interf;

import net.runelite.client.input.KeyListener;
import net.runelite.client.input.MouseListener;

public interface IController extends KeyListener, MouseListener {

    void resetTransientState();
    void onGenericEvent(Object event);

}
