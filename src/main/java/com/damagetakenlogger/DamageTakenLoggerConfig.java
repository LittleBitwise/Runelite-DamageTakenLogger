package com.damagetakenlogger;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("damagetakenlogger")
public interface DamageTakenLoggerConfig extends Config
{
	@ConfigItem(
		keyName = "loggerColor",
		name = "Color",
		description = "The color of the 'damage taken' messages."
	)
	default String loggerColor()
	{
		return "255 0 0"; // todo, unused
	}
}
