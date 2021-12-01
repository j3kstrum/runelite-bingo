package com.runeliteminigames.controllers.impl;

import com.runeliteminigames.controllers.interf.IController;
import com.runeliteminigames.enums.ShowPluginEvent;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class MainController implements IController {

    private final List<IController> controllers;

    public MainController() {
        this.controllers = new ArrayList<>();
        controllers.add(new CombatController());
    }

    private void onShowPluginEvent(ShowPluginEvent event) {
        throw new NotImplementedException("Not yet implemented");
    }

    @Override
    public void onGenericEvent(Object event) {
        if (event instanceof ShowPluginEvent) {
            this.onShowPluginEvent((ShowPluginEvent) event);
        }
        for (IController controller : this.controllers) {
            controller.onGenericEvent(event);
        }
    }

    @Override
    public void resetTransientState() {
        for (IController controller: this.controllers) {
            controller.resetTransientState();
        }
    }

}
