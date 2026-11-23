package com.agristock.test;

import com.agristock.model.AgriException;
import com.agristock.model.Models.Farmer;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.HarvestLog;
import com.agristock.service.Fmt;
import com.agristock.ui.Ctx;
import com.agristock.ui.UI;
import com.agristock.ui.View;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TEST-ONLY screen for module 2 (Farmer Tracking: linked list + linear search).
 *
 * The project has the farmer logic in AgriStockSystem (registerFarmer, deleteFarmer, recordHarvest,
 * findFarmer, nextFarmerId) but no screen for it, so this gives those methods something to click on.
 * It adds no logic of its own; every action calls the real system method. It is not used by MainApp.
 */
public class FarmersTestView extends View {
    private final TableView<Farmer> farmerTable = UI.table();
    private final TableView<HarvestLog> logTable = UI.table();
    private final ComboBox<String> farmerSelect = new ComboBox<>();
    private final TextField farmerId, name, contact, crop, qty, findId;
    private final DatePicker date;

    public FarmersTestView(Ctx ctx) {
        super(ctx);
        header("2 - Farmer Tracking (test-only screen)",
                "The main app has no screen for module 2, so this is a minimal one over the real farmer methods (linked list + linear search).");

        UI.Form reg = new UI.Form();
        farmerId = reg.text("Farmer ID");
        name = reg.text("Name");
        contact = reg.text("Contact");
        reg.wide(UI.btn("Register Farmer", "primary", this::register));
        reg.wide(UI.btn("Delete selected farmer", "danger", this::delete));

        UI.Form har = new UI.Form();
        har.add("Farmer ID", farmerSelect);
        crop = har.text("Crop");
        qty = har.text("Qty (kg)");
        date = har.date("Date", LocalDate.now());
        har.wide(UI.btn("Record Harvest", "secondary", this::recordHarvest));

        UI.Form find = new UI.Form();
        findId = find.text("Farmer ID", "e.g. FR-1002");
        find.wide(UI.btn("Find (linear search)", "secondary", this::find));

        UI.col(farmerTable, "ID", f -> f.id);
        UI.col(farmerTable, "Name", f -> f.name);
        UI.col(farmerTable, "Contact", f -> f.contact);
        UI.col(farmerTable, "Harvest logs", f -> String.valueOf(f.harvests.size()));
        UI.col(farmerTable, "Total harvest (kg)", f -> Fmt.kg(f.totalHarvest()));
        farmerTable.getSelectionModel().selectedItemProperty().addListener((o, a, f) -> showLogs(f));

        UI.col(logTable, "Crop", h -> h.cropType);
        UI.col(logTable, "Qty (kg)", h -> Fmt.kg(h.quantity));
        UI.col(logTable, "Date", h -> h.date.toString());

        VBox left = new VBox(14,
                UI.card(UI.cardTitle("Register farmer"), reg.grid),
                UI.card(UI.cardTitle("Record harvest"), har.grid),
                UI.card(UI.cardTitle("Find farmer"), find.grid));
        VBox right = new VBox(14,
                UI.card(UI.cardTitle("Farmers (linked list)"), farmerTable),
                UI.card(UI.cardTitle("Harvest logs of the selected farmer"), logTable));
        getChildren().add(UI.split(left, right, 340));
    }

    @Override public String title() { return "2  Farmers"; }
    @Override public Feature feature() { return Feature.BATCHES; }   // same permission the system uses for farmer actions

    private void register() {
        sys.registerFarmer(farmerId.getText(), name.getText(), contact.getText());
        farmerId.clear(); name.clear(); contact.clear();
        refresh();
    }

    private void delete() {
        Farmer f = farmerTable.getSelectionModel().getSelectedItem();
        if (f == null) throw new AgriException("Select a farmer in the table first.");
        if (!UI.confirm("Delete farmer " + f.id + " (" + f.name + ")?")) return;
        sys.deleteFarmer(f.id);
        refresh();
    }

    private void recordHarvest() {
        String id = farmerSelect.getValue();
        if (id == null) throw new AgriException("Select a farmer ID.");
        sys.recordHarvest(id, crop.getText(), UI.num(qty, "Qty"), date.getValue());
        crop.clear(); qty.clear();
        refresh();
    }

    private void find() {
        Farmer f = sys.findFarmer(findId.getText().trim());
        if (f == null) throw new AgriException("Farmer not found.");
        farmerTable.getSelectionModel().select(f);
        farmerTable.scrollTo(f);
    }

    private void showLogs(Farmer f) {
        List<HarvestLog> logs = new ArrayList<>();
        if (f != null) for (HarvestLog h : f.harvests) logs.add(h);
        UI.fill(logTable, logs);
    }

    @Override public void refresh() {
        Farmer keep = farmerTable.getSelectionModel().getSelectedItem();
        String keepId = keep == null ? null : keep.id;

        List<Farmer> farmers = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (Farmer f : sys.farmers) { farmers.add(f); ids.add(f.id); }
        UI.fill(farmerTable, farmers);
        UI.setItems(farmerSelect, ids);

        if (keepId != null)
            for (Farmer f : farmers) if (f.id.equals(keepId)) farmerTable.getSelectionModel().select(f);
        showLogs(farmerTable.getSelectionModel().getSelectedItem());

        if (farmerId.getText().isEmpty()) farmerId.setText(sys.nextFarmerId());   // suggested next ID
    }
}