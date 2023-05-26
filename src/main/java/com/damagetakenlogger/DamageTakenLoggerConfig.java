package com.damagetakenlogger;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("damagetakenlogger")
public interface DamageTakenLoggerConfig extends Config
{
	@ConfigItem(
		keyName = "showDamageTaken",
		name = "Show Damage Taken",
		description = "Show a notification whenever you take damage.",
		position = 10
	)
	default boolean showDamageTaken()
	{
		return true;
	}

	@ConfigItem(
		keyName = "damageTakenColor",
		name = "Damage Taken",
		description = "Color for the 'you were hit' messages.",
		position = 20
	)
	default Color damageTakenColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		keyName = "showExperienceGained",
		name = "Show XP Gained",
		description = "Show the accumulated amount of XP gained per kill.",
		position = 30
	)
	default boolean showExperienceGained()
	{
		return true;
	}

	@ConfigItem(
		keyName = "experienceGainedColor",
		name = "XP Gained",
		description = "Color for the 'you gained XP' messages.",
		position = 40
	)
	default Color experienceGainedColor()
	{
		return Color.CYAN;
	}

	enum ExperienceStyle
	{
		TOTAL,
		PER_SECOND,
	}
	@ConfigItem(
			position = 5,
			keyName = "ExperienceGainedAmount",
			name = "XP Amount Style",
			description = "Determines how the amount of experience should be displayed."
	)
	default ExperienceStyle ExperienceGainedAmount() { return ExperienceStyle.TOTAL; }
}
