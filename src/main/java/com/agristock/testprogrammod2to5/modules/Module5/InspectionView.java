package com.agristock.testprogrammod2to5.modules.Module5;

import com.agristock.testprogrammod2to5.model.AgriException;
import com.agristock.testprogrammod2to5.model.Models.Batch;
import com.agristock.testprogrammod2to5.model.Models.BatchStatus;
import com.agristock.testprogrammod2to5.model.Models.Feature;
import com.agristock.testprogrammod2to5.model.Models.StockRecord;
import com.agristock.testprogrammod2to5.ui.View;
import com.agristock.testprogrammod2to5.ui.UI;
import com.agristock.testprogrammod2to5.ui.Ctx;
import com.agristock.testprogrammod2to5.service.AgriStockSystem;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

// MODULE 5: Quality Inspection & Grading (Queue, FIFO scheduling)
public class InspectionView extends View {
    private final TableView<Batch> queueTable = UI.table();
    private final TableView<Batch> inspectedTable = UI.table();
    private final ComboBox<BatchStatus> status = new ComboBox<>();
    private TextField grade, notes, unitValue;
    private final InspectionService inspectionService;

    public InspectionView(Ctx ctx) {
        super(ctx);
        inspectionService = new InspectionService(sys);
        build();
    }

    private void build() {
        header("5 - Quality Inspection & Grading",
                "FIFO Queue - batches are inspected front-first (earliest queued batch) using enqueue/dequeue.");

        // ---------- Left panel: inspect front batch ----------
        UI.Form f = new UI.Form();
        f.add("Status", status);
        status.getItems().addAll(BatchStatus.ACCEPTED, BatchStatus.DOWNGRADED, BatchStatus.REJECTED);
        status.setValue(BatchStatus.ACCEPTED);
        grade = f.text("Grade");
        notes = f.text("Notes (required if not accepted)");
        unitValue = f.text("Unit Value (per kg)");
        f.wide(UI.btn("Inspect Front Batch", "primary", this::inspectFront));
        VBox inspectCard = UI.card(UI.cardTitle("Inspect Next Batch"), f.grid);
        inspectCard.setPrefWidth(380);

        // ---------- Left panel: actions ----------
        VBox actions = new VBox(10,
                UI.btn("Refresh", "ghost", () -> refresh())
        );
        actions.setMaxWidth(Double.MAX_VALUE);
        for (Object o : actions.getChildren()) {
            if (o instanceof Button) ((Button) o).setMaxWidth(Double.MAX_VALUE);
        }
        VBox actionCard = UI.card(UI.cardTitle("Actions"), actions);
        actionCard.setPrefWidth(380);

        VBox left = new VBox(14, inspectCard, actionCard);
        left.setPrefWidth(400);

        // ---------- Right panel: queue + inspected ----------
        UI.col(queueTable, "Batch ID", b -> b.batchId);
        UI.col(queueTable, "Farm", b -> b.farmId);
        UI.col(queueTable, "Crop", b -> b.crop);
        UI.col(queueTable, "Variety", b -> b.variety);
        UI.col(queueTable, "Qty (kg)", b -> String.format("%,.2f", b.quantityKg));
        UI.col(queueTable, "Status", b -> b.status.toString());

        VBox queueCard = UI.card(UI.cardTitle("Inspection Queue (FIFO - front first)"), queueTable);
        VBox.setVgrow(queueTable, Priority.ALWAYS);
        VBox.setVgrow(queueCard, Priority.ALWAYS);

        UI.col(inspectedTable, "Batch ID", b -> b.batchId);
        UI.col(inspectedTable, "Crop", b -> b.crop);
        UI.col(inspectedTable, "Grade", b -> b.grade);
        UI.col(inspectedTable, "Status", b -> b.status.toString());

        VBox inspectedCard = UI.card(UI.cardTitle("Inspected Batches"), inspectedTable);
        VBox.setVgrow(inspectedTable, Priority.ALWAYS);
        VBox.setVgrow(inspectedCard, Priority.ALWAYS);

        VBox right = new VBox(14, queueCard, inspectedCard);
        VBox.setVgrow(right, Priority.ALWAYS);

        getChildren().addAll(UI.split(left, right, 400));
    }

    @Override public String title() { return "5  Inspection"; }
    @Override public Feature feature() { return Feature.INSPECTION; }

    private void inspectFront() {
        if (sys.inspectionQueue.isEmpty()) {
            UI.info("Nothing to inspect", "The inspection queue is empty.");
            return;
        }
        BatchStatus sel = status.getValue();
        if (sel == null) throw new AgriException("Select an inspection result.");
        String g = grade.getText();
        String n = notes.getText();
        double uv = 0;
        String uvText = unitValue.getText().trim();
        if (!uvText.isEmpty()) uv = UI.num(unitValue, "Unit Value");
        inspectionService.inspect(sel, g, n, uv);
        grade.clear(); notes.clear(); unitValue.clear();
        refresh();
    }

    @Override public void refresh() {
        // Queue: batches awaiting inspection, front first.
        List<Batch> queue = new ArrayList<>();
        for (Batch b : sys.inspectionQueue) queue.add(b);
        UI.fill(queueTable, queue);

        // Inspected: all registered batches (sorted), showing their latest status.
        List<Batch> inspected = new ArrayList<>();
        for (Batch b : sys.batches.toSortedArray()) inspected.add(b);
        UI.fill(inspectedTable, inspected);
    }
}
