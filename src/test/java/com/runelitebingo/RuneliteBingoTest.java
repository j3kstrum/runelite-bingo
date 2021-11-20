package com.runelitebingo;

import com.runeliteminigame.RuneliteMiniGamePlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RuneliteBingoTest
{
	public static void main(String[] args) throws Exception
	{
		//noinspection unchecked
		ExternalPluginManager.loadBuiltin(RuneliteMiniGamePlugin.class);
		RuneLite.main(args);
	}
}