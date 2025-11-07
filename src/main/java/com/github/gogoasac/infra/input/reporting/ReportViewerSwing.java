package com.github.gogoasac.infra.input.reporting;

import com.github.gogoasac.application.dto.BookReport;
import com.github.gogoasac.application.dto.CollectionReport;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Simple Swing viewer for collection reports. Shows a scrollable text area with a rendered report.
 * Lightweight helper — used only when the user requests a GUI view from the CLI.
 */
public class ReportViewerSwing implements ReportViewer {

    public void showReports(final List<CollectionReport> reports) {
        if (reports == null || reports.isEmpty()) return;
        final List<CollectionReport> snapshot = List.copyOf(reports);
        SwingUtilities.invokeLater(() -> createAndShow(snapshot));
    }

    private void createAndShow(final List<CollectionReport> reports) {
        final JFrame frame = new JFrame("Library Collection Reports");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        // create books panel first so we can pass its model to the collections panel
        final DefaultListModel<BookReport> booksModel = new DefaultListModel<>();
        final JComponent booksPanel = createBooksPanel(booksModel);
        final JComponent collectionsPanel = createCollectionsPanel(reports, booksModel);

        final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, collectionsPanel, booksPanel);
        split.setResizeWeight(0.35);
        frame.getContentPane().add(split, BorderLayout.CENTER);

        // pre-select first collection to populate books list
        SwingUtilities.invokeLater(() -> {
            final JList<?> collList = (JList<?>) collectionsPanel.getClientProperty("collectionList");
            if (collList != null && collList.getModel().getSize() > 0) {
                collList.setSelectedIndex(0);
            }
        });

        frame.setVisible(true);
    }

    private JComponent createCollectionsPanel(final List<CollectionReport> reports,
                                                     final DefaultListModel<BookReport> booksModel) {
        final DefaultListModel<CollectionReport> model = new DefaultListModel<>();
        reports.forEach(model::addElement);

        final JList<CollectionReport> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer((l, value, index, isSelected, cellHasFocus) -> {
            final String name = value == null ? "<unknown>" : value.collectionName();
            final int count = value == null ? 0 : (value.books() == null ? 0 : value.books().size());
            final String text = name + "  (" + count + " books)";
            final JLabel label = new JLabel(text);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            if (isSelected) {
                label.setBackground(l.getSelectionBackground());
                label.setForeground(l.getSelectionForeground());
            } else {
                label.setBackground(l.getBackground());
                label.setForeground(l.getForeground());
            }
            return label;
        });

        final JScrollPane scroll = new JScrollPane(list);
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Collections"), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        // update the provided books model directly when selection changes
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                final CollectionReport selected = list.getSelectedValue();
                booksModel.clear();
                if (selected != null && selected.books() != null) {
                    selected.books().stream().filter(Objects::nonNull).forEach(booksModel::addElement);
                }
            }
        });

        panel.putClientProperty("collectionList", list);
        return panel;
    }

    private JComponent createBooksPanel(final DefaultListModel<BookReport> booksModel) {
        final JList<BookReport> booksList = new JList<>(booksModel);
        booksList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksList.setCellRenderer((l, value, index, isSelected, cellHasFocus) -> {
            final String title = value == null ? "" : htmlEscape(value.title());
            final String author = value == null ? "" : htmlEscape(value.authorName());
            final String text = String.format("<html><b>%s</b><br/><i>%s</i></html>", title, author);
            final JLabel label = new JLabel(text);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            if (isSelected) {
                label.setBackground(l.getSelectionBackground());
                label.setForeground(l.getSelectionForeground());
            } else {
                label.setBackground(l.getBackground());
                label.setForeground(l.getForeground());
            }
            return label;
        });

        booksList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2 && !booksList.isSelectionEmpty()) {
                    final BookReport br = booksList.getSelectedValue();
                    showBookDetailsDialog(booksList, br);
                }
            }
        });

        final JScrollPane scroll = new JScrollPane(booksList);
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Books"), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        panel.putClientProperty("booksModel", booksModel);
        panel.putClientProperty("booksList", booksList);
        return panel;
    }

    private void showBookDetailsDialog(final Component parent, final BookReport br) {
        final StringJoiner sj = new StringJoiner(System.lineSeparator());
        sj.add("Title: " + (br == null ? "" : br.title()));
        sj.add("Author: " + (br == null ? "" : br.authorName()));

        final JTextArea area = new JTextArea(sj.toString());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        final JButton copy = new JButton("Copy");
        copy.addActionListener(a -> {
            final StringSelection sel = new StringSelection(sj.toString());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
        });

        final JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(copy);

        final JPanel content = new JPanel(new BorderLayout(6, 6));
        content.add(new JScrollPane(area), BorderLayout.CENTER);
        content.add(btns, BorderLayout.SOUTH);

        final JOptionPane pane = new JOptionPane(content, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
        final JDialog dialog = pane.createDialog(parent, "Book Details");
        dialog.setModal(true);
        dialog.setResizable(true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private String htmlEscape(final String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
