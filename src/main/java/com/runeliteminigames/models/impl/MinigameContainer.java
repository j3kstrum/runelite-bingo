package com.runeliteminigames.models.impl;

import com.runeliteminigames.enums.ControlAction;
import com.runeliteminigames.models.interf.IMinigameModel;
import com.runeliteminigames.models.interf.IModel;
import com.runeliteminigames.views.impl.MinigameContainerView;
import net.runelite.client.ui.overlay.Overlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinigameContainer implements IModel {

    private final List<IMinigameModel> minigames;
    private boolean shouldDisplay = false;
    private final MinigameContainerView view;
    private int startDrawPosition = 0;

    public MinigameContainer() {
        this.minigames = new ArrayList<>();
        this.view = new MinigameContainerView();
    }

    public Overlay getOverlay() {
        return this.view;
    }

    /**
     * Determines the currently active game.
     * @return The currently active/displayed minigame, or null if none are active.
     */
    private int activeGamePosition() {
        verifyActiveGameCount();
        for (int i = 0; i < minigames.size(); i++) {
            if (minigames.get(i).isActive()) {
                return i;
            }
        }
        return 0;
    }

    private void verifyActiveGameCount() {
        assert minigames.stream().filter(IMinigameModel::isActive).count() < 2;
    }

    public int getStartDrawPosition() {
        return startDrawPosition;
    }

    public List<IMinigameModel> getMinigames() {
        List<IMinigameModel> newList = new ArrayList<>();
        Collections.copy(newList, minigames);
        return newList;
    }

    @Override
    public void actionReceived(ControlAction action) {
        int previousDrawPosition = this.startDrawPosition;
        switch (action) {
            case EXIT_DISPLAY:
                if (this.shouldDisplay) {
                    this.shouldDisplay = false;
                    this.view.update(this);
                }
                break;
            case ENTER_DISPLAY:
                if (!this.shouldDisplay) {
                    this.shouldDisplay = true;
                    this.view.update(this);
                }
                break;
            case ROTATE_LEFT:
                if (this.startDrawPosition == 0) {
                    this.startDrawPosition = this.minigames.size();
                }
                else {
                    this.startDrawPosition -= 1;
                }
                break;
            case ROTATE_RIGHT:
                this.startDrawPosition += 1;
                if (this.startDrawPosition == this.minigames.size()) {
                    this.startDrawPosition = 0;
                }
                break;
        }
        if (previousDrawPosition != this.startDrawPosition) {
            this.view.update(this);
        }
    }
}
