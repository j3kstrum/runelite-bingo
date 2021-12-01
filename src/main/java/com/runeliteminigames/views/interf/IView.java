package com.runeliteminigames.views.interf;

import com.runeliteminigames.models.interf.IModel;

import java.awt.image.BufferedImage;

public interface IView {

    BufferedImage image();
    void update(IModel usingModel);
}
