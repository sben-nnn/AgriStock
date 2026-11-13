package com.agristock.modules.Module4;

import com.agristock.model.AgriException;
import com.agristock.model.Models.Batch;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.StockRecord;
import com.agristock.ui.View;
import com.agristock.ui.UI;
import com.agristock.ui.Ctx;
import com.agristock.service.AgriStockSystem;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// MODULE 4: Harvest Stock Recording (Linked List + Linear Search)
public class HarvestStockView extends View {
    private final TableView<StockRecord> recordTable = UI.table();
    private final ComboBox<String> batchSelect = new ComboBox<>();
    private TextField harvestedQty, rejectedQty, grade, ripeness, moisture, rejectReason;

    public HarvestStockView(Ctx ctx) { super(ctx); build(); }

    private void build() {
        header("4 - Harvest Stock Recording", "Record harvested stock quantities and quality. Stock records use a linked list; lookup uses linear search.");
        UI.Form f = new UI.Form();
        f.add("Batch ID", batchSelect);
        harvestedQty = f.text("Harvested Qty (kg)");
        rejectedQty = f.text("Rejected Qty (kg)");
        grade = f.text("Grade");
        ripeness = f.text("Ripeness");
        moisture = f.text("Moisture (%)");
        rejectReason = f.text("Reject Reason");
        f.wide(UI.btn("Record Stock", "primary", this::recordStock));

        UI.col(recordTable, "Batch ID", r -> r.batchId);
        UI.col(recordTable, "Harvested (kg)", r -> String.format("%,.2f", r.harvestedQty));
        UI.col(recordTable, "Rejected (kg)", r -> String.format("%,.2f", r.rejectedQty));
        UI.col(recordTable, "Saleable (kg)", r -> String.format("%,.2f", r.saleableQty));
        UI.col(recordTable, "Grade", r -> r.grade);
        UI.col(recordTable, "Moisture (%)", r -> String.format("%.1f", r.moisture));
        UI.col(recordTable, "Recorded", r -> r.recorded.toString());

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
        double moist = UI.num(moisture, "Moisture");
        sys.recordHarvestedStock(bid, harvested, rejected, grade.getText(), ripeness.getText(), moist, rejectReason.getText());
        harvestedQty.clear(); rejectedQty.clear(); grade.clear(); ripeness.clear();
        moisture.clear(); rejectReason.clear();
        refresh();
    }

    @Override public void refresh() {
        List<String> bids = new ArrayList<>();
        for (Batch b : sys.batches.toSortedArray()) bids.add(b.batchId);
        UI.setItems(batchSelect, bids);

        List<StockRecord> records = new ArrayList<>();
        for (StockRecord r : sys.stockRecords) records.add(r);
        UI.fill(recordTable, records);
    }
}
