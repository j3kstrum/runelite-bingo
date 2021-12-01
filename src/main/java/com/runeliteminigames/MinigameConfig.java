package com.runeliteminigames;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;

@ConfigGroup(MinigameConfig.configurationGroup)
public interface MinigameConfig extends Config
{
    String configurationGroup = "RuneliteCustomMinigames";
    String serializationKey = "MinigameContainer";
}
