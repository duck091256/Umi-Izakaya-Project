package services;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;

public class ClickableFileListener extends MouseAdapter {
    private final JTextPane chatPane;

    public ClickableFileListener(JTextPane chatPane) {
        this.chatPane = chatPane;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int pos = chatPane.viewToModel2D(e.getPoint());
        StyledDocument doc = chatPane.getStyledDocument();
        Element elem = doc.getCharacterElement(pos);

        try {
            String clickedText = doc.getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset()).trim();
            File tempFile = new File("temp_files/" + clickedText);
            if (tempFile.exists()) {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(clickedText));
                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File savePath = chooser.getSelectedFile();
                    Files.copy(tempFile.toPath(), savePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    JOptionPane.showMessageDialog(null, "Đã tải file thành công!");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}