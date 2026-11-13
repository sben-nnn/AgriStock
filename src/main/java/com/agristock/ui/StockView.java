package com.agristock.ui;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.Arrays;

/** MODULE 7: Stock Monitoring & Control (AVL tree, in-order traversal, binary search). */
public class StockView extends View {
    private final TableView<StockItem> table = UI.table();
    private final TextField search = new TextField(), low = new TextField(), aging = new TextField(), near = new TextField();
    private final ComboBox<String> category = new ComboBox<>(), flag = new ComboBox<>();
    private final Label count = UI.hint("");

    public StockView(Ctx ctx) {
        super(ctx);
        header("7 - Stock Monitoring & Control", "Stock lives in an AVL tree: in-order traversal gives a sorted inventory, binary search finds an item, flags show low, aging and near-expiry stock.");
        search.setPromptText("Search ID or crop"); search.setPrefWidth(170);
        category.getItems().addAll("All", "Grain", "Vegetable", "Fruit", "Seed", "Other"); category.setValue("All");
        flag.getItems().addAll("All", "Low", "Aging", "Near-expiry", "Expired"); flag.setValue("All");
        low.setPrefWidth(60); aging.setPrefWidth(50); near.setPrefWidth(50);
        low.setText(String.valueOf((int) sys.lowStockKg)); aging.setText(String.valueOf(sys.agingDays)); near.setText(String.valueOf(sys.nearExpiryDays));
        HBox filters = UI.row(search, category, flag, UI.btn("Filter", "primary", this::refresh),
                UI.btn("Find by ID (binary search)", "secondary", this::find));
        HBox thresholds = UI.row(new Label("Low stock \u2264 (kg)"), low, new Label("Aging after (days)"), aging, new Label("Near-expiry within (days)"), near,
                UI.btn("Apply thresholds", "ghost", this::applyThresholds));
        HBox actions = UI.row(UI.btn("Add item", "primary", this::add), UI.btn("Edit selected", "secondary", this::edit),
                UI.btn("Reserve", "secondary", () -> reserve(true)), UI.btn("Release reservation", "secondary", () -> reserve(false)), count);

        UI.col(table, "Item / Batch", s -> s.itemId);
        UI.col(table, "Crop", s -> s.crop);
        UI.col(table, "Category", s -> s.category);
        UI.col(table, "Grade", s -> s.grade);
        UI.col(table, "Harvested", s -> Fmt.kg(s.harvested));
        UI.col(table, "Stored", s -> Fmt.kg(s.stored));
        UI.col(table, "Reserved", s -> Fmt.kg(s.reserved));
        UI.col(table, "Sold", s -> Fmt.kg(s.sold));
        UI.col(table, "Dispatched", s -> Fmt.kg(s.dispatched));
        UI.col(table, "Damaged", s -> Fmt.kg(s.damaged));
        UI.col(table, "Remaining", s -> Fmt.kg(s.remaining));
        UI.col(table, "Available", s -> Fmt.kg(sys.computeAvailableStock(s)));
        UI.col(table, "Expiry", s -> s.expiryDate.toString());
        UI.col(table, "Value/kg", s -> Fmt.peso(s.unitValue));
        UI.col(table, "Flags", s -> sys.flagsOf(s));
        table.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(StockItem s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null || isSelected()) { setStyle(""); return; }
                if (sys.flagExpired(s)) setStyle("-fx-background-color:#ffcdd2;");
                else if (sys.flagNearExpiry(s)) setStyle("-fx-background-color:#fff59d;");
                else if (sys.flagLowStock(s)) setStyle("-fx-background-color:#ffe0b2;");
                else if (sys.flagAgingStock(s)) setStyle("-fx-background-color:#d7ccc8;");
                else setStyle("");
            }
        });
        getChildren().add(UI.card(UI.cardTitle("Categorized inventory (in-order traversal)"), filters, thresholds, actions, table,
                UI.hint("Row colours: red = expired, yellow = near-expiry, orange = low stock, grey = aging.   Remaining = harvested - damaged - sold - FEFO dispatched.")));
        VBox.setVgrow(getChildren().get(getChildren().size() - 1), javafx.scene.layout.Priority.ALWAYS);
    }

    @Override public String title() { return "7  Stock Monitoring"; }

    private void applyThresholds() {
        sys.lowStockKg = UI.num(low, "Low-stock threshold");
        sys.agingDays = (int) UI.num(aging, "Aging days");
        sys.nearExpiryDays = (int) UI.num(near, "Near-expiry days");
        refresh();
    }

    private void find() {
        StockItem s = sys.searchStock(search.getText());
        if (s == null) throw new AgriException("No stock item with ID '" + search.getText().trim() + "'.");
        UI.fill(table, java.util.Collections.singletonList(s));
        count.setText("Binary search found 1 item.");
    }

    private void add() {
        UI.Form f = new UI.Form();
        TextField id = f.text("Item ID"), crop = f.text("Crop"), qty = f.text("Quantity (kg)"), val = f.text("Value per kg");
        ComboBox<String> cat = f.combo("Category", Arrays.asList("Grain", "Vegetable", "Fruit", "Seed", "Other")); cat.setValue("Seed");
        DatePicker exp = f.date("Expiry date", LocalDate.now().plusDays(30));
        UI.formDialog("Add stock item", f, () -> sys.addItem(id.getText(), crop.getText(), cat.getValue(), UI.num(qty, "Quantity"), exp.getValue(), UI.num(val, "Value per kg")));
        refresh();
    }

    private void edit() {
        StockItem s = table.getSelectionModel().getSelectedItem();
        if (s == null) throw new AgriException("Select a stock item first.");
        UI.Form f = new UI.Form();
        f.add("Item", new Label(s.itemId + " - " + s.crop));
        ComboBox<String> cat = f.combo("Category", Arrays.asList("Grain", "Vegetable", "Fruit", "Seed", "Other")); cat.setValue(s.category);
        DatePicker exp = f.date("Expiry date", s.expiryDate);
        TextField val = f.text("Value per kg"); val.setText(String.valueOf(s.unitValue));
        UI.formDialog("Edit stock item", f, () -> sys.editItem(s.itemId, cat.getValue(), exp.getValue(), UI.num(val, "Value per kg")));
        refresh();
    }

    private void reserve(boolean reserve) {
        StockItem s = table.getSelectionModel().getSelectedItem();
        if (s == null) throw new AgriException("Select a stock item first.");
        UI.Form f = new UI.Form();
        f.add("Item", new Label(s.itemId + " (available " + Fmt.kg(s.available()) + " kg, reserved " + Fmt.kg(s.reserved) + " kg)"));
        TextField q = f.text("Quantity (kg)");
        UI.formDialog(reserve ? "Reserve stock" : "Release reservation", f, () -> {
            if (reserve) sys.reserveManually(s.itemId, UI.num(q, "Quantity")); else sys.releaseManually(s.itemId, UI.num(q, "Quantity"));
        });
        refresh();
    }

    @Override public void refresh() {
        var list = sys.filterStock(search.getText(), category.getValue(), flag.getValue());
        UI.fill(table, list);
        count.setText(list.size() + " of " + sys.stock.size() + " items shown");
    }
}
