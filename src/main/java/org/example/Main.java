package org.example;

import net.sourceforge.tess4j.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.Toolkit;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main extends JFrame {

    private JTextArea textArea;
    private JButton openButton, saveButton, copyButton;
    private JComboBox<String> languageComboBox;
    private JProgressBar progressBar;
    private JToggleButton themeToggleButton;
    private String selectedLanguage = "eng";  // Default language: English

    public Main() {
        // Set up the frame
        setTitle("Tesseract OCR Redesign");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        openButton = new JButton("Open Image/PDF");
        saveButton = new JButton("Save Text");
        copyButton = new JButton("Copy Text");
        languageComboBox = new JComboBox<>(new String[]{"English", "Spanish", "French"});
        progressBar = new JProgressBar(0, 100);
        themeToggleButton = new JToggleButton("Dark Mode");

        // Layout setup
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(openButton, BorderLayout.NORTH);
        panel.add(languageComboBox, BorderLayout.WEST);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(themeToggleButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);

        // Event Listeners
        openButton.addActionListener(e -> openFile());
        saveButton.addActionListener(e -> saveText());
        copyButton.addActionListener(e -> copyText());
        languageComboBox.addActionListener(e -> selectLanguage());
        themeToggleButton.addActionListener(e -> toggleTheme());

        // Set initial theme
        setLightTheme();
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Image or PDF");
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                progressBar.setIndeterminate(true);
                textArea.setText("");  // Clear text area
                String text = extractText(file);
                textArea.setText(text);
                progressBar.setIndeterminate(false);
            } catch (Exception ex) {
                showError("Error processing file: " + ex.getMessage());
            }
        }
    }

    private String extractText(File file) throws Exception {
        String text = "";
        String extension = getFileExtension(file);

        if ("pdf".equalsIgnoreCase(extension)) {
            PDDocument document = PDDocument.load(file);
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document);
            document.close();
        } else if ("jpg".equalsIgnoreCase(extension) || "png".equalsIgnoreCase(extension)) {
            ITesseract instance = new Tesseract();
            instance.setLanguage(selectedLanguage);
            text = instance.doOCR(file);
        }

        return text;
    }

    private void saveText() {
        String text = textArea.getText();
        if (text.isEmpty()) {
            showError("No text to save!");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Text");
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                Files.write(Paths.get(file.toURI()), text.getBytes());
            } catch (IOException ex) {
                showError("Error saving file: " + ex.getMessage());
            }
        }
    }

    private void copyText() {
        String text = textArea.getText();
        if (!text.isEmpty()) {
            StringSelection selection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        }
    }

    private void selectLanguage() {
        String selected = (String) languageComboBox.getSelectedItem();
        if ("English".equalsIgnoreCase(selected)) {
            selectedLanguage = "eng";
        } else if ("Spanish".equalsIgnoreCase(selected)) {
            selectedLanguage = "spa";
        } else if ("French".equalsIgnoreCase(selected)) {
            selectedLanguage = "fra";
        }
    }

    private void toggleTheme() {
        if (themeToggleButton.isSelected()) {
            setDarkTheme();
        } else {
            setLightTheme();
        }
    }

    private void setLightTheme() {
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(Color.BLACK);
        getContentPane().setBackground(Color.LIGHT_GRAY);
        openButton.setBackground(Color.WHITE);
        saveButton.setBackground(Color.WHITE);
        copyButton.setBackground(Color.WHITE);
    }

    private void setDarkTheme() {
        textArea.setBackground(Color.DARK_GRAY);
        textArea.setForeground(Color.WHITE);
        getContentPane().setBackground(Color.DARK_GRAY);
        openButton.setBackground(Color.DARK_GRAY);
        saveButton.setBackground(Color.DARK_GRAY);
        copyButton.setBackground(Color.DARK_GRAY);
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        return (lastIndex == -1) ? "" : name.substring(lastIndex + 1);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
