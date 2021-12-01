package com.runelitebingo;

import com.runeliteminigames.MinigamePlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RuneliteBingoTest
{
	public static void main(String[] args) throws Exception
	{
		//noinspection unchecked
		ExternalPluginManager.loadBuiltin(MinigamePlugin.class);
		RuneLite.main(args);
	}
}