package com.goaltracker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.runelite.api.Client;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Goal
{
    private String id;
    private String name;
    private String description;
    private GoalType type;
    private GoalStatus status;
    private Instant creationDate;
    private Instant completionDate;
    private String category;
    private int currentProgress;
    private int targetValue;
    private boolean acknowledged;

    public Goal(String name, String description, GoalType type, int targetValue, String category)
    {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.type = type;
        this.status = GoalStatus.IN_PROGRESS;
        this.creationDate = Instant.now();
        this.category = category;
        this.targetValue = targetValue;
        this.currentProgress = 0;
        this.acknowledged = false;
    }

    /**
     * Update the goal's progress based on client data
     */
    public abstract void updateProgress(Client client);

    /**
     * Check if the goal is completed
     */
    public boolean isCompleted()
    {
        return status == GoalStatus.COMPLETED;
    }

    /**
     * Calculate progress percentage (0-100)
     */
    public int getProgressPercentage()
    {
        if (targetValue == 0)
        {
            return 0;
        }

        int percentage = (int) (((double) currentProgress / targetValue) * 100);
        return Math.min(percentage, 100);
    }

    /**
     * Mark goal as completed if target is reached
     */
    protected void checkCompletion()
    {
        if (currentProgress >= targetValue && status != GoalStatus.COMPLETED)
        {
            status = GoalStatus.COMPLETED;
            completionDate = Instant.now();
        }
    }
}

enum GoalStatus
{
    IN_PROGRESS,
    COMPLETED,
    PAUSED
}

enum GoalType
{
    SKILL,
    QUEST,
    ITEM,
    ACHIEVEMENT,
    COMBAT,
    OTHER
}