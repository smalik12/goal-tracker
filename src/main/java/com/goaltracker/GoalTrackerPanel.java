package com.goaltracker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ProgressBar;
import net.runelite.client.util.SwingUtil;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.Notifier;

public class GoalTrackerPanel extends PluginPanel {
    private final GoalTrackerPlugin plugin;
    private final JPanel goalListPanel = new JPanel();
    private final JButton addGoalButton = new JButton("Add New Goal");
    private final IconTextField searchBar = new IconTextField();
    private final JPanel noGoalsPanel = new JPanel();

    public GoalTrackerPanel(GoalTrackerPlugin plugin) {
        super(false);
        this.plugin = plugin;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Search bar
        searchBar.setIcon(IconTextField.Icon.SEARCH);
        searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        searchBar.addActionListener(e -> updateGoalList());
        searchBar.addClearListener(() -> updateGoalList());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(0, 5));
        topPanel.add(searchBar, BorderLayout.NORTH);

        // Add goal button
        addGoalButton.addActionListener(e -> openAddGoalDialog());
        topPanel.add(addGoalButton, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Goal list panel
        goalListPanel.setLayout(new BoxLayout(goalListPanel, BoxLayout.Y_AXIS));
        goalListPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        // No goals placeholder
        noGoalsPanel.setLayout(new BorderLayout());
        JLabel noGoalsLabel = new JLabel("No goals created yet");
        noGoalsLabel.setHorizontalAlignment(JLabel.CENTER);
        noGoalsPanel.add(noGoalsLabel, BorderLayout.CENTER);
        noGoalsPanel.setBorder(new EmptyBorder(50, 0, 0, 0));

        // Scroll pane for goal list
        JScrollPane scrollPane = new JScrollPane(goalListPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        add(scrollPane, BorderLayout.CENTER);

        // Build the initial panel
        rebuild();
    }

    public void rebuild() {
        goalListPanel.removeAll();

        List<Goal> goals = plugin.getGoals();

        if (goals.isEmpty()) {
            goalListPanel.add(noGoalsPanel);
        } else {
            // Group goals by category
            for (GoalType type : GoalType.values()) {
                List<Goal> categoryGoals = plugin.getGoalsByCategory(type);

                if (categoryGoals.isEmpty()) {
                    continue;
                }

                // Create category panel
                JPanel categoryPanel = new JPanel();
                categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
                categoryPanel.setBorder(new CompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(ColorScheme.LIGHT_GRAY_COLOR),
                                type.toString(),
                                TitledBorder.CENTER,
                                TitledBorder.TOP,
                                FontManager.getRunescapeBoldFont()
                        ),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));

                // Add goals to category panel
                for (Goal goal : categoryGoals) {
                    // Apply search filter if needed
                    String searchText = searchBar.getText().toLowerCase();
                    if (!searchText.isEmpty()) {
                        if (!goal.getName().toLowerCase().contains(searchText) &&
                                !goal.getDescription().toLowerCase().contains(searchText) &&
                                !goal.getCategory().toLowerCase().contains(searchText)) {
                            continue;
                        }
                    }

                    // Skip completed goals based on config
                    if (goal.isCompleted()) {
                        continue;
                    }

                    categoryPanel.add(createGoalPanel(goal));
                }

                // Only add category if it has visible goals
                if (categoryPanel.getComponentCount() > 0) {
                    goalListPanel.add(categoryPanel);
                }
            }
        }

        goalListPanel.revalidate();
        goalListPanel.repaint();
    }

    private JPanel createGoalPanel(Goal goal) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 0));
        panel.setBorder(new EmptyBorder(5, 0, 5, 0));

        // Status color indicator
        JPanel statusIndicator = new JPanel();
        statusIndicator.setPreferredSize(new Dimension(5, 0));

        if (goal.isCompleted()) {
            statusIndicator.setBackground(ColorScheme.PROGRESS_COMPLETE_COLOR);
        } else {
            statusIndicator.setBackground(ColorScheme.PROGRESS_INPROGRESS_COLOR);
        }

        panel.add(statusIndicator, BorderLayout.WEST);

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;

        // Goal name
        JLabel nameLabel = new JLabel(goal.getName());
        nameLabel.setFont(FontManager.getRunescapeBoldFont());
        c.insets = new Insets(5, 5, 0, 0);
        contentPanel.add(nameLabel, c);

        // Goal description
        if (goal.getDescription() != null && !goal.getDescription().isEmpty()) {
            JLabel descLabel = new JLabel(goal.getDescription());
            descLabel.setFont(FontManager.getRunescapeSmallFont());
            descLabel.setForeground(Color.LIGHT_GRAY);
            c.gridy++;
            c.insets = new Insets(2, 5, 0, 0);
            contentPanel.add(descLabel, c);
        }

        // Progress bar
        ProgressBar progressBar = new ProgressBar();
        progressBar.setMaximumValue(goal.getTargetValue());
        progressBar.setValue(goal.getCurrentProgress());
        progressBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 80, 15));

        if (goal.isCompleted()) {
            progressBar.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
        } else {
            progressBar.setForeground(ColorScheme.PROGRESS_INPROGRESS_COLOR);
        }

        c.gridy++;
        c.insets = new Insets(5, 5, 5, 5);
        contentPanel.add(progressBar, c);

        // Progress text
        String progressText;
        if (goal instanceof SkillGoal) {
            progressText = ((SkillGoal) goal).getFormattedProgress();
        } else if (goal instanceof CombatGoal) {
            progressText = ((CombatGoal) goal).getFormattedProgress();
        } else {
            progressText = goal.getCurrentProgress() + "/" + goal.getTargetValue();
        }

        JLabel progressLabel = new JLabel(progressText + " (" + goal.getProgressPercentage() + "%)");
        progressLabel.setFont(FontManager.getRunescapeSmallFont());
        progressLabel.setHorizontalAlignment(JLabel.RIGHT);

        c.gridy++;
        c.insets = new Insets(0, 5, 5, 5);
        contentPanel.add(progressLabel, c);

        // Category tag if present
        if (goal.getCategory() != null && !goal.getCategory().isEmpty()) {
            JPanel tagPanel = new JPanel();
            tagPanel.setLayout(new BorderLayout());
            tagPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

            JLabel tagLabel = new JLabel(goal.getCategory());
            tagLabel.setFont(FontManager.getRunescapeSmallFont());
            tagLabel.setForeground(ColorScheme.PROGRESS_INPROGRESS_COLOR);
            tagLabel.setBorder(new EmptyBorder(0, 5, 5, 0));

            tagPanel.add(tagLabel, BorderLayout.WEST);

            c.gridy++;
            contentPanel.add(tagPanel, c);
        }

        panel.add(contentPanel, BorderLayout.CENTER);

        // Add right-click menu
        JPopupMenu popupMenu = new JPopupMenu();
//        JMenuItem editItem = new JMenuItem("Edit");
        JMenuItem deleteItem = new JMenuItem("Delete");

//        editItem.addActionListener(e -> editGoal(goal));
        deleteItem.addActionListener(e -> removeGoal(goal));

//        popupMenu.add(editItem);
        popupMenu.add(deleteItem);

        if (goal.isCompleted() && !goal.isAcknowledged()) {
            JMenuItem acknowledgeItem = new JMenuItem("Acknowledge Completion");
            acknowledgeItem.addActionListener(e -> acknowledgeGoalCompletion(goal));
            popupMenu.add(acknowledgeItem);
        }

        panel.setComponentPopupMenu(popupMenu);

        // Add mouse listener for double-click to edit
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
//                    editGoal(goal);
                }
            }
        });

        return panel;
    }

    public void updateGoalList() {
        SwingUtilities.invokeLater(this::rebuild);
    }

    private void openAddGoalDialog() {
        AddGoalDialog dialog = new AddGoalDialog(plugin);
        dialog.setVisible(true);
    }

//    private void editGoal(Goal goal)
//    {
//        EditGoalDialog dialog = new EditGoalDialog(plugin, goal);
//        dialog.setVisible(true);
//    }

    private void removeGoal(Goal goal) {
        plugin.removeGoal(goal);
    }

    private void acknowledgeGoalCompletion(Goal goal) {
        plugin.acknowledgeGoal(goal);
        rebuild();
    }

//    public void showGoalCompletedNotification(Goal goal)
//    {
//        if (plugin.getConfig().notifyOnCompletion())
//        {
//            // This would typically use the client's notifier
//            // For simplicity in this example, we'll just update the UI
//            SwingUtilities.invokeLater(this::rebuild);
//        }
//    }
}