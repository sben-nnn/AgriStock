package com.agristock.ui;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** MODULE 6: Storage & Location Management (array of locations + FIFO rotation queue). */
public class StorageView extends View {
    private final TableView<StorageEntry> table = UI.table();
    private final TableView<String[]> summary = UI.table();
    private final ComboBox<String> item, location;
    private final TextField qty, conditions;
    private final DatePicker date;

    public StorageView(Ctx ctx) {
        super(ctx);
        header("6 - Storage & Location Management", "Locations are a fixed array; every stored lot joins a FIFO queue that defines the rotation order (oldest lot is used first).");
        UI.Form f = new UI.Form();
        item = f.combo("Batch", new ArrayList<>());
        location = f.combo("Location", Arrays.asList(sys.locations)); location.getSelectionModel().selectFirst();
        qty = f.text("Quantity (kg)");
        date = f.date("Storage date", LocalDate.now());
        conditions = f.text("Conditions", "e.g. 10\u00B0C, 90% RH");
        f.wide(UI.btn("Assign location & store", "primary", this::store));
        UI.Form r = new UI.Form();
        r.wide(UI.hint("Select a row to update its conditions, or rotate to see which lot must be used first."));
        r.wide(UI.buttons(UI.btn("Update conditions of selected", "secondary", this::updateConditions),
                UI.btn("Rotate stock (FIFO)", "accent", this::rotate)));

        UI.col(table, "Order", e -> String.valueOf(position(e)));
        UI.col(table, "Batch", e -> e.batchId);
        UI.col(table, "Location", e -> e.location);
        UI.col(table, "Qty (kg)", e -> Fmt.kg(e.qty));
        UI.col(table, "Stored on", e -> e.storageDate.toString());
        UI.col(table, "Conditions", e -> e.conditions);
        UI.col(table, "Expiry", e -> { StockItem s = sys.stock.search(e.batchId); return s == null ? "" : s.expiryDate.toString(); });
        UI.col(table, "Rotations", e -> String.valueOf(e.rotations));
        UI.col(summary, "Location", a -> a[0]); UI.col(summary, "Lots", a -> a[1]); UI.col(summary, "Total (kg)", a -> a[2]);
        summary.setPrefHeight(210);
        VBox right = new VBox(12, UI.card(UI.cardTitle("Rotation queue (front = use first)"), table), UI.card(UI.cardTitle("Location summary (array)"), summary));
        VBox.setVgrow(right.getChildren().get(0), javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(UI.split(new VBox(14, UI.card(UI.cardTitle("Store a batch"), f.grid), UI.card(UI.cardTitle("Rotation & conditions"), r.grid)), right, 340));
    }

    private int position(StorageEntry t) { int i = 1; for (StorageEntry e : sys.rotationQueue) { if (e == t) return i; i++; } return 0; }

    @Override public String title() { return "6  Storage & Location"; }
    @Override public Feature feature() { return Feature.STORAGE; }

    private void store() {
        sys.assignLocation(UI.idOf(item), location.getSelectionModel().getSelectedIndex(), UI.num(qty, "Quantity"), date.getValue(), conditions.getText());
        qty.clear(); conditions.clear(); refresh();
    }

    private void updateConditions() {
        StorageEntry e = table.getSelectionModel().getSelectedItem();
        if (e == null) throw new AgriException("Select a storage entry first.");
        sys.recordStorageConditions(e, conditions.getText()); refresh();
    }

    private void rotate() {
        StorageEntry e = sys.rotateStockFIFO(); refresh();
        UI.info("FIFO rotation", "Use first: batch " + e.batchId + " (" + Fmt.kg(e.qty) + " kg) at " + e.location + ", stored on " + e.storageDate + ".\nThe lot is re-queued at the back.");
    }

    @Override public void refresh() {
        UI.fill(table, sys.rotationQueue);
        List<String> items = new ArrayList<>();
        for (StockItem s : sys.traverseInventory()) {
            double free = s.remaining - s.stored;
            if (free > 0.0001) items.add(s.itemId + " | " + s.crop + " (" + Fmt.kg(free) + " kg unstored)");
        }
        UI.setItems(item, items);
        List<String[]> rows = new ArrayList<>();
        for (String loc : sys.locations) {
            int lots = 0; double total = 0;
            for (StorageEntry e : sys.rotationQueue) if (e.location.equals(loc)) { lots++; total += e.qty; }
            rows.add(new String[]{loc, String.valueOf(lots), Fmt.kg(total)});
        }
        UI.fill(summary, rows);
    }
}
