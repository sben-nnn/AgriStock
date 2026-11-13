package com.agristock.ui;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** MODULE 2: Farmer Tracking (linked list + linear search). */
public class FarmersView extends View {
    private final TableView<Farmer> farmers = UI.table();
    private final TableView<HarvestLog> logs = UI.table();
    private final TextField id, name, contact, qty;
    private final ComboBox<String> crop = new ComboBox<>();
    private final DatePicker date;

    public FarmersView(Ctx ctx) {
        super(ctx);
        header("2 - Farmer Tracking", "Farmer profiles and harvest logs live in linked lists; lookups use linear search from the head node.");
        UI.Form reg = new UI.Form();
        id = reg.text("Farmer ID", "e.g. F-004");
        name = reg.text("Name");
        contact = reg.text("Contact no.");
        reg.wide(UI.btn("Register farmer", "primary", this::register));

        UI.Form har = new UI.Form();
        har.wide(UI.hint("Select a farmer in the table, then log a harvest."));
        crop.setEditable(true); har.add("Crop", crop);
        qty = har.text("Quantity (kg)");
        date = har.date("Harvest date", LocalDate.now());
        har.wide(UI.btn("Record harvest", "primary", this::record));

        VBox left = new VBox(14, UI.card(UI.cardTitle("Register farmer"), reg.grid), UI.card(UI.cardTitle("Record harvest activity"), har.grid));

        UI.col(farmers, "ID", f -> f.id);
        UI.col(farmers, "Name", f -> f.name);
        UI.col(farmers, "Contact", f -> f.contact);
        UI.col(farmers, "Harvest logs", f -> String.valueOf(f.harvests.size()));
        UI.col(farmers, "Total harvested (kg)", f -> Fmt.kg(f.totalHarvest()));
        UI.col(logs, "Date", h -> h.date.toString());
        UI.col(logs, "Crop", h -> h.cropType);
        UI.col(logs, "Quantity (kg)", h -> Fmt.kg(h.quantity));
        logs.setPrefHeight(200);
        farmers.getSelectionModel().selectedItemProperty().addListener((o, a, f) -> {
            if (f == null) logs.getItems().clear(); else UI.fill(logs, f.harvests);
        });
        VBox right = new VBox(12, UI.card(UI.cardTitle("Farmer registry"), farmers),
                UI.card(UI.cardTitle("Harvest activity of selected farmer"), logs));
        VBox.setVgrow(right.getChildren().get(0), javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(UI.split(left, right, 330));
    }

    @Override public String title() { return "2  Farmer Tracking"; }
    @Override public Feature feature() { return Feature.FARMERS; }

    private void register() { sys.registerFarmer(id.getText(), name.getText(), contact.getText()); id.clear(); name.clear(); contact.clear(); refresh(); }

    private void record() {
        Farmer f = farmers.getSelectionModel().getSelectedItem();
        if (f == null) throw new AgriException("Select a farmer in the registry first.");
        sys.recordHarvest(f.id, crop.getEditor().getText(), UI.num(qty, "Quantity"), date.getValue());
        qty.clear(); int idx = farmers.getSelectionModel().getSelectedIndex(); refresh(); farmers.getSelectionModel().select(idx);
    }

    @Override public void refresh() {
        UI.fill(farmers, sys.farmers);
        List<String> names = new ArrayList<>();
        for (Crop c : sys.crops) if (!names.contains(c.name)) names.add(c.name);
        UI.setItems(crop, names);
    }
}
