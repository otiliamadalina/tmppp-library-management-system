package com.example.tmppp_library_management.ui;

import java.util.Stack;

public class CommandHistory {
    private Stack<Command> history = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();

    public void push(Command command) {
        history.push(command);
        redoStack.clear();
    }

    public Command pop() {
        if (!history.isEmpty()) {
            return history.pop();
        }
        return null;
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (canUndo()) {
            Command command = history.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    public void redo() {
        if (canRedo()) {
            Command command = redoStack.pop();
            command.execute();
            history.push(command);
        }
    }

    public void clear() {
        history.clear();
        redoStack.clear();
    }

    public String getLastCommandDescription() {
        if (history.isEmpty()) return "Nicio comanda";
        return history.peek().getDescription();
    }
}