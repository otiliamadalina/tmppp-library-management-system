package com.example.tmppp_library_management.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class SearchableComboBox<T> extends JPanel {
    private JComboBox<T> comboBox;
    private JTextField searchField;
    private List<T> originalItems;
    private List<T> filteredItems;
    private boolean isSearching = false;

    public SearchableComboBox(List<T> items) {
        this.originalItems = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>(items);
        setLayout(new BorderLayout());
        setBackground(new Color(255, 182, 193));

        searchField = new JTextField();
        searchField.setFont(new Font("Garamond", Font.PLAIN, 12));
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(Color.BLACK);
        searchField.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Garamond", Font.PLAIN, 12));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(Color.BLACK);

        add(searchField, BorderLayout.NORTH);
        add(comboBox, BorderLayout.CENTER);

        updateComboBox(filteredItems);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    comboBox.requestFocus();
                    comboBox.showPopup();
                }
            }
        });
    }

    private void filter() {
        if (isSearching) return;
        isSearching = true;

        String searchText = searchField.getText().toLowerCase();
        filteredItems.clear();

        if (searchText.isEmpty()) {
            filteredItems.addAll(originalItems);
        } else {
            for (T item : originalItems) {
                if (item.toString().toLowerCase().contains(searchText)) {
                    filteredItems.add(item);
                }
            }
        }

        updateComboBox(filteredItems);
        if (!filteredItems.isEmpty()) {
            comboBox.setSelectedIndex(0);
        }

        isSearching = false;
    }

    private void updateComboBox(List<T> items) {
        comboBox.removeAllItems();
        for (T item : items) {
            comboBox.addItem(item);
        }
        if (!items.isEmpty()) {
            comboBox.setSelectedIndex(0);
        }
    }

    public T getSelectedItem() {
        return (T) comboBox.getSelectedItem();
    }

    public void setSelectedItem(T item) {
        comboBox.setSelectedItem(item);
        if (item != null) {
            searchField.setText(item.toString());
        }
    }

    public void clearSearch() {
        searchField.setText("");
        filter();
    }

    public void setItems(List<T> items) {
        this.originalItems = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>(items);
        updateComboBox(filteredItems);
        searchField.setText("");
    }

    public void addActionListener(ActionListener listener) {
        comboBox.addActionListener(listener);
    }
}