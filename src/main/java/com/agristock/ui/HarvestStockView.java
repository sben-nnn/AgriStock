package com.agristock.ui;

import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** MODULE 4: Harvest Stock Recording (linked list + linear search). */
public class HarvestStockView extends View {
    private final TableView<StockRecord> table = UI.table();
    private final ComboBox<String> batch, grade, ripeness;
    private final TextField harvested, rejected, moisture;
    private final TextArea reason;
    private final Label info = UI.hint("Select a batch to see its registered quantity.");

    public HarvestStockView(Ctx ctx) {
        super(ctx);
        header("4 - Harvest Stock Recording", "Records go into a linked list (newest first). Linear search blocks duplicate entries; saleable and rejected produce are separated.");
        UI.Form f = new UI.Form();
        batch = f.combo("Batch", new ArrayList<>());
        f.wide(info);
        harvested = f.text("Harvested (kg)");
        rejected = f.text("Rejected (kg)"); rejected.setText("0");
        grade = f.combo("Grade", Arrays.asList("A", "B", "C")); grade.setValue("A");
        ripeness = f.combo("Ripeness", Arrays.asList("N/A", "Unripe", "Ripe", "Overripe")); ripeness.setValue("N/A");
        moisture = f.text("Moisture (%)"); moisture.setText("0");
        reason = f.area("Reject reason", 2);
        f.wide(UI.btn("Record harvested stock", "primary", this::record));
        batch.valueProperty().addListener((o, a, v) -> {
            Batch b = v == null ? null : sys.batches.search(UI.idOf(batch));
            if (b != null) { info.setText("Registered: " + Fmt.kg(b.quantityKg) + " kg of " + b.crop + ". Recording queues it for inspection."); harvested.setText(String.valueOf(b.quantityKg)); }
        });
        UI.col(table, "Batch", r -> r.batchId);
        UI.col(table, "Harvested (kg)", r -> Fmt.kg(r.harvestedQty));
        UI.col(table, "Saleable (kg)", r -> Fmt.kg(r.saleableQty));
        UI.col(table, "Rejected (kg)", r -> Fmt.kg(r.rejectedQty));
        UI.col(table, "Grade", r -> r.grade);
        UI.col(table, "Ripeness", r -> r.ripeness);
        UI.col(table, "Moisture %", r -> Fmt.kg(r.moisture));
        UI.col(table, "Reject reason", r -> r.rejectReason);
        UI.col(table, "Recorded", r -> UI.d(r.recorded));
        getChildren().add(UI.split(UI.card(UI.cardTitle("Record harvested stock"), f.grid), UI.card(UI.cardTitle("Stock records"), table), 340));
    }

    @Override public String title() { return "4  Harvest Stock"; }
    @Override public Feature feature() { return Feature.HARVEST_STOCK; }

    private void record() {
        sys.recordHarvestedStock(UI.idOf(batch), UI.num(harvested, "Harvested quantity"), UI.num(rejected, "Rejected quantity"),
                grade.getValue(), ripeness.getValue(), UI.num(moisture, "Moisture"), reason.getText());
        reason.clear(); harvested.clear(); rejected.setText("0"); batch.setValue(null);
        refresh();
        UI.info("Recorded", "Stock recorded and the batch was added to the inspection queue.");
    }

    @Override public void refresh() {
        UI.fill(table, sys.stockRecords);
        List<String> l = new ArrayList<>();
        sys.batches.inOrder(b -> { if (b.status == BatchStatus.REGISTERED) l.add(b.batchId + " | " + b.crop + " (" + Fmt.kg(b.quantityKg) + " kg)"); });
        UI.setItems(batch, l);
    }
}
