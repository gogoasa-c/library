package com.github.gogoasac.infra.input.menu;

import com.github.gogoasac.common.StringUtils;

import java.io.*;
import java.util.*;
import java.util.function.Supplier;

public abstract class MenuHandler {
    private final static String MENU_SIZE_NOT_SUPPORTED = "Supplied menu size not supported.";
    private final static String MENU_ITEM_INVALID = "Supplied menu item does not exist.";
    private final static int GO_BACK_MENU_ITEM_NO = 9;
    public static final String PROMPT = "Choose: ";

    private final String menuName;
    private final List<MenuItem> menuItemList;
    private final PrintWriter writer;
    private final Supplier<String> reader;

    private String menuText;

    public MenuHandler(final String menuName,
                       final PrintStream outputStream,
                       final InputStream inputStream) {
        this.menuName = menuName;
        this.menuItemList = new ArrayList<>();
        this.writer = new PrintWriter(outputStream, true);
        this.menuText = StringUtils.EMPTY_STRING;

        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        this.reader = () -> StringUtils.orElse(bufferedReader::readLine, StringUtils.EMPTY_STRING);
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
            stringBuilder.append("\n%d) %s".formatted(itemIndex, this.menuItemList.get(itemIndex).content()));
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
        return (index >= 0 && index <= this.menuItemList.size()) || index == GO_BACK_MENU_ITEM_NO - 1;
    }

    protected String readLine() {
        return this.reader.get().trim();
    }

    protected String readLine(final String prompt) {
        this.printLine(prompt);
        return this.reader.get().trim();
    }

    protected void printLine(final String prompt) {
        this.writer.println(prompt);
        this.writer.flush();
    }

    protected Long readLong(final String prompt) {
        final String s = this.readLine(prompt).trim();

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
}
