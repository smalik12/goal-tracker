package com.goaltracker;

import net.runelite.api.Skill;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AddGoalDialog extends JDialog
{
    private final GoalTrackerPlugin plugin;

    private GoalType selectedGoalType = GoalType.SKILL;
    private final JPanel contentPanel;
    private final JPanel goalConfigPanel;

    // Common fields
    private final FlatTextField nameTextField = new FlatTextField();
    private final FlatTextField descTextField = new FlatTextField();
    private final FlatTextField categoryTextField = new FlatTextField();
    private final FlatTextField targetValueField = new FlatTextField();

    // Skill goal fields
    private final JComboBox<Skill> skillComboBox = new JComboBox<>(Skill.values());
    private final JComboBox<SkillGoalType> skillGoalTypeComboBox = new JComboBox<>(SkillGoalType.values());

    // Item goal fields
    private final FlatTextField itemIdField = new FlatTextField();
    private final JComboBox<ItemGoalType> itemGoalTypeComboBox = new JComboBox<>(ItemGoalType.values());

    // Combat goal fields
    private final FlatTextField npcNameField = new FlatTextField();
    private final FlatTextField npcIdField = new FlatTextField();
    private final JComboBox<CombatGoalType> combatGoalTypeComboBox = new JComboBox<>(CombatGoalType.values());

    public AddGoalDialog(GoalTrackerPlugin plugin)
    {
        super();
        this.plugin = plugin;

        setTitle("Add New Goal");
        setSize(400, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setModalityType(ModalityType.APPLICATION_MODAL);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Goal type selection
        JPanel goalTypePanel = new JPanel();
        goalTypePanel.setLayout(new BoxLayout(goalTypePanel, BoxLayout.Y_AXIS));

        JLabel goalTypeLabel = new JLabel("Goal Type:");
        goalTypeLabel.setFont(FontManager.getRunescapeBoldFont());
        goalTypePanel.add(goalTypeLabel);

        JComboBox<GoalType> goalTypeComboBox = new JComboBox<>(GoalType.values());
        goalTypeComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                selectedGoalType = (GoalType) e.getItem();
                updateGoalConfigPanel();
            }
        });
        goalTypePanel.add(goalTypeComboBox);

        contentPanel.add(goalTypePanel, BorderLayout.NORTH);

        // Goal configuration panel (changes based on goal type)
        goalConfigPanel = new JPanel();
        goalConfigPanel.setLayout(new BoxLayout(goalConfigPanel, BoxLayout.Y_AXIS));
        contentPanel.add(goalConfigPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        JButton addButton = new JButton("Add Goal");
        addButton.setBackground(ColorScheme.PROGRESS_COMPLETE_COLOR);
        addButton.addActionListener(e -> addGoal());

        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(contentPanel);

        // Initialize the goal config panel with the default goal type
        updateGoalConfigPanel();

        // Close on ESC
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                dispose();
            }
        });
    }

    private void updateGoalConfigPanel()
    {
        goalConfigPanel.removeAll();

        // Add common fields first
        addCommonFields();

        // Add goal type specific fields
        switch (selectedGoalType)
        {
            case SKILL:
                addSkillGoalFields();
                break;
            case ITEM:
                addItemGoalFields();
                break;
            case COMBAT:
                addCombatGoalFields();
                break;
            case QUEST:
            case ACHIEVEMENT:
            case OTHER:
                // Not implemented yet
                break;
        }

        goalConfigPanel.revalidate();
        goalConfigPanel.repaint();
    }

    private void addCommonFields()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 0, 5));
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Name field
        JLabel nameLabel = new JLabel("Goal Name:");
        nameLabel.setFont(FontManager.getRunescapeBoldFont());
        panel.add(nameLabel);
        nameTextField.setPreferredSize(new Dimension(100, 25));
        panel.add(nameTextField);

        // Description field
        JLabel descLabel = new JLabel("Description (optional):");
        descLabel.setFont(FontManager.getRunescapeBoldFont());
        panel.add(descLabel);
        descTextField.setPreferredSize(new Dimension(100, 25));
        panel.add(descTextField);

        // Category field
        JLabel categoryLabel = new JLabel("Category (optional):");
        categoryLabel.setFont(FontManager.getRunescapeBoldFont());
        panel.add(categoryLabel);
        categoryTextField.setPreferredSize(new Dimension(100, 25));
        panel.add(categoryTextField);

        // Target value field
        JLabel targetLabel = new JLabel("Target Value:");
        targetLabel.setFont(FontManager.getRunescapeBoldFont());
        panel.add(targetLabel);
        targetValueField.setPreferredSize(new Dimension(100, 25));
        panel.add(targetValueField);

        goalConfigPanel.add(panel);
    }

    private void addSkillGoalFields()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 0, 5));
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Skill selection
        JLabel skillLabel = new JLabel("Skill:");
        skillLabel.setFont(FontManager.getRunescapeBoldFont());
        panel.add(skillLabel);
        panel.add(skillComboBox);

        // Skill goal type selection
        JLabel typeLabel = new JLabel("Goal Type:");
        typeLabel.setFont(FontManager.getRunescapeBoldFont());
        panel.add(typeLabel);
        panel.add(skillGoalTypeComboBox);

        goalConfigPanel.add(panel);
    }

    private void addItemGoalFields()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 0, 5));
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Item ID field
        JLabel itemIdLabel = new JLabel("Item ID:");
        itemIdLabel.setFont(FontManager.getRunescapeBoldFont());
        panel.add(itemIdLabel);
        itemIdField.setPreferredSize(new Dimension(100, 25));
        panel.add(itemIdField);

        // Item goal type selection
        JLabel typeLabel = new JLabel("Item Goal Type:");
        typeLabel.setFont(FontManager.getRunescapeBoldFont());
        panel.add(typeLabel);
        panel.add(itemGoalTypeComboBox);

        goalConfigPanel.add(panel);
    }

    private void addCombatGoalFields()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1, 0, 5));
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // NPC Name field
        JLabel npcNameLabel = new JLabel("NPC Name:");
        npcNameLabel.setFont(FontManager.getRunescapeBoldFont());
        panel.add(npcNameLabel);
        npcNameField.setPreferredSize(new Dimension(100, 25));
        panel.add(npcNameField);

        // NPC ID field
        JLabel npcIdLabel = new JLabel("NPC ID (optional):");
        npcIdLabel.setFont(FontManager.getRunescapeBoldFont());
        panel.add(npcIdLabel);
        npcIdField.setPreferredSize(new Dimension(100, 25));
        panel.add(npcIdField);

        // Combat goal type selection
        JLabel typeLabel = new JLabel("Combat Goal Type:");
        typeLabel.setFont(FontManager.getRunescapeBoldFont());
        panel.add(typeLabel);
        panel.add(combatGoalTypeComboBox);

        goalConfigPanel.add(panel);
    }

    private void addGoal()
    {
        try
        {
            // Get common fields
            String name = nameTextField.getText();
            if (name == null || name.trim().isEmpty())
            {
                JOptionPane.showMessageDialog(this, "Goal name is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String description = descTextField.getText();
            String category = categoryTextField.getText();

            String targetValueStr = targetValueField.getText();
            if (targetValueStr == null || targetValueStr.trim().isEmpty())
            {
                JOptionPane.showMessageDialog(this, "Target value is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int targetValue;
            try
            {
                targetValue = Integer.parseInt(targetValueStr);
                if (targetValue <= 0)
                {
                    throw new NumberFormatException("Value must be positive");
                }
            }
            catch (NumberFormatException e)
            {
                JOptionPane.showMessageDialog(this, "Target value must be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create goal based on selected type
            Goal goal = null;

            switch (selectedGoalType)
            {
                case SKILL:
                    Skill skill = (Skill) skillComboBox.getSelectedItem();
                    SkillGoalType skillGoalType = (SkillGoalType) skillGoalTypeComboBox.getSelectedItem();
                    goal = new SkillGoal(name, description, skill, skillGoalType, targetValue, category);
                    break;

                case ITEM:
                    String itemIdStr = itemIdField.getText();
                    if (itemIdStr == null || itemIdStr.trim().isEmpty())
                    {
                        JOptionPane.showMessageDialog(this, "Item ID is required.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int itemId;
                    try
                    {
                        itemId = Integer.parseInt(itemIdStr);
                    }
                    catch (NumberFormatException e)
                    {
                        JOptionPane.showMessageDialog(this, "Item ID must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    ItemGoalType itemGoalType = (ItemGoalType) itemGoalTypeComboBox.getSelectedItem();
                    goal = new ItemGoal(name, description, itemId, itemGoalType, targetValue, category);
                    break;

                case COMBAT:
                    String npcName = npcNameField.getText();
                    if (npcName == null || npcName.trim().isEmpty())
                    {
                        JOptionPane.showMessageDialog(this, "NPC name is required.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int npcId = 0;
                    String npcIdStr = npcIdField.getText();
                    if (npcIdStr != null && !npcIdStr.trim().isEmpty())
                    {
                        try
                        {
                            npcId = Integer.parseInt(npcIdStr);
                        }
                        catch (NumberFormatException e)
                        {
                            JOptionPane.showMessageDialog(this, "NPC ID must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    CombatGoalType combatGoalType = (CombatGoalType) combatGoalTypeComboBox.getSelectedItem();
                    goal = new CombatGoal(name, description, npcName, npcId, combatGoalType, targetValue, category);
                    break;

                case QUEST:
                case ACHIEVEMENT:
                case OTHER:
                    // Not implemented yet
                    JOptionPane.showMessageDialog(this, "This goal type is not implemented yet.", "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
                    return;
            }

            if (goal != null)
            {
                plugin.addGoal(goal);
                dispose();
            }
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(this, "Error adding goal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}