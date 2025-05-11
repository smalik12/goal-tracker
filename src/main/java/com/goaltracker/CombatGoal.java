package com.goaltracker;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;

@Data
@EqualsAndHashCode(callSuper = true)
public class CombatGoal extends Goal
{
    private String npcName;
    private int npcId;
    private CombatGoalType combatGoalType;
    private int initialKillCount;

    public CombatGoal()
    {
        super();
    }

    public CombatGoal(String name, String description, String npcName, int npcId,
                      CombatGoalType combatGoalType, int targetValue, String category)
    {
        super(name, description, GoalType.COMBAT, targetValue, category);
        this.npcName = npcName;
        this.npcId = npcId;
        this.combatGoalType = combatGoalType;
        this.initialKillCount = -1;
    }

    @Override
    public void updateProgress(Client client)
    {
        if (client == null)
        {
            return;
        }

        // For now, we'll just update based on combat level or slayer task
        // In a real implementation, you would need to track kills via other methods
        switch (combatGoalType)
        {
            case SLAYER_TASK:
                int currentAmount = client.getVarpValue(VarPlayer.SLAYER_TASK_SIZE);
                // Calculate progress as initial task size - current amount
                if (initialKillCount == -1)
                {
                    // First time checking, so set initial kill count
                    initialKillCount = getTargetValue() + currentAmount;
                }

                // Task progress is inverse - starts at full amount and decreases
                setCurrentProgress(initialKillCount - currentAmount);
                break;

            case BOSS_KILLS:
                // This would require tracking boss kill count from the collection log
                // or other sources, which is beyond the scope of this simple example
                // For a real implementation, you would need to track kills via chat messages
                // or other indicators
                break;

            case MONSTER_KILLS:
                // Similar to boss kills, would need to track via chat messages
                // or other indicators
                break;
        }

        checkCompletion();
    }

    public String getFormattedProgress()
    {
        switch (combatGoalType)
        {
            case SLAYER_TASK:
                return getCurrentProgress() + "/" + getTargetValue() + " killed";
            case BOSS_KILLS:
                return getCurrentProgress() + "/" + getTargetValue() + " boss kills";
            case MONSTER_KILLS:
                return getCurrentProgress() + "/" + getTargetValue() + " kills";
            default:
                return getCurrentProgress() + "/" + getTargetValue();
        }
    }
}

enum CombatGoalType
{
    SLAYER_TASK,
    BOSS_KILLS,
    MONSTER_KILLS
}