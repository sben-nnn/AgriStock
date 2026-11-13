package com.agristock.ui;

import com.agristock.model.AgriException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Function;

/** Small helper toolkit so every view stays short and consistent. */
public final class UI {
    private UI() {}

    public static String css() { return UI.class.getResource("/com/agristock/agristock.css").toExternalForm(); }

    // ---------------------------------------------------------------- dialogs
    public static void error(String msg) { alert(Alert.AlertType.ERROR, "Action not allowed", msg); }
    public static void info(String title, String msg) { alert(Alert.AlertType.INFORMATION, title, msg); }

    private static void alert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t, msg, ButtonType.OK);
        a.setTitle(title); a.setHeaderText(title);
        a.getDialogPane().getStylesheets().add(css());
        a.showAndWait();
    }

    public static boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setHeaderText("Please confirm");
        a.getDialogPane().getStylesheets().add(css());
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    /** Shows monospaced text (receipts, reports) in a dialog. */
    public static void textDialog(String title, String text) {
        TextArea ta = new TextArea(text);
        ta.setEditable(false); ta.getStyleClass().add("mono"); ta.setPrefSize(560, 380);
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(title); d.getDialogPane().setContent(ta);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        d.getDialogPane().getStylesheets().add(css());
        d.showAndWait();
    }

    /** Runs an action and converts validation errors into an alert. */
    public static void run(Runnable r) {
        try { r.run(); }
        catch (AgriException e) { error(e.getMessage()); }
        catch (RuntimeException e) { e.printStackTrace(); error("Unexpected problem: " + e); }
    }

    public static void formDialog(String title, Form f, Runnable onOk) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(title); d.setHeaderText(title);
        f.grid.setPadding(new Insets(10));
        d.getDialogPane().setContent(f.grid);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.getDialogPane().getStylesheets().add(css());
        Button ok = (Button) d.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            try { onOk.run(); }
            catch (AgriException e) { error(e.getMessage()); ev.consume(); }
        });
        d.showAndWait();
    }

    // ---------------------------------------------------------------- parsing
    public static double num(TextField f, String name) {
        String t = f.getText().trim().replace(",", "");
        if (t.isEmpty()) throw new AgriException(name + " is required.");
        try { return Double.parseDouble(t); }
        catch (NumberFormatException e) { throw new AgriException("Enter a valid number for " + name + "."); }
    }

    /** "B-1001 | Rice ..." -> "B-1001" */
    public static String idOf(ComboBox<String> c) {
        String v = c.getValue();
        if (v == null) return null;
        int i = v.indexOf(" |");
        return i < 0 ? v : v.substring(0, i);
    }

    public static void setItems(ComboBox<String> c, Collection<String> items) {
        String old = c.getValue();
        c.setItems(FXCollections.observableArrayList(items));
        if (old != null && items.contains(old)) c.setValue(old);
    }

    // ---------------------------------------------------------------- tables
    public static <T> TableView<T> table() {
        TableView<T> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("No records yet"));
        VBox.setVgrow(t, Priority.ALWAYS);
        return t;
    }

    public static <T> void col(TableView<T> t, String title, Function<T, String> f) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(f.apply(cd.getValue())));
        t.getColumns().add(c);
    }

    public static <T> void fill(TableView<T> t, Iterable<T> items) {
        ObservableList<T> l = FXCollections.observableArrayList();
        for (T x : items) l.add(x);
        t.setItems(l);
    }

    // ---------------------------------------------------------------- widgets
    public static Label heading(String s) { Label l = new Label(s); l.getStyleClass().add("page-title"); return l; }
    public static Label subtitle(String s) { Label l = new Label(s); l.getStyleClass().add("page-sub"); l.setWrapText(true); return l; }
    public static Label hint(String s) { Label l = new Label(s); l.getStyleClass().add("hint"); l.setWrapText(true); return l; }
    public static Label cardTitle(String s) { Label l = new Label(s); l.getStyleClass().add("card-title"); return l; }

    public static Button btn(String text, String style, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add(style);
        b.setOnAction(e -> run(action));
        return b;
    }

    public static VBox card(Node... children) {
        VBox v = new VBox(10, children);
        v.getStyleClass().add("card");
        return v;
    }

    public static HBox row(Node... children) {
        HBox h = new HBox(8, children);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    public static FlowPane buttons(Button... bs) {
        FlowPane f = new FlowPane(8, 8, bs);
        return f;
    }

    /** Form card on the left (fixed width, scrollable) and content on the right. */
    public static HBox split(Node left, Node right, double leftWidth) {
        ScrollPane sp = new ScrollPane(left);
        sp.setFitToWidth(true); sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setMinWidth(leftWidth); sp.setPrefWidth(leftWidth); sp.setMaxWidth(leftWidth);
        HBox h = new HBox(14, sp, right);
        HBox.setHgrow(right, Priority.ALWAYS);
        VBox.setVgrow(h, Priority.ALWAYS);
        return h;
    }

    public static String d(LocalDate d) { return d == null ? "" : d.toString(); }

    // ---------------------------------------------------------------- form builder
    public static class Form {
        public final GridPane grid = new GridPane();
        private int r;

        public Form() {
            grid.setHgap(10); grid.setVgap(8);
            ColumnConstraints c0 = new ColumnConstraints(); c0.setMinWidth(95);
            ColumnConstraints c1 = new ColumnConstraints(); c1.setHgrow(Priority.ALWAYS); c1.setFillWidth(true);
            grid.getColumnConstraints().addAll(c0, c1);
        }

        public void add(String label, Node n) {
            Label l = new Label(label);
            grid.add(l, 0, r); grid.add(n, 1, r++);
            if (n instanceof Region) ((Region) n).setMaxWidth(Double.MAX_VALUE);
        }

        public void wide(Node n) { grid.add(n, 0, r++, 2, 1); }

        public TextField text(String label, String prompt) {
            TextField t = new TextField(); t.setPromptText(prompt == null ? "" : prompt); add(label, t); return t;
        }
        public TextField text(String label) { return text(label, null); }

        public PasswordField password(String label) { PasswordField p = new PasswordField(); add(label, p); return p; }

        public TextArea area(String label, int rows) {
            TextArea a = new TextArea(); a.setPrefRowCount(rows); a.setWrapText(true); add(label, a); return a;
        }

        public ComboBox<String> combo(String label, Collection<String> items) {
            ComboBox<String> c = new ComboBox<>(FXCollections.observableArrayList(items));
            c.setMaxWidth(Double.MAX_VALUE);
            add(label, c);
            return c;
        }

        public DatePicker date(String label, LocalDate def) {
            DatePicker p = new DatePicker(def); p.setMaxWidth(Double.MAX_VALUE); add(label, p); return p;
        }
    }
}
