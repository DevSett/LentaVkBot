package org.bot;

/**
 * Created by killsett on 10.06.17.
 */
public enum Commands {
    START("/start"),
    HELP("/help"),
    CONNECT("/bind"),
    LASTTEN("/lastten"),
    LAST("/last"),
    STOP("/unbind"),
    STOPALL("/unbindall"),
    LIST("/list"),
    UNCNOWN("");
    String command;

    Commands(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
