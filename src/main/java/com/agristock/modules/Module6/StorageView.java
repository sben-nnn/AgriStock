package com.agristock.modules.Module6;

import com.agristock.model.AgriException;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.StorageEntry;
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

// MODULE 6: Storage & Location Management (Array + FIFO Queue rotation)
public class StorageView extends View {
    private final TableView<StorageEntry> storeTable = UI.table();
    private final TableView<StockItem> stockTable = UI.table();
    private final ComboBox<String> batchSelect = new ComboBox<>();
    private final ComboBox<String> locationSelect = new ComboBox<>();
    private TextField storageQty;
    private DatePicker storageDate;
    private TextField storageConditions;
    private StorageEntry editingEntry;

    public StorageView(Ctx ctx) { super(ctx); build(); }

    private void build() {
        header("6 - Storage & Location Management",
                "Assign harvested batches to storage locations and rotate stock FIFO (oldest lot used first).");

        // ---------- Left panel: assign a batch to a location ----------
        UI.Form f = new UI.Form();
        f.add("Batch / Item", batchSelect);
        f.add("Location", locationSelect);
        storageQty = f.text("Qty (kg)", "e.g. 200");
        storageDate = f.date("Storage Date", LocalDate.now());
        storageConditions = f.text("Conditions", "e.g. 22°C, 60% RH");
        f.wide(UI.buttons(
                UI.btn("Assign Location", "primary", this::assignLocation),
                UI.btn("Save Changes", "secondary", this::editEntry),
                UI.btn("Delete Entry", "danger", this::deleteEntry)));
        VBox assignCard = UI.card(UI.cardTitle("Assign Storage Location"), f.grid);
        assignCard.setPrefWidth(360);

        // ---------- Left panel: storage actions ----------
        VBox actions = new VBox(10,
                UI.btn("Rotate Stock FIFO (oldest first)", "secondary", this::rotateFifo),
                UI.btn("Refresh", "ghost", this::refreshAll)
        );
        actions.setMaxWidth(Double.MAX_VALUE);
        for (Object o : actions.getChildren()) {
            if (o instanceof Button) ((Button) o).setMaxWidth(Double.MAX_VALUE);
        }
        VBox actionCard = UI.card(UI.cardTitle("Storage Actions"), actions);
        actionCard.setPrefWidth(360);

        VBox left = new VBox(14, assignCard, actionCard);
        left.setPrefWidth(380);

        // ---------- Right panel: storage entries + stock status ----------
        UI.col(storeTable, "Batch ID", e -> e.batchId);
        UI.col(storeTable, "Location", e -> e.location);
        UI.col(storeTable, "Qty (kg)", e -> String.format("%,.2f", e.qty));
        UI.col(storeTable, "Date", e -> e.storageDate.toString());
        UI.col(storeTable, "Conditions", e -> e.conditions);
        UI.col(storeTable, "Rotations", e -> String.valueOf(e.rotations));
        storeTable.getSelectionModel().selectedItemProperty().addListener((o, a, e) -> {
            if (e == null) return;
            editingEntry = e;
            batchSelect.setValue(e.batchId);
            int idx = -1;
            for (int i = 0; i < sys.locations.length; i++) if (sys.locations[i].equals(e.location)) idx = i;
            if (idx >= 0) locationSelect.getSelectionModel().select(idx);
            storageQty.setText(String.valueOf(e.qty));
            storageDate.setValue(e.storageDate);
            storageConditions.setText(e.conditions);
        });

        VBox storeCard = UI.card(UI.cardTitle("Storage Entries (FIFO rotation queue)"), storeTable);
        VBox.setVgrow(storeTable, Priority.ALWAYS);
        VBox.setVgrow(storeCard, Priority.ALWAYS);

        UI.col(stockTable, "Item ID", s -> s.itemId);
        UI.col(stockTable, "Crop", s -> s.crop);
        UI.col(stockTable, "Location", s -> s.location);
        UI.col(stockTable, "Stored (kg)", s -> String.format("%,.2f", s.stored));
        UI.col(stockTable, "Remaining (kg)", s -> String.format("%,.2f", s.remaining));

        VBox stockCard = UI.card(UI.cardTitle("Stock Storage Status"), stockTable);
        VBox.setVgrow(stockTable, Priority.ALWAYS);
        VBox.setVgrow(stockCard, Priority.ALWAYS);

        VBox right = new VBox(14, storeCard, stockCard);
        VBox.setVgrow(right, Priority.ALWAYS);

        getChildren().addAll(UI.split(left, right, 380));
    }

    @Override public String title() { return "6  Storage"; }
    @Override public Feature feature() { return Feature.STORAGE; }

    private void assignLocation() {
        String bid = batchSelect.getValue();
        if (bid == null) throw new AgriException("Select a batch / item.");
        int loc = locationSelect.getSelectionModel().getSelectedIndex();
        if (loc < 0) throw new AgriException("Select a storage location.");
        double qty = UI.num(storageQty, "Qty");
        LocalDate date = storageDate.getValue();
        if (date == null) throw new AgriException("Storage date is required.");
        String conditions = storageConditions.getText();
        sys.assignLocation(bid, loc, qty, date, conditions);
        clearForm();
        refreshAll();
    }

    private void clearForm() {
        editingEntry = null;
        batchSelect.setValue(null);
        locationSelect.setValue(null);
        storageQty.clear(); storageConditions.clear();
        storageDate.setValue(LocalDate.now());
        storeTable.getSelectionModel().clearSelection();
    }

    private void editEntry() {
        if (editingEntry == null) throw new AgriException("Select a storage entry to edit.");
        int loc = locationSelect.getSelectionModel().getSelectedIndex();
        if (loc < 0) throw new AgriException("Select a storage location.");
        double qty = UI.num(storageQty, "Qty");
        LocalDate date = storageDate.getValue();
        if (date == null) throw new AgriException("Storage date is required.");
        sys.editStorageEntry(editingEntry, loc, qty, date, storageConditions.getText());
        clearForm(); refreshAll();
    }

    private void deleteEntry() {
        if (editingEntry == null) throw new AgriException("Select a storage entry to delete.");
        if (!UI.confirm("Delete this storage entry for " + editingEntry.batchId + "?")) return;
        sys.deleteStorageEntry(editingEntry);
        clearForm(); refreshAll();
    }

    private void rotateFifo() {
        if (!UI.confirm("Move the oldest stored lot to the front of the FIFO queue?")) return;
        sys.rotateStockFIFO();
        refreshAll();
    }

    @Override public void refresh() {
        // Batches that still have stock left to store.
        List<String> ids = new ArrayList<>();
        for (StockItem s : sys.traverseInventory()) {
            if (s.remaining > 0.0001) ids.add(s.itemId);
        }
        if (editingEntry != null && !ids.contains(editingEntry.batchId)) ids.add(editingEntry.batchId);
        UI.setItems(batchSelect, ids);

        // Storage locations from the system array.
        List<String> locs = new ArrayList<>();
        for (int i = 0; i < sys.locations.length; i++) locs.add((i + 1) + ". " + sys.locations[i]);
        if (locationSelect.getItems().isEmpty()) locationSelect.setItems(FXCollections.observableArrayList(locs));

        List<StorageEntry> entries = new ArrayList<>();
        for (StorageEntry e : sys.rotationQueue) entries.add(e);
        UI.fill(storeTable, entries);

        List<StockItem> items = new ArrayList<>();
        for (StockItem s : sys.traverseInventory()) items.add(s);
        UI.fill(stockTable, items);
    }
}
