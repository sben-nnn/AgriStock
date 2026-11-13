package com.agristock.ui;

import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Arrays;

/** MODULE 5: Quality Inspection & Grading (FIFO queue). */
public class InspectionView extends View {
    private final TableView<Batch> queue = UI.table(), results = UI.table();
    private final ComboBox<String> status, grade;
    private final TextField unitValue;
    private final TextArea notes;
    private final Label next = new Label();

    public InspectionView(Ctx ctx) {
        super(ctx);
        header("5 - Quality Inspection & Grading", "Batches are inspected strictly first-in, first-out. Only accepted or downgraded stock enters saleable inventory.");
        next.setWrapText(true); next.getStyleClass().add("card-title");
        UI.Form f = new UI.Form();
        f.wide(next);
        status = f.combo("Result", Arrays.asList("ACCEPTED", "DOWNGRADED", "REJECTED")); status.setValue("ACCEPTED");
        grade = f.combo("Grade", Arrays.asList("A", "B", "C")); grade.setValue("A");
        unitValue = f.text("Value per kg", "Estimated price in \u20B1");
        notes = f.area("Justification", 3);
        f.wide(UI.hint("A justification is required for downgraded and rejected batches."));
        f.wide(UI.btn("Inspect & grade next batch", "primary", this::inspect));

        UI.col(queue, "#", b -> String.valueOf(position(b)));
        UI.col(queue, "Batch", b -> b.batchId);
        UI.col(queue, "Crop", b -> b.crop);
        UI.col(queue, "Saleable (kg)", b -> { StockRecord r = sys.findStockRecord(b.batchId); return r == null ? "" : Fmt.kg(r.saleableQty); });
        UI.col(queue, "Harvested", b -> b.harvestDate.toString());
        UI.col(results, "Batch", b -> b.batchId);
        UI.col(results, "Crop", b -> b.crop);
        UI.col(results, "Status", b -> b.status.name());
        UI.col(results, "Grade", b -> b.grade);
        UI.col(results, "Note", b -> { StockRecord r = sys.findStockRecord(b.batchId); return r == null ? "" : r.rejectReason; });
        TabPane tabs = new TabPane(new Tab("Inspection queue (FIFO)", queue), new Tab("Results", results));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabs, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(UI.split(UI.card(UI.cardTitle("Inspect next batch"), f.grid), UI.card(tabs), 340));
    }

    private int position(Batch target) {
        int i = 1;
        for (Batch b : sys.inspectionQueue) { if (b == target) return i; i++; }
        return 0;
    }

    @Override public String title() { return "5  Inspection & Grading"; }
    @Override public Feature feature() { return Feature.INSPECTION; }

    private void inspect() {
        double v = status.getValue() != null && !status.getValue().equals("REJECTED") ? UI.num(unitValue, "Value per kg") : 0;
        Batch b = sys.inspectBatch(status.getValue(), grade.getValue(), notes.getText(), v);
        notes.clear(); unitValue.clear(); refresh();
        UI.info("Inspection complete", "Batch " + b.batchId + " was classified as " + b.status + ".");
    }

    @Override public void refresh() {
        UI.fill(queue, sys.inspectionQueue);
        java.util.List<Batch> done = new java.util.ArrayList<>();
        sys.batches.inOrder(b -> { if (b.status == BatchStatus.ACCEPTED || b.status == BatchStatus.DOWNGRADED || b.status == BatchStatus.REJECTED) done.add(b); });
        UI.fill(results, done);
        Batch front = sys.inspectionQueue.peek();
        next.setText(front == null ? "Queue is empty - nothing to inspect." : "Next in queue: " + front.batchId + " (" + front.crop + ")");
    }
}
