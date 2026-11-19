package com.agristock.modules.Module7;

import com.agristock.model.AgriException;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.StockItem;
import com.agristock.ui.View;
import com.agristock.ui.UI;
import com.agristock.ui.Ctx;
import com.agristock.service.AgriStockSystem;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// MODULE 7: Stock Monitoring & Control (AVL Tree, in-order traversal, binary search)
public class StockView extends View {
    private final TableView<StockItem> stockTable = UI.table();
    private final TextField search = new TextField();
    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final ComboBox<String> flagFilter = new ComboBox<>();
    private final ComboBox<String> itemSelect = new ComboBox<>();
    private TextField itemId, crop, category, harvestedQty, expiryDate, unitValue;
    private TextField reserveQty, releaseQty;

    public StockView(Ctx ctx) { super(ctx); build(); }

    private void build() {
        header("7 - Stock Monitoring & Control",
                "Browse inventory (AVL in-order), search stock, and add or adjust stock items.");

        // ---------- Left panel: search & filters ----------
        UI.Form searchF = new UI.Form();
        searchF.add("Search", search);
        search.setPromptText("Item ID or crop...");
        searchF.add("Category", categoryFilter);
        searchF.add("Flag", flagFilter);
        searchF.wide(UI.btn("Apply Filters", "primary", e -> refresh()));
        searchF.wide(UI.btn("Refresh", "ghost", e -> refresh()));
        VBox filterCard = UI.card(UI.cardTitle("Search & Filters"), searchF.grid);
        filterCard.setPrefWidth(380);

        // ---------- Left panel: add stock item ----------
        UI.Form addF = new UI.Form();
        itemId = addF.text("Item ID");
        crop = addF.text("Crop");
        category = addF.text("Category");
        harvestedQty = addF.text("Qty (kg)");
        expiryDate = addF.text("Expiry (YYYY-MM-DD)");
        unitValue = addF.text("Unit Value");
        addF.wide(UI.btn("Add Stock Item", "primary", this::addItem));
        VBox addCard = UI.card(UI.cardTitle("Add Stock Item"), addF.grid);
        addCard.setPrefWidth(380);

        // ---------- Left panel: reserve / release ----------
        UI.Form adjF = new UI.Form();
        adjF.add("Item", itemSelect);
        reserveQty = adjF.text("Reserve (kg)");
        releaseQty = adjF.text("Release (kg)");
        adjF.wide(UI.btn("Reserve", "secondary", this::reserve));
        adjF.wide(UI.btn("Release", "secondary", this::release));
        VBox adjCard = UI.card(UI.cardTitle("Reserve / Release Stock"), adjF.grid);
        adjCard.setPrefWidth(380);

        VBox left = new VBox(14, filterCard, addCard, adjCard);
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

        VBox tableCard = UI.card(UI.cardTitle("Stock Inventory (AVL tree, in-order traversal)"), stockTable);
        VBox.setVgrow(stockTable, Priority.ALWAYS);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        VBox right = new VBox(14, tableCard);
        VBox.setVgrow(right, Priority.ALWAYS);

        getChildren().addAll(UI.split(left, right, 400));
    }

    @Override public String title() { return "7  Stock"; }
    @Override public Feature feature() { return Feature.STOCK_EDIT; }

    private void addItem() {
        String id = itemId.getText();
        String c = crop.getText();
        String cat = category.getText();
        double qty = UI.num(harvestedQty, "Qty");
        LocalDate expiry = LocalDate.parse(expiryDate.getText());
        double value = UI.num(unitValue, "Unit Value");
        sys.addItem(id, c, cat, qty, expiry, value);
        itemId.clear(); crop.clear(); category.clear();
        harvestedQty.clear(); expiryDate.clear(); unitValue.clear();
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
        List<String> cats = new ArrayList<>();
        cats.add("All");
        for (StockItem s : sys.traverseInventory()) {
            if (!cats.contains(s.category)) cats.add(s.category);
        }
        UI.setItems(categoryFilter, cats);
        categoryFilter.setValue("All");

        List<String> flags = new ArrayList<>();
        flags.add("All");
        flags.add("Expired");
        flags.add("Near-expiry");
        flags.add("Low");
        flags.add("Aging");
        UI.setItems(flagFilter, flags);
        flagFilter.setValue("All");

        List<String> ids = new ArrayList<>();
        for (StockItem s : sys.traverseInventory()) ids.add(s.itemId);
        UI.setItems(itemSelect, ids);

        List<StockItem> items = new ArrayList<>();
        String t = search.getText().trim().toLowerCase();
        String cat = categoryFilter.getValue();
        String flag = flagFilter.getValue();
        for (StockItem s : sys.traverseInventory()) {
            if (!t.isEmpty()
                    && !s.itemId.toLowerCase().contains(t)
                    && !s.crop.toLowerCase().contains(t)) continue;
            if (cat != null && !cat.equals("All") && !s.category.equals(cat)) continue;
            if (flag != null && !flag.equals("All") && !sys.flagsOf(s).contains(flag)) continue;
            items.add(s);
        }
        UI.fill(stockTable, items);
    }
}
