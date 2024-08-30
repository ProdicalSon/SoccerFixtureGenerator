package com.example.soccer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SoccerFixtureGeneratorUI extends JFrame {

    private JTextField teamCountField;
    private JTextArea teamNamesArea;
    private JTextArea fixturesArea;
    private JTextField resultField;
    private JTextArea leaderboardArea;
    private List<Team> teams;
    private List<String> fixtures;
    private int currentFixtureIndex;

    public SoccerFixtureGeneratorUI() {
        // Set up the main frame
        setTitle("Soccer Fixture Generator & Leaderboard");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2));

        inputPanel.add(new JLabel("Number of Teams:"));
        teamCountField = new JTextField();
        inputPanel.add(teamCountField);

        inputPanel.add(new JLabel("Enter Team Names (comma separated):"));
        teamNamesArea = new JTextArea(3, 20);
        inputPanel.add(new JScrollPane(teamNamesArea));

        JButton generateButton = new JButton("Generate Fixtures");
        inputPanel.add(generateButton);

        fixturesArea = new JTextArea(10, 40);
        fixturesArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(fixturesArea);

        // Add input panel and output area to the frame
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Leaderboard and result input
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new GridLayout(2, 1));

        resultField = new JTextField();
        resultPanel.add(new JLabel("Enter Result (e.g., Team1 2-1 Team2):"));
        resultPanel.add(resultField);

        JButton updateResultButton = new JButton("Update Result");
        resultPanel.add(updateResultButton);

        leaderboardArea = new JTextArea(10, 40);
        leaderboardArea.setEditable(false);
        JScrollPane leaderboardScrollPane = new JScrollPane(leaderboardArea);

        add(resultPanel, BorderLayout.SOUTH);
        add(leaderboardScrollPane, BorderLayout.EAST);

        // Add event listener to the buttons
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateFixtures();
            }
        });

        updateResultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateResult();
            }
        });

        teams = new ArrayList<>();
        fixtures = new ArrayList<>();
        currentFixtureIndex = 0;
    }

    private void generateFixtures() {
        try {
            int numberOfTeams = Integer.parseInt(teamCountField.getText());

            if (numberOfTeams % 2 != 0 || numberOfTeams < 2) {
                JOptionPane.showMessageDialog(this, "Number of teams must be even and at least 2.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] teamNames = teamNamesArea.getText().split(",");
            if (teamNames.length != numberOfTeams) {
                JOptionPane.showMessageDialog(this, "Please enter exactly " + numberOfTeams + " team names.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            teams.clear();
            fixtures.clear();
            fixturesArea.setText("");
            leaderboardArea.setText("");

            for (String name : teamNames) {
                String trimmedName = name.trim();
                if (trimmedName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Team names cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                teams.add(new Team(trimmedName));
            }

            // Generate and display fixtures
            fixturesArea.append("First Leg Fixtures:\n");
            for (int round = 1; round < numberOfTeams; round++) {
                fixturesArea.append("Round " + round + ":\n");
                for (int i = 0; i < numberOfTeams / 2; i++) {
                    String home = teams.get(i).getName();
                    String away = teams.get(numberOfTeams - 1 - i).getName();
                    fixtures.add(home + " vs " + away);
                    fixturesArea.append(home + " vs " + away + "\n");
                }
                Collections.rotate(teams.subList(1, teams.size()), 1);
            }

            // Add Second Leg
            fixturesArea.append("\nSecond Leg Fixtures:\n");
            for (int round = 1; round < numberOfTeams; round++) {
                fixturesArea.append("Round " + round + ":\n");
                for (int i = 0; i < numberOfTeams / 2; i++) {
                    String home = teams.get(numberOfTeams - 1 - i).getName();
                    String away = teams.get(i).getName();
                    fixtures.add(home + " vs " + away);
                    fixturesArea.append(home + " vs " + away + "\n");
                }
                Collections.rotate(teams.subList(1, teams.size()), 1);
            }

            currentFixtureIndex = 0;

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number of teams.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateResult() {
        try {
            String result = resultField.getText().trim();
            String[] parts = result.split(" ");
            if (parts.length != 3 || !parts[1].contains("-")) {
                JOptionPane.showMessageDialog(this, "Please enter result in the format: Team1 2-1 Team2", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String team1Name = parts[0];
            String team2Name = parts[2];
            String[] score = parts[1].split("-");
            int team1Goals = Integer.parseInt(score[0]);
            int team2Goals = Integer.parseInt(score[1]);

            Team team1 = findTeamByName(team1Name);
            Team team2 = findTeamByName(team2Name);

            if (team1 == null || team2 == null) {
                JOptionPane.showMessageDialog(this, "Invalid team names entered.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update the teams' stats
            team1.updateStats(team1Goals, team2Goals);
            team2.updateStats(team2Goals, team1Goals);

            // Update leaderboard
            updateLeaderboard();

            // Clear result input
            resultField.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid score.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Team findTeamByName(String name) {
        for (Team team : teams) {
            if (team.getName().equalsIgnoreCase(name)) {
                return team;
            }
        }
        return null;
    }

    private void updateLeaderboard() {
        // Sort teams by points, then by goal difference
        teams.sort(Comparator.comparing(Team::getPoints).thenComparing(Team::getGoalDifference).reversed());

        // Display leaderboard
        leaderboardArea.setText("Leaderboard:\n");
        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            leaderboardArea.append((i + 1) + ". " + team.getName() + " - Points: " + team.getPoints()
                    + ", GD: " + team.getGoalDifference() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SoccerFixtureGeneratorUI().setVisible(true);
            }
        });
    }
}

class Team {
    private String name;
    private int points;
    private int goalsScored;
    private int goalsConceded;

    public Team(String name) {
        this.name = name;
        this.points = 0;
        this.goalsScored = 0;
        this.goalsConceded = 0;
    }
    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public int getGoalDifference() {
        return goalsScored - goalsConceded;
    }

    public void updateStats(int goalsFor, int goalsAgainst) {
        goalsScored += goalsFor;
        goalsConceded += goalsAgainst;

        if (goalsFor > goalsAgainst) {
            points += 3; // Win
        } else if (goalsFor == goalsAgainst)
            if (goalsFor == goalsAgainst) {
                points += 1; // Draw
            }
            // No points for a loss, so no action needed in that case
        }
    }
