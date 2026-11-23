package com.agristock.modules.Module7;

import com.agristock.model.AgriException;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.StockItem;
import com.agristock.service.AgriStockSystem;
import com.agristock.ui.View;
import com.agristock.ui.UI;
import com.agristock.ui.Ctx;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// MODULE 7: Stock Monitoring & Control (AVL Tree, in-order traversal, binary search)
public class StockView extends View {
    private final TableView<StockItem> stockTable = UI.table();
    private final TextField search = new TextField();
    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final ComboBox<String> flagFilter = new ComboBox<>();
    private final ComboBox<String> itemSelect = new ComboBox<>();
    private final ComboBox<String> editSelect = new ComboBox<>();
    private TextField crop, harvestedQty, unitValue;
    private ComboBox<String> category;
    private DatePicker expiryDate;
    private TextField reserveQty, releaseQty;
    private ComboBox<String> editCategory;
    private DatePicker editExpiry;
    private TextField editValue;

    public StockView(Ctx ctx) { super(ctx); build(); }

    private void build() {
        header("7 - Stock Monitoring & Control",
                "Browse inventory, search stock, and add or adjust stock items.");

        // Filter defaults are set ONCE here (refresh() must not reset them, or filtering never works).
        categoryFilter.setItems(FXCollections.observableArrayList("All"));
        categoryFilter.setValue("All");
        flagFilter.setItems(FXCollections.observableArrayList("All", "Expired", "Near-expiry", "Low", "Aging"));
        flagFilter.setValue("All");

        // ---------- Left panel: search & filters ----------
        UI.Form searchF = new UI.Form();
        searchF.add("Search", search);
        search.setPromptText("Item ID or crop...");
        search.setOnAction(e -> refresh());
        searchF.add("Category", categoryFilter);
        searchF.add("Flag", flagFilter);
        searchF.wide(UI.btn("Apply Filters", "primary", () -> refresh()));
        VBox filterCard = UI.card(UI.cardTitle("Search & Filters"), searchF.grid);
        filterCard.setPrefWidth(380);

        // ---------- Left panel: add stock item ----------
        UI.Form addF = new UI.Form();
        crop = addF.text("Crop");
        category = addF.combo("Category", Arrays.asList(AgriStockSystem.CATEGORIES));
        harvestedQty = addF.text("Qty (kg)");
        expiryDate = addF.date("Expiry", LocalDate.now().plusDays(7));
        unitValue = addF.text("Unit Value");
        addF.wide(UI.btn("Add Stock Item", "primary", this::addItem));
        VBox addCard = UI.card(UI.cardTitle("Add Stock Item"), addF.grid);
        addCard.setPrefWidth(380);

        // ---------- Left panel: edit stock item ----------
        UI.Form editF = new UI.Form();
        editF.add("Item", editSelect);
        editCategory = editF.combo("Category", Arrays.asList(AgriStockSystem.CATEGORIES));
        editExpiry = editF.date("Expiry", null);
        editValue = editF.text("Unit Value");
        editF.wide(UI.btn("Save Changes", "secondary", this::editItem));
        editSelect.valueProperty().addListener((o, a, id) -> {
            StockItem s = id == null ? null : sys.searchStock(id);
            if (s == null) return;
            editCategory.setValue(s.category);
            editExpiry.setValue(s.expiryDate);
            editValue.setText(String.valueOf(s.unitValue));
        });
        VBox editCard = UI.card(UI.cardTitle("Edit Stock Item"), editF.grid);
        editCard.setPrefWidth(380);

        // ---------- Left panel: reserve / release ----------
        UI.Form adjF = new UI.Form();
        adjF.add("Item", itemSelect);
        reserveQty = adjF.text("Reserve (kg)");
        releaseQty = adjF.text("Release (kg)");
        adjF.wide(UI.btn("Reserve", "secondary", this::reserve));
        adjF.wide(UI.btn("Release", "secondary", this::release));
        VBox adjCard = UI.card(UI.cardTitle("Reserve / Release Stock"), adjF.grid);
        adjCard.setPrefWidth(380);

        VBox left = new VBox(14, filterCard, addCard, editCard, adjCard);
        left.setPrefWidth(400);

        // ---------- Right panel: stock inventory table ----------
        UI.col(stockTable, "Item ID", s -> s.itemId);
        UI.col(stockTable, "Crop", s -> s.crop);
        UI.col(stockTable, "Category", s -> s.category);
        UI.col(stockTable, "Harvested (kg)", s -> String.format("%,.2f", s.harvested));
        UI.col(stockTable, "Remaining (kg)", s -> String.format("%,.2f", s.remaining));
        UI.col(stockTable, "Available (kg)", s -> String.format("%,.2f", s.available()));
        UI.col(stockTable, "Unit Value", s -> String.format("%,.2f", s.unitValue));
        UI.col(stockTable, "Expiry", s -> s.expiryDate == null ? "-" : s.expiryDate.toString());
        UI.col(stockTable, "Flags", s -> sys.flagsOf(s));
        UI.col(stockTable, "Location", s -> s.location);

        VBox tableCard = UI.card(UI.cardTitle("Stock Inventory"), stockTable);
        VBox.setVgrow(stockTable, Priority.ALWAYS);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        VBox right = new VBox(14, tableCard);
        VBox.setVgrow(right, Priority.ALWAYS);

        getChildren().addAll(UI.split(left, right, 400));
    }

    @Override public String title() { return "7  Stock"; }
    @Override public Feature feature() { return Feature.STOCK_EDIT; }

    private void addItem() {
        String c = crop.getText();
        double qty = UI.num(harvestedQty, "Qty");
        double value = UI.num(unitValue, "Unit Value");
        sys.addItem(c, category.getValue(), qty, expiryDate.getValue(), value);
        crop.clear(); category.setValue(null);
        harvestedQty.clear(); unitValue.clear();
        expiryDate.setValue(LocalDate.now().plusDays(7));
        refresh();
    }

    private void editItem() {
        String id = editSelect.getValue();
        if (id == null) throw new AgriException("Select an item to edit.");
        if (editCategory.getValue() == null) throw new AgriException("Select a category.");
        sys.editItem(id, editCategory.getValue(), editExpiry.getValue(), UI.num(editValue, "Unit Value"));
        refresh();
    }

    private void reserve() {
        String id = itemSelect.getValue();
        if (id == null) throw new AgriException("Select an item.");
        double qty = UI.num(reserveQty, "Reserve Qty");
        sys.reserveManually(id, qty);
        reserveQty.clear();
        refresh();
    }

    private void release() {
        String id = itemSelect.getValue();
        if (id == null) throw new AgriException("Select an item.");
        double qty = UI.num(releaseQty, "Release Qty");
        sys.releaseManually(id, qty);
        releaseQty.clear();
        refresh();
    }

    @Override public void refresh() {
        // Category filter list (keeps the current selection; "All" is always present).
        List<String> cats = new ArrayList<>();
        cats.add("All");
        for (StockItem s : sys.traverseInventory()) {
            if (!cats.contains(s.category)) cats.add(s.category);
        }
        UI.setItems(categoryFilter, cats);

        List<String> ids = new ArrayList<>();
        for (StockItem s : sys.traverseInventory()) ids.add(s.itemId);
        UI.setItems(itemSelect, ids);
        UI.setItems(editSelect, ids);

        UI.fill(stockTable, sys.filterStock(search.getText(), categoryFilter.getValue(), flagFilter.getValue()));
    }
}