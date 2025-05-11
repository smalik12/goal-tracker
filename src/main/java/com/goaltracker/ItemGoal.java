package com.goaltracker;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;

@Data
@EqualsAndHashCode(callSuper = true)
public class ItemGoal extends Goal
{
    private int itemId;
    private ItemGoalType itemGoalType;

    public ItemGoal()
    {
        super();
    }

    public ItemGoal(String name, String description, int itemId, ItemGoalType itemGoalType, int targetValue, String category)
    {
        super(name, description, GoalType.ITEM, targetValue, category);
        this.itemId = itemId;
        this.itemGoalType = itemGoalType;
    }

    @Override
    public void updateProgress(Client client)
    {
        if (client == null)
        {
            return;
        }

        int count = 0;

        switch (itemGoalType)
        {
            case INVENTORY:
                ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
                if (inventory != null)
                {
                    count = getItemCount(inventory, itemId);
                }
                break;
            case BANK:
                ItemContainer bank = client.getItemContainer(InventoryID.BANK);
                if (bank != null)
                {
                    count = getItemCount(bank, itemId);
                }
                break;
            case EQUIPMENT:
                ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
                if (equipment != null)
                {
                    count = getItemCount(equipment, itemId);
                }
                break;
            case ALL:
                int inventoryCount = 0;
                int bankCount = 0;
                int equipmentCount = 0;

                ItemContainer inv = client.getItemContainer(InventoryID.INVENTORY);
                if (inv != null)
                {
                    inventoryCount = getItemCount(inv, itemId);
                }

                ItemContainer bnk = client.getItemContainer(InventoryID.BANK);
                if (bnk != null)
                {
                    bankCount = getItemCount(bnk, itemId);
                }

                ItemContainer equip = client.getItemContainer(InventoryID.EQUIPMENT);
                if (equip != null)
                {
                    equipmentCount = getItemCount(equip, itemId);
                }

                count = inventoryCount + bankCount + equipmentCount;
                break;
        }

        setCurrentProgress(count);
        checkCompletion();
    }

    private int getItemCount(ItemContainer container, int id)
    {
        int count = 0;

        if (container == null)
        {
            return count;
        }

        for (int i = 0; i < container.getItems().length; i++)
        {
            net.runelite.api.Item item = container.getItems()[i];
            if (item == null)
            {
                continue;
            }

            if (item.getId() == id)
            {
                count += item.getQuantity();
            }
        }

        return count;
    }
}

enum ItemGoalType
{
    INVENTORY,
    BANK,
    EQUIPMENT,
    ALL
}