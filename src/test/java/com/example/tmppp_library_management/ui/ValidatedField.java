package com.example.tmppp_library_management.ui;

import com.example.tmppp_library_management.chainOfResponsability.*;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class ValidatedField {
    private JTextField textField;
    private JLabel errorLabel;
    private InputValidator chain;
    private String fieldName;
    private Object context;

    public ValidatedField(JPanel panel, GridBagConstraints gbc, int row,
                          String labelText, InputValidator chain, String fieldName, Object context) {
        this.chain = chain;
        this.fieldName = fieldName;
        this.context = context;

        JLabel label = new JLabel(labelText);
        label.setForeground(Color.BLACK);
        label.setFont(new Font("Garamond", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(label, gbc);

        this.textField = new JTextField(20);
        styleTextField(textField);
        gbc.gridx = 1;
        panel.add(textField, gbc);

        this.errorLabel = new JLabel(" ");
        this.errorLabel.setForeground(Color.RED);
        this.errorLabel.setFont(new Font("Garamond", Font.ITALIC, 11));
        gbc.gridx = 1;
        gbc.gridy = row + 1;
        panel.add(errorLabel, gbc);
    }

    public boolean validate() {
        String value = textField.getText();
        ValidationResult result = chain.validate(value, fieldName, context);
        if (!result.isValid()) {
            errorLabel.setText(result.getErrorMessage());
            errorLabel.setForeground(Color.RED);
            return false;
        } else {
            errorLabel.setText("✓");
            errorLabel.setForeground(new Color(0, 150, 0));
            return true;
        }
    }

    public String getValue() {
        return textField.getText();
    }

    public String getRawValue() {
        return textField.getText().replaceAll("-", "");
    }

    public void clearError() {
        errorLabel.setText(" ");
    }

    private void styleTextField(JTextField field) {
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setFont(new Font("Garamond", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { clearError(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { clearError(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { clearError(); }
        });
    }

    private boolean isFormatting = false;

    public void setIsbnFormatting() {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (isFormatting) {
                    super.insertString(fb, offset, string, attr);
                    return;
                }
                isFormatting = true;
                try {
                    String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String newText = currentText.substring(0, offset) + string + currentText.substring(offset);
                    String formatted = formatIsbn(newText);

                    String digitsOnly = formatted.replaceAll("-", "");
                    if (digitsOnly.length() > 12) {
                        return;
                    }

                    fb.getDocument().remove(0, fb.getDocument().getLength());
                    fb.getDocument().insertString(0, formatted, attr);
                } finally {
                    isFormatting = false;
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (isFormatting) {
                    super.replace(fb, offset, length, text, attrs);
                    return;
                }
                isFormatting = true;
                try {
                    String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String newText;
                    if (length > 0) {
                        newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                    } else {
                        newText = currentText.substring(0, offset) + text + currentText.substring(offset);
                    }
                    String formatted = formatIsbn(newText);

                    String digitsOnly = formatted.replaceAll("-", "");
                    if (digitsOnly.length() > 12) {
                        return;
                    }

                    fb.getDocument().remove(0, fb.getDocument().getLength());
                    fb.getDocument().insertString(0, formatted, attrs);
                } finally {
                    isFormatting = false;
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                if (isFormatting) {
                    super.remove(fb, offset, length);
                    return;
                }
                isFormatting = true;
                try {
                    String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String newText = currentText.substring(0, offset) + currentText.substring(offset + length);
                    String formatted = formatIsbn(newText);

                    fb.getDocument().remove(0, fb.getDocument().getLength());
                    fb.getDocument().insertString(0, formatted, null);
                } finally {
                    isFormatting = false;
                }
            }

            private String formatIsbn(String input) {
                String digits = input.replaceAll("[^0-9]", "");

                if (digits.length() > 12) {
                    digits = digits.substring(0, 12);
                }

                if (digits.length() <= 3) {
                    return digits;
                } else if (digits.length() <= 6) {
                    return digits.substring(0, 3) + "-" + digits.substring(3);
                } else if (digits.length() <= 9) {
                    return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
                } else {
                    return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6, 9) + "-" + digits.substring(9);
                }
            }
        });
    }
}