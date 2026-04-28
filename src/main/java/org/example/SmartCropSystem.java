package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import org.example.irrigation.IrrigationAdvisor;

public class SmartCropSystem {

    static JLabel imageLabel;
    static JTextArea diseaseResultArea;
    static JTextArea irrigationResultArea;
    static JComboBox<String> cropBox;
    static JTextField cityField;
    static JTextField areaField;
    static JComboBox<String> unitBox;
    static JComboBox<String> soilBox;
    static JTextField daysField;
    static DefaultTableModel tableModel;

    static File selectedFile;

    static double[] getModelAccuracies() {
        double cnn = 0, svm = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader("src/main/java/results.txt"));
            String line;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("CNN:")) {
                    cnn = Double.parseDouble(line.split(":")[1]) * 100;
                } else if (line.startsWith("SVM:")) {
                    svm = Double.parseDouble(line.split(":")[1]) * 100;
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new double[]{svm, cnn};
    }

    static class GraphPanel extends JPanel {

        double svm, cnn;

        GraphPanel(double svm, double cnn) {
            this.svm = svm;
            this.cnn = cnn;
            setBackground(new Color(24,24,24));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int baseY = 250;

            g.setColor(Color.RED);
            g.fillRect(100, baseY - (int) svm, 100, (int) svm);
            g.setColor(Color.WHITE);
            g.drawString("SVM", 130, baseY + 20);
            g.drawString((int) svm + "%", 130, baseY - (int) svm - 5);

            g.setColor(Color.GREEN);
            g.fillRect(300, baseY - (int) cnn, 100, (int) cnn);
            g.setColor(Color.WHITE);
            g.drawString("CNN", 330, baseY + 20);
            g.drawString((int) cnn + "%", 330, baseY - (int) cnn - 5);
        }
    }

    public static void main(String[] args) {

        Color bg = new Color(24, 24, 24);
        Color card = new Color(40, 40, 40);
        Color accent = new Color(0, 180, 90);
        Color text = Color.WHITE;

        JFrame frame = new JFrame("Smart Farming System");
        frame.setSize(1100, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(bg);
        frame.setLayout(new BorderLayout(15,15));

        JLabel title = new JLabel("Smart Farming System", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(text);
        title.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));

        // LEFT PANEL (Disease Detection)
        JPanel leftCard = new JPanel();
        leftCard.setBackground(card);
        leftCard.setLayout(new BorderLayout(10,10));
        leftCard.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JLabel leftTitle = new JLabel("Disease Detection Module", JLabel.CENTER);
        leftTitle.setForeground(text);
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        leftCard.add(leftTitle, BorderLayout.NORTH);

        JPanel imagePanel = new JPanel(new BorderLayout(5, 5));
        imagePanel.setBackground(card);

        imageLabel = new JLabel("Upload Image", JLabel.CENTER);
        imageLabel.setForeground(Color.GRAY);
        imageLabel.setPreferredSize(new Dimension(350,250));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        JButton uploadBtn = new JButton("Upload Image");
        styleButton(uploadBtn, accent);

        uploadBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                selectedFile = fc.getSelectedFile();
                ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(350, 250, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(img));
                imageLabel.setText("");
            }
        });

        imagePanel.add(imageLabel, BorderLayout.CENTER);
        imagePanel.add(uploadBtn, BorderLayout.SOUTH);

        diseaseResultArea = new JTextArea();
        diseaseResultArea.setBackground(bg);
        diseaseResultArea.setForeground(text);
        diseaseResultArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        diseaseResultArea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        diseaseResultArea.setEditable(false);
        JScrollPane diseaseScroll = new JScrollPane(diseaseResultArea);
        diseaseScroll.setPreferredSize(new Dimension(350, 150));

        JPanel leftBottomPanel = new JPanel(new BorderLayout(5, 5));
        leftBottomPanel.setBackground(card);
        
        JButton detectBtn = new JButton("Detect Disease");
        styleButton(detectBtn, accent);
        JButton graphBtn = new JButton("Model Comparison");
        styleButton(graphBtn, new Color(70,130,180));

        JPanel leftButtons = new JPanel(new GridLayout(1,2,5,5));
        leftButtons.setBackground(card);
        leftButtons.add(detectBtn);
        leftButtons.add(graphBtn);

        leftBottomPanel.add(leftButtons, BorderLayout.NORTH);
        leftBottomPanel.add(diseaseScroll, BorderLayout.CENTER);

        leftCard.add(imagePanel, BorderLayout.CENTER);
        leftCard.add(leftBottomPanel, BorderLayout.SOUTH);

        // RIGHT PANEL (Irrigation Scheduling)
        JPanel rightCard = new JPanel();
        rightCard.setBackground(card);
        rightCard.setLayout(new BorderLayout(10,10));
        rightCard.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JLabel rightTitle = new JLabel("Irrigation Scheduling Module", JLabel.CENTER);
        rightTitle.setForeground(text);
        rightTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10)); // Changed to 6 rows
        inputPanel.setBackground(card);

        JLabel cropLabel = new JLabel("Crop:");
        cropLabel.setForeground(text);
        cropBox = new JComboBox<>(new String[]{"Wheat", "Rice", "Maize", "Sugarcane", "Cotton", "Soybean", "Mustard"});
        cropBox.setBackground(card);
        cropBox.setForeground(text);
        
        JLabel cityLabel = new JLabel("City:");
        cityLabel.setForeground(text);
        cityField = new JTextField();
        cityField.setBackground(bg);
        cityField.setForeground(text);
        cityField.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        JLabel areaLabel = new JLabel("Land Area:");
        areaLabel.setForeground(text);
        areaField = new JTextField();
        areaField.setBackground(bg);
        areaField.setForeground(text);
        areaField.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        
        JLabel unitLabel = new JLabel("Land Unit:");
        unitLabel.setForeground(text);
        unitBox = new JComboBox<>(new String[]{"Acre", "Hectare"});
        unitBox.setBackground(card);
        unitBox.setForeground(text);
        
        JLabel soilLabel = new JLabel("Soil Type:");
        soilLabel.setForeground(text);
        soilBox = new JComboBox<>(new String[]{"Sandy", "Loamy", "Clay"});
        soilBox.setBackground(card);
        soilBox.setForeground(text);
        
        JLabel daysLabel = new JLabel("Last Irrigation (days):");
        daysLabel.setForeground(text);
        daysField = new JTextField();
        daysField.setBackground(bg);
        daysField.setForeground(text);
        daysField.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        inputPanel.add(cropLabel);
        inputPanel.add(cropBox);
        inputPanel.add(cityLabel);
        inputPanel.add(cityField);
        inputPanel.add(areaLabel);
        inputPanel.add(areaField);
        inputPanel.add(unitLabel);
        inputPanel.add(unitBox);
        inputPanel.add(soilLabel);
        inputPanel.add(soilBox);
        inputPanel.add(daysLabel);
        inputPanel.add(daysField);

        JPanel rightTopPanel = new JPanel(new BorderLayout(10, 10));
        rightTopPanel.setBackground(card);
        rightTopPanel.add(rightTitle, BorderLayout.NORTH);
        rightTopPanel.add(inputPanel, BorderLayout.CENTER);

        irrigationResultArea = new JTextArea();
        irrigationResultArea.setBackground(bg);
        irrigationResultArea.setForeground(text);
        irrigationResultArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        irrigationResultArea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        irrigationResultArea.setEditable(false);
        JScrollPane irrigationScroll = new JScrollPane(irrigationResultArea);

        JButton irrigationBtn = new JButton("Get Irrigation Advice");
        styleButton(irrigationBtn, new Color(30,144,255));

        rightCard.add(rightTopPanel, BorderLayout.NORTH);
        rightCard.add(irrigationScroll, BorderLayout.CENTER);
        rightCard.add(irrigationBtn, BorderLayout.SOUTH);

        // Button Actions
        detectBtn.addActionListener(e -> {
            if (selectedFile == null) {
                diseaseResultArea.setText("Please upload an image first!");
                return;
            }

            String disease = Test.predictDisease(selectedFile);
            if (disease.equals("Error")) {
                diseaseResultArea.setText("Error detecting disease. Try another image.");
                return;
            }

            String solution = Test.solution(disease);
            diseaseResultArea.setText("Disease: " + disease + "\nConfidence: Detected\n\nSuggested Treatment:\n" + solution);

            String crop = (String) cropBox.getSelectedItem();
            tableModel.addRow(new Object[]{crop, disease, "Detected"});
        });

        graphBtn.addActionListener(e -> {
            double[] acc = getModelAccuracies();
            JFrame graphFrame = new JFrame("Model Accuracy Comparison");
            graphFrame.setSize(500, 350);
            graphFrame.add(new GraphPanel(acc[0], acc[1]));
            graphFrame.setVisible(true);
        });

        irrigationBtn.addActionListener(e -> {
            String crop = (String) cropBox.getSelectedItem();
            String city = cityField.getText().trim();
            String areaStr = areaField.getText().trim();
            String unit = (String) unitBox.getSelectedItem();
            String soil = (String) soilBox.getSelectedItem();
            String daysStr = daysField.getText().trim();

            if (city.isEmpty()) {
                irrigationResultArea.setText("Please enter a city name.");
                return;
            }
            if (areaStr.isEmpty()) {
                irrigationResultArea.setText("Please enter the land area.");
                return;
            }
            if (daysStr.isEmpty()) {
                irrigationResultArea.setText("Please enter days since last irrigation.");
                return;
            }

            double area = 0;
            try {
                area = Double.parseDouble(areaStr);
                if (area <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                irrigationResultArea.setText("Invalid Area. Please enter a valid positive number.");
                return;
            }

            int days = 0;
            try {
                days = Integer.parseInt(daysStr);
                if (days < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                irrigationResultArea.setText("Invalid days. Please enter a valid non-negative integer.");
                return;
            }

            String recommendation = IrrigationAdvisor.getRecommendation(city, crop, area, unit, soil, days);
            irrigationResultArea.setText(recommendation);
        });

        // Table
        tableModel = new DefaultTableModel(new String[]{"Crop","Disease","Confidence"},0);
        JTable table = new JTable(tableModel);
        table.setBackground(card);
        table.setForeground(text);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(950, 150));

        JPanel centerPanel = new JPanel(new GridLayout(1,2,15,15));
        centerPanel.setBackground(bg);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        centerPanel.add(leftCard);
        centerPanel.add(rightCard);

        frame.add(title, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(tableScroll, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    static void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
    }
}