package com.agristock.modules.Module4;

import com.agristock.model.AgriException;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.StockRecord;
import com.agristock.ui.View;
import com.agristock.ui.UI;
import com.agristock.ui.Ctx;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

// MODULE 4: Harvest Stock Recording (Linked List + Linear Search)
public class HarvestStockView extends View {
    private final TableView<StockRecord> recordTable = UI.table();
    private final ComboBox<String> batchSelect = new ComboBox<>();
    private final HarvestStockService svc;
    private TextField harvestedQty, rejectedQty, rejectReason;
    private ComboBox<String> grade, ripeness;
    private String editingBatchId;

    // Grade and ripeness options offered as selections.
    private static final List<String> GRADES = List.of("A", "B", "C", "D", "E", "F");
    private static final List<String> RIPENESS = List.of("N/A", "Ripe", "Unripe");

    public HarvestStockView(Ctx ctx) { super(ctx); svc = new HarvestStockService(sys); build(); }

    private void build() {
        header("4 - Harvest Stock Recording", "");
        UI.Form f = new UI.Form();
        f.add("Batch ID", batchSelect);
        harvestedQty = f.text("Harvested Qty (kg)", "e.g. 500");
        rejectedQty = f.text("Rejected Qty (kg)", "e.g. 50");
        grade = f.combo("Grade", GRADES);
        ripeness = f.combo("Ripeness", RIPENESS);
        rejectReason = f.text("Reject Reason", "e.g. Moldy grains");
        f.wide(UI.buttons(
                UI.btn("Record Stock", "primary", this::recordStock),
                UI.btn("Save Changes", "secondary", this::editRecord),
                UI.btn("Delete Record", "danger", this::deleteRecord)));

        UI.col(recordTable, "Batch ID", r -> r.batchId);
        UI.col(recordTable, "Harvested (kg)", r -> String.format("%,.2f", r.harvestedQty));
        UI.col(recordTable, "Rejected (kg)", r -> String.format("%,.2f", r.rejectedQty));
        UI.col(recordTable, "Saleable (kg)", r -> String.format("%,.2f", r.saleableQty));
        UI.col(recordTable, "Grade", r -> r.grade);
        UI.col(recordTable, "Recorded", r -> r.recorded.toString());
        recordTable.getSelectionModel().selectedItemProperty().addListener((o, a, r) -> {
            if (r == null) return;
            editingBatchId = r.batchId;
            batchSelect.setValue(r.batchId);
            harvestedQty.setText(String.valueOf(r.harvestedQty));
            rejectedQty.setText(String.valueOf(r.rejectedQty));
            grade.setValue(r.grade); ripeness.setValue(r.ripeness);
            rejectReason.setText(r.rejectReason);
        });

        getChildren().addAll(
                UI.card(UI.cardTitle("Record Harvested Stock"), f.grid),
                UI.card(UI.cardTitle("Stock Records (linked list)"), recordTable)
        );
    }

    @Override public String title() { return "4  Harvest Stock"; }
    @Override public Feature feature() { return Feature.HARVEST_STOCK; }

    private void recordStock() {
        String bid = batchSelect.getValue();
        if (bid == null) throw new AgriException("Select a batch.");
        double harvested = UI.num(harvestedQty, "Harvested Qty");
        double rejected = UI.num(rejectedQty, "Rejected Qty");
        svc.record(bid, harvested, rejected, grade.getValue(), ripeness.getValue(), rejectReason.getText());
        clearForm(); refreshAll();
    }

    private void clearForm() {
        editingBatchId = null;
        batchSelect.setValue(null);
        harvestedQty.clear(); rejectedQty.clear(); grade.setValue(null); ripeness.setValue(null);
        rejectReason.clear();
        recordTable.getSelectionModel().clearSelection();
    }

    private void editRecord() {
        if (editingBatchId == null) throw new AgriException("Select a stock record to edit.");
        double harvested = UI.num(harvestedQty, "Harvested Qty");
        double rejected = UI.num(rejectedQty, "Rejected Qty");
        svc.edit(editingBatchId, harvested, rejected, grade.getValue(), ripeness.getValue(), rejectReason.getText());
        clearForm(); refreshAll();
    }

    private void deleteRecord() {
        if (editingBatchId == null) throw new AgriException("Select a stock record to delete.");
        if (!UI.confirm("Delete the stock record for batch " + editingBatchId + "?")) return;
        svc.delete(editingBatchId);
        clearForm(); refreshAll();
    }

    @Override public void refresh() {
        // Only batches that do not have a harvest stock record yet.
        List<String> bids = svc.eligibleBatchIds();
        if (editingBatchId != null && !bids.contains(editingBatchId)) bids.add(editingBatchId);
        UI.setItems(batchSelect, bids);

        List<StockRecord> records = new ArrayList<>();
        for (StockRecord r : svc.getAll()) records.add(r);
        UI.fill(recordTable, records);
    }
}