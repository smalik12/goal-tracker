package com.goaltracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("goaltracker")
public interface GoalTrackerConfig extends Config
{
	@ConfigItem(
			keyName = "showCompleted",
			name = "Show Completed Goals",
			description = "Show completed goals in the tracker panel",
			position = 1
	)
	default boolean showCompleted()
	{
		return true;
	}

	@ConfigItem(
			keyName = "notifyOnCompletion",
			name = "Notify on Goal Completion",
			description = "Show a notification when a goal is completed",
			position = 2
	)
	default boolean notifyOnCompletion()
	{
		return true;
	}

	@ConfigItem(
			keyName = "autoHideEmpty",
			name = "Auto-hide Empty Categories",
			description = "Automatically hide categories with no goals",
			position = 3
	)
	default boolean autoHideEmpty()
	{
		return false;
	}
}