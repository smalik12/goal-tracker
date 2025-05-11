package com.goaltracker;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.Skill;

@Data
@EqualsAndHashCode(callSuper = true)
public class SkillGoal extends Goal
{
    private Skill skill;
    private SkillGoalType goalType;

    public SkillGoal()
    {
        super();
    }

    public SkillGoal(String name, String description, Skill skill, SkillGoalType goalType, int targetValue, String category)
    {
        super(name, description, GoalType.SKILL, targetValue, category);
        this.skill = skill;
        this.goalType = goalType;
    }

    @Override
    public void updateProgress(Client client)
    {
        if (client == null)
        {
            return;
        }

        switch (goalType)
        {
            case LEVEL:
                setCurrentProgress(client.getRealSkillLevel(skill));
                break;
            case EXPERIENCE:
                setCurrentProgress(client.getSkillExperience(skill));
                break;
            case VIRTUAL_LEVEL:
                int xp = client.getSkillExperience(skill);
                int virtualLevel = Experience.getLevelForXp(xp);
                setCurrentProgress(virtualLevel);
                break;
        }

        checkCompletion();
    }

    public String getFormattedProgress()
    {
        switch (goalType)
        {
            case LEVEL:
            case VIRTUAL_LEVEL:
                return "Level " + getCurrentProgress() + "/" + getTargetValue();
            case EXPERIENCE:
                return getCurrentProgress() + "/" +
                        getTargetValue() + " XP";
            default:
                return getCurrentProgress() + "/" + getTargetValue();
        }
    }
}

enum SkillGoalType
{
    LEVEL,
    EXPERIENCE,
    VIRTUAL_LEVEL
}