package com.runelitebingo;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RuneliteBingoTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RuneliteBingoPlugin.class);
		RuneLite.main(args);
	}
}