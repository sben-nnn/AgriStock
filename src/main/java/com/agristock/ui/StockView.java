package com.agristock.ui;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** MODULE 7: Stock Monitoring & Control (AVL tree, in-order traversal, binary search). */
public class StockView extends View {
    private final TableView<StockItem> table = UI.table();
    private final ComboBox<String> item = new ComboBox<>();
    private final TextField category, expiry, unitValue, reserveQty;
    private final Label info = new Label();

    public StockView(Ctx ctx) {
        super(ctx);
        header("7 - Stock Monitoring", "View all stock items in AVL tree order. Edit items, reserve/release stock, and see flags.");
        UI.Form f = new UI.Form();
        f.add("Item", item);
        category = f.text("Category");
        expiry = f.text("Expiry Date (YYYY-MM-DD)");
        unitValue = f.text("Unit Value (/kg)");
        reserveQty = f.text("Reserve/Release (kg)");
        f.wide(UI.btn("Edit Item", "primary", this::edit));
        f.wide(UI.btn("Reserve Stock", "secondary", this::reserve));
        f.wide(UI.btn("Release Reservation", "ghost", this::release));
        info.setWrapText(true); info.getStyleClass().add("card-title");
        f.wide(info);
        UI.col(table, "Item ID", s -> s.itemId); UI.col(table, "Crop", s -> s.crop);
        UI.col(table, "Category", s -> s.category); UI.col(table, "Location", s -> s.location);
        UI.col(table, "Harvested", s -> Fmt.kg(s.harvested)); UI.col(table, "Stored", s -> Fmt.kg(s.stored));
        UI.col(table, "Reserved", s -> Fmt.kg(s.reserved)); UI.col(table, "Sold", s -> Fmt.kg(s.sold));
        UI.col(table, "Damaged", s -> Fmt.kg(s.damaged)); UI.col(table, "Remaining", s -> Fmt.kg(s.remaining));
        UI.col(table, "Available", s -> Fmt.kg(s.available())); UI.col(table, "Unit Value", s -> Fmt.peso(s.unitValue));
        UI.col(table, "Expiry", s -> s.expiryDate.toString());
        getChildren().add(UI.split(new VBox(14, UI.card(UI.cardTitle("Edit / Reserve / Release"), f.grid), UI.card(UI.cardTitle("Flags"), info)),
                UI.card(UI.cardTitle("Stock Items (in-order)"), table), 340));
    }

    @Override public String title() { return "7  Stock"; }
    @Override public Feature feature() { return Feature.STOCK_EDIT; }

    private void edit() {
        StockItem s = sys.mustFindStock(UI.idOf(item));
        LocalDate ex = null;
        String exStr = expiry.getText().trim();
        if (!exStr.isEmpty()) ex = LocalDate.parse(exStr);
        double uv = unitValue.getText().trim().isEmpty() ? s.unitValue : UI.num(unitValue, "Unit Value");
        sys.editItem(UI.idOf(item), category.getText().trim(), ex, uv);
        category.clear(); expiry.clear(); unitValue.clear(); refresh();
        UI.info("Edited", "Item " + s.itemId + " updated.");
    }

    private void reserve() {
        StockItem s = sys.mustFindStock(UI.idOf(item));
        double q = UI.num(reserveQty, "Quantity");
        sys.reserveManually(UI.idOf(item), q);
        reserveQty.clear(); refresh();
        UI.info("Reserved", q + " kg reserved for " + s.itemId);
    }

    private void release() {
        StockItem s = sys.mustFindStock(UI.idOf(item));
        double q = UI.num(reserveQty, "Quantity");
        sys.releaseManually(UI.idOf(item), q);
        reserveQty.clear(); refresh();
        UI.info("Released", q + " kg reservation released for " + s.itemId);
    }

    @Override public void refresh() {
        UI.fill(table, sys.traverseInventory());
        List<String> items = new ArrayList<>();
        for (StockItem s : sys.traverseInventory()) items.add(s.itemId + " | " + s.crop + " (" + Fmt.kg(s.available()) + " kg available)");
        UI.setItems(item, items);
        StringBuilder flags = new StringBuilder();
        int low = 0, near = 0, expired = 0, aging = 0;
        for (StockItem s : sys.traverseInventory()) {
            if (sys.flagExpired(s)) expired++;
            if (sys.flagNearExpiry(s)) near++;
            if (sys.flagLowStock(s)) low++;
            if (sys.flagAgingStock(s)) aging++;
        }
        flags.append("Expired: ").append(expired).append(" | Near-expiry: ").append(near)
             .append(" | Low stock: ").append(low).append(" | Aging: ").append(aging);
        info.setText(flags.toString());
    }
}
