package com.goaltracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GoalManager
{
    private static final String GOALS_KEY = "goals";
    private final Gson gson;

    public GoalManager()
    {
        // Set up a custom Gson instance that can handle polymorphic Goal objects
        GsonBuilder builder = new GsonBuilder();

        // Register type adapter for the Goal class
        builder.registerTypeAdapter(Goal.class, new GoalTypeAdapter());

        // Create the Gson instance
        gson = builder.create();
    }

    public void saveGoals(List<Goal> goals, ConfigManager configManager, String configGroup)
    {
        try
        {
            String json = gson.toJson(goals);
            configManager.setConfiguration(configGroup, GOALS_KEY, json);
        }
        catch (Exception e)
        {
            log.error("Error saving goals", e);
        }
    }

    public List<Goal> loadGoals(ConfigManager configManager, String configGroup)
    {
        List<Goal> goals = new ArrayList<>();

        try
        {
            String json = configManager.getConfiguration(configGroup, GOALS_KEY);

            if (json == null || json.isEmpty())
            {
                return goals;
            }

            // Parse the JSON to an array of Goal objects
            Goal[] loadedGoals = gson.fromJson(json, Goal[].class);

            // Add each goal to the list
            for (Goal goal : loadedGoals)
            {
                if (goal != null)
                {
                    goals.add(goal);
                }
            }
        }
        catch (Exception e)
        {
            log.error("Error loading goals", e);
        }

        return goals;
    }

    /**
     * Custom type adapter for Goal class to handle polymorphism
     */
    private static class GoalTypeAdapter implements JsonSerializer<Goal>, JsonDeserializer<Goal>
    {
        @Override
        public JsonElement serialize(Goal goal, Type type, JsonSerializationContext context)
        {
            return context.serialize(goal);
        }

        @Override
        public Goal deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = json.getAsJsonObject();

            // Get the goal type from the JSON
            GoalType goalType = GoalType.valueOf(jsonObject.get("type").getAsString());

            // Based on the goal type, deserialize to the appropriate subclass
            Class<? extends Goal> goalClass;
            switch (goalType)
            {
                case SKILL:
                    goalClass = SkillGoal.class;
                    break;
                case ITEM:
                    goalClass = ItemGoal.class;
                    break;
                case COMBAT:
                    goalClass = CombatGoal.class;
                    break;
                // Add more cases for other goal types
                default:
                    throw new JsonParseException("Unknown goal type: " + goalType);
            }

            // Deserialize to the appropriate subclass
            return context.deserialize(jsonObject, goalClass);
        }
    }
}