package com.github.gogoasac.infra.input.menu;

import com.github.gogoasac.common.StringUtils;

import java.io.*;
import java.util.*;

/* ...existing code... */

public abstract class MenuHandler {
    private final static String MENU_SIZE_NOT_SUPPORTED = "Supplied menu size not supported.";
    private final static String MENU_ITEM_INVALID = "Supplied menu item does not exist.";
    private final static int GO_BACK_MENU_ITEM_NO = 9;
    public static final String PROMPT = "Choose: ";

    private final String menuName;
    private final List<MenuItem> menuItemList;
    private final PrintStream writer;
    private final BufferedReader reader;

    private String menuText;

    // changed constructor to accept a shared PrintStream and BufferedReader
    public MenuHandler(final String menuName,
                       final PrintStream outputStream,
                       final BufferedReader sharedReader) {
        this.menuName = menuName;
        this.menuItemList = new ArrayList<>();
        this.writer = Objects.requireNonNull(outputStream, "outputStream");
        this.reader = Objects.requireNonNull(sharedReader, "sharedReader");
        this.menuText = StringUtils.EMPTY_STRING;
    }

    protected void setMenuItemList(final List<MenuItem> menuItemList) {
        Optional.of(menuItemList.size())
            .filter(size -> size < 9)
            .orElseThrow(() -> new IllegalArgumentException(MENU_SIZE_NOT_SUPPORTED));

        this.menuItemList.addAll(menuItemList);
    }

    public final void run() {
        int option = -1;

        while (option != GO_BACK_MENU_ITEM_NO) {
            this.displayMenu();
            option = this.pickOption();
        }
    }

    private String getMenuText() {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\n--- %s ---".formatted(this.menuName));
        for (int itemIndex = 0; itemIndex < this.menuItemList.size(); itemIndex += 1) {
            // show 1-based indices to the user
            stringBuilder.append("\n%d) %s".formatted(itemIndex + 1, this.menuItemList.get(itemIndex).content()));
        }
        stringBuilder.append("\n%d) Back".formatted(GO_BACK_MENU_ITEM_NO));

        return stringBuilder.toString();
    }

    public final void displayMenu() {
        if (Objects.equals(this.menuText, StringUtils.EMPTY_STRING)) {
            this.menuText = this.getMenuText();
        }

        this.printLine(menuText);
    }

    private int toMenuItemIndex(final String option) {
        return Integer.parseInt(option) - 1;
    }

    protected final int pickOption() {
        this.printLine(PROMPT);

        final int option = this.toMenuItemIndex(this.readLine());

        Optional.of(option)
            .filter(this::isIndexValid)
            .orElseThrow(() -> new IllegalArgumentException(MENU_ITEM_INVALID));

        if (Objects.equals(option, GO_BACK_MENU_ITEM_NO - 1)) {
            return GO_BACK_MENU_ITEM_NO;
        }

        this.menuItemList.get(option).action().run();

        return option;
    }

    private boolean isIndexValid(final int index) {
        return (index >= 0 && index < this.menuItemList.size()) || index == GO_BACK_MENU_ITEM_NO - 1;
    }

    protected String readLine() {
        try {
            String line = this.reader.readLine();
            return line == null ? "" : line.trim();
        } catch (IOException e) {
            return "";
        }
    }

    protected String readLine(final String prompt) {
        this.printLine(prompt);
        return readLine();
    }

    protected void printLine(final String prompt) {
        this.writer.println(prompt);
        this.writer.flush();
    }

    protected Long readLong(final String prompt) {
        final String s = this.readLine(prompt);

        if (s.isEmpty()) {
            this.printLine("Cancelled");
            return null;
        }

        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            this.printLine("Invalid number: '" + s + "'");
            return null;
        }
    }

    protected Integer readInt(final String prompt) {
        final String s = this.readLine(prompt);

        if (s.isEmpty()) {
            this.printLine("Cancelled");
            return null;
        }

        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            this.printLine("Invalid number: '" + s + "'");
            return null;
        }
    }
}
