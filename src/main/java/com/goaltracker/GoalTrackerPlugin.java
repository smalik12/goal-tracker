package com.goaltracker;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@PluginDescriptor(
		name = "Goal Tracker",
		description = "Tracks your OSRS goals and provides progress updates",
		tags = {"goals", "tracker", "progress", "skilling"}
)
public class GoalTrackerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private GoalTrackerConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	private NavigationButton navButton;
	private GoalTrackerPanel panel;
	private final List<Goal> goals = new ArrayList<>();
	private final Map<GoalType, List<Goal>> goalsByCategory = new HashMap<>();
	private final GoalManager goalManager = new GoalManager();

	private static final String CONFIG_GROUP = "goaltracker";
	private static final int UPDATE_INTERVAL = 5; // Game ticks
	private int tickCounter = 0;

	@Override
	protected void startUp() throws Exception
	{
		log.info("HERE!@#!@#");
		panel = new GoalTrackerPanel(this);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/goal_icon.png");

		navButton = NavigationButton.builder()
				.tooltip("Goal Tracker")
				.icon(icon)
				.priority(5)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
		loadGoals();

		log.info("Goal Tracker plugin started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		saveGoals();
		clientToolbar.removeNavigation(navButton);
		log.info("Goal Tracker plugin stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			// Update all goals when logging in
			updateAllGoals();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Only update periodically to avoid unnecessary processing
		if (++tickCounter % UPDATE_INTERVAL != 0)
		{
			return;
		}

		tickCounter = 0;
		updateAllGoals();
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		// Update related skill goals when stats change
		updateSkillGoals(statChanged.getSkill());
	}

	@Provides
	GoalTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GoalTrackerConfig.class);
	}

	public void addGoal(Goal goal)
	{
		goals.add(goal);

		// Add to category map
		goalsByCategory.computeIfAbsent(goal.getType(), k -> new ArrayList<>()).add(goal);

		// Update goal progress
		updateGoalProgress(goal);

		// Save goals to config
		saveGoals();

		// Refresh UI
		panel.rebuild();
	}

	public void removeGoal(Goal goal)
	{
		goals.remove(goal);

		List<Goal> categoryGoals = goalsByCategory.get(goal.getType());
		if (categoryGoals != null)
		{
			categoryGoals.remove(goal);
		}

		// Save goals to config
		saveGoals();

		// Refresh UI
		panel.rebuild();
	}

	public List<Goal> getGoals()
	{
		return new ArrayList<>(goals);
	}

	public List<Goal> getGoalsByCategory(GoalType category)
	{
		return goalsByCategory.getOrDefault(category, new ArrayList<>());
	}

	public void updateAllGoals()
	{
		if (client == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		for (Goal goal : goals)
		{
			updateGoalProgress(goal);
		}

		// Refresh UI if needed
		panel.updateGoalList();
	}

	private void updateSkillGoals(Skill skill)
	{
		if (client == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		// Update only goals related to the changed skill
		for (Goal goal : goals)
		{
			if (goal.getType() == GoalType.SKILL && ((SkillGoal)goal).getSkill() == skill)
			{
				updateGoalProgress(goal);
			}
		}

		// Refresh UI if needed
		panel.updateGoalList();
	}

	private void updateGoalProgress(Goal goal)
	{
		if (client == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		goal.updateProgress(client);

		// Check if goal is newly completed
		if (goal.isCompleted() && !goal.isAcknowledged())
		{
			goal.setCompletionDate(Instant.now());
//			panel.showGoalCompletedNotification(goal);
		}
	}

	private void saveGoals()
	{
		goalManager.saveGoals(goals, configManager, CONFIG_GROUP);
	}

	private void loadGoals()
	{
		goals.clear();
		goalsByCategory.clear();

		List<Goal> loadedGoals = goalManager.loadGoals(configManager, CONFIG_GROUP);
		for (Goal goal : loadedGoals)
		{
			goals.add(goal);
			goalsByCategory.computeIfAbsent(goal.getType(), k -> new ArrayList<>()).add(goal);
		}

		// Update all goals after loading
		if (client != null && client.getGameState() == GameState.LOGGED_IN)
		{
			updateAllGoals();
		}
	}

	public void acknowledgeGoal(Goal goal)
	{
		goal.setAcknowledged(true);
		saveGoals();
	}

	public Client getClient()
	{
		return client;
	}
}