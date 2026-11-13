package com.agristock.modules.Module2;

import com.agristock.model.AgriException;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.Farmer;
import com.agristock.model.Models.HarvestLog;
import com.agristock.ui.View;
import com.agristock.ui.UI;
import com.agristock.ui.Ctx;
import com.agristock.service.AgriStockSystem;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// MODULE 2: Farmer Tracking (Linked List + Linear Search)
public class FarmersView extends View {
    private final TableView<Farmer> farmerTable = UI.table();
    private final TableView<HarvestLog> harvestTable = UI.table();
    private final ComboBox<String> farmerSelect = new ComboBox<>();
    private TextField farmerId, farmerName, farmerContact;
    private TextField harvestCrop, harvestQty, harvestDate;

    public FarmersView(Ctx ctx) { super(ctx); build(); }

    private void build() {
        header("2 - Farmer Tracking", "Register farmers and record their harvests. Harvest logs use a linked list; farmer lookup uses linear search.");
        UI.Form f = new UI.Form();
        f.add("Farmer ID", farmerId = f.text("Farmer ID"));
        f.add("Name", farmerName = f.text("Name"));
        f.add("Contact", farmerContact = f.text("Contact"));
        f.wide(UI.btn("Register Farmer", "primary", this::registerFarmer));

        UI.Form h = new UI.Form();
        h.add("Farmer ID", farmerSelect);
        h.add("Crop", harvestCrop = h.text("Crop"));
        h.add("Qty (kg)", harvestQty = h.text("Qty (kg)"));
        h.add("Date", harvestDate = h.text("Date (YYYY-MM-DD)"));
        h.wide(UI.btn("Record Harvest", "secondary", this::recordHarvest));

        UI.col(farmerTable, "ID", f -> f.id);
        UI.col(farmerTable, "Name", f -> f.name);
        UI.col(farmerTable, "Contact", f -> f.contact);
        UI.col(farmerTable, "Total Harvest", f -> String.format("%,.2f kg", f.totalHarvest()));

        UI.col(harvestTable, "Crop", h -> h.cropType);
        UI.col(harvestTable, "Qty (kg)", h -> String.format("%,.2f", h.quantity));
        UI.col(harvestTable, "Date", h -> h.date.toString());

        getChildren().addAll(
                UI.card(UI.cardTitle("Register Farmer"), f.grid),
                UI.card(UI.cardTitle("Record Harvest"), h.grid),
                UI.card(UI.cardTitle("Farmers (linear search)"), farmerTable),
                UI.card(UI.cardTitle("Harvest Logs (linked list)"), harvestTable)
        );
    }

    @Override public String title() { return "2  Farmers"; }
    @Override public Feature feature() { return Feature.FARMERS; }

    private void registerFarmer() {
        sys.registerFarmer(farmerId.getText(), farmerName.getText(), farmerContact.getText());
        farmerId.clear(); farmerName.clear(); farmerContact.clear();
        refresh();
    }

    private void recordHarvest() {
        String fid = farmerSelect.getValue();
        if (fid == null) throw new AgriException("Select a farmer.");
        String crop = harvestCrop.getText();
        double qty = UI.num(harvestQty, "Qty");
        LocalDate date = LocalDate.parse(harvestDate.getText());
        sys.recordHarvest(fid, crop, qty, date);
        harvestCrop.clear(); harvestQty.clear(); harvestDate.clear();
        refresh();
    }

    @Override public void refresh() {
        List<Farmer> list = new ArrayList<>();
        for (Farmer f : sys.farmers) list.add(f);
        UI.fill(farmerTable, list);

        List<String> ids = new ArrayList<>();
        for (Farmer f : sys.farmers) ids.add(f.id);
        UI.setItems(farmerSelect, ids);

        Farmer selected = farmerSelect.getValue() != null ? sys.findFarmer(farmerSelect.getValue()) : null;
        if (selected != null) {
            List<HarvestLog> logs = new ArrayList<>();
            for (HarvestLog h : selected.harvests) logs.add(h);
            UI.fill(harvestTable, logs);
        } else {
            UI.fill(harvestTable, new ArrayList<>());
        }
    }
}
