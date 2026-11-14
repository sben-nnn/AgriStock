package com.agristock.ui;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** MODULE 6: Storage & Location (array + FIFO queue rotation). */
public class StorageView extends View {
    private final TableView<StorageEntry> table = UI.table();
    private final ComboBox<String> item = new ComboBox<>(), location = new ComboBox<>();
    private final TextField qty, conditions;
    private final LocalDate[] dateHolder = { null };

    public StorageView(Ctx ctx) {
        super(ctx);
        header("6 - Storage & Location", "Assign batches to storage locations from the fixed location array. FIFO rotation moves the oldest lot to the front.");
        UI.Form f = new UI.Form();
        f.add("Batch", item);
        location.setItems(FXCollections.observableArrayList(sys.locations));
        f.add("Location", location);
        qty = f.text("Quantity (kg)");
        dateHolder[0] = sys.today();
        f.add("Storage Date", new DatePicker(sys.today()) {{
            setOnAction(e -> dateHolder[0] = ((DatePicker) e.getSource()).getValue());
        }});
        conditions = f.text("Conditions");
        f.wide(UI.btn("Assign Location", "primary", this::assign));
        f.wide(UI.btn("FIFO Rotate (oldest first)", "secondary", this::rotate));
        UI.col(table, "Batch", e -> e.batchId); UI.col(table, "Location", e -> e.location);
        UI.col(table, "Qty (kg)", e -> Fmt.kg(e.qty)); UI.col(table, "Date", e -> e.storageDate.toString());
        UI.col(table, "Conditions", e -> e.conditions); UI.col(table, "Rotations", e -> String.valueOf(e.rotations));
        getChildren().add(UI.split(new VBox(14, UI.card(UI.cardTitle("Assign Storage"), f.grid),
                UI.card(UI.cardTitle("Rotate Stock"), UI.btn("Rotate oldest lot to front (FIFO)", "secondary", this::rotate))),
                UI.card(UI.cardTitle("Stored Lots (rotation order)"), table), 340));
    }

    @Override public String title() { return "6  Storage"; }
    @Override public Feature feature() { return Feature.STORAGE; }

    private void assign() {
        String itemId = UI.idOf(item);
        if (itemId == null) throw new AgriException("Select a batch.");
        int loc = location.getSelectionModel().getSelectedIndex();
        if (loc < 0) throw new AgriException("Select a location.");
        double q = UI.num(qty, "Quantity");
        LocalDate d = dateHolder[0];
        if (d == null) throw new AgriException("Storage date is required.");
        StorageEntry e = sys.assignLocation(itemId, loc, q, d, conditions.getText());
        qty.clear(); conditions.clear(); refresh();
        UI.info("Stored", e.qty + " kg of " + e.batchId + " at " + e.location);
    }

    private void rotate() {
        StorageEntry e = sys.rotateStockFIFO();
        refresh();
        UI.info("Rotated", "Oldest lot " + e.batchId + " at " + e.location + " moved to front (rotation #" + e.rotations + ")");
    }

    @Override public void refresh() {
        List<StorageEntry> list = new ArrayList<>();
        for (StorageEntry e : sys.rotationQueue) list.add(e);
        UI.fill(table, list);
        List<String> items = new ArrayList<>();
        for (StockItem s : sys.traverseInventory()) {
            double remaining = Math.max(0, s.remaining - s.stored);
            if (remaining > 0) items.add(s.itemId + " | " + s.crop + " (" + Fmt.kg(remaining) + " kg not yet stored)");
        }
        UI.setItems(item, items);
    }
}
