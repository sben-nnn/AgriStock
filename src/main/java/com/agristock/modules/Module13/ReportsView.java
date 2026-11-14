package com.agristock.modules.Module13;

import com.agristock.ds.DynamicArray;
import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import com.agristock.ui.Ctx;
import com.agristock.ui.View;
import com.agristock.ui.UI;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** MODULE 13: Reports & Analytics (array, merge sort, binary search on date ranges). */
public class ReportsView extends View {
    private final TableView<ReportEntry> table = UI.table();
    private final ComboBox<String> type = new ComboBox<>(), crop = new ComboBox<>(), farm = new ComboBox<>();
    private final TextField batch = new TextField();
    private final DatePicker from = new DatePicker(), to = new DatePicker();
    private final Label totals = new Label();
    private final TextArea analytics = new TextArea();

    public ReportsView(Ctx ctx) {
        super(ctx);
        header("13 - Reports & Analytics", "Report data is copied to an array, merge-sorted by date, and binary search finds the start and end of the chosen date range.");
        type.getItems().addAll(Arrays.asList("Harvest", "Inventory", "Sales", "Dispatch", "Loss")); type.setValue("Harvest");
        batch.setPromptText("Batch ID"); batch.setPrefWidth(110);
        from.setPromptText("From"); to.setPromptText("To"); from.setPrefWidth(120); to.setPrefWidth(120);
        HBox filters = UI.row(new Label("Report"), type, new Label("Crop"), crop, new Label("Farm"), farm, batch, from, to, UI.btn("Generate", "primary", this::generate));
        totals.getStyleClass().add("card-title");
        UI.col(table, "Type", e -> e.type); UI.col(table, "Batch", e -> e.ref); UI.col(table, "Crop", e -> e.crop); UI.col(table, "Farm", e -> e.farm);
        UI.col(table, "Date", e -> e.date.toString()); UI.col(table, "Quantity (kg)", e -> Fmt.kg(e.qty)); UI.col(table, "Value", e -> Fmt.peso(e.value));
        table.setPrefHeight(260);
        analytics.setEditable(false); analytics.getStyleClass().add("mono"); analytics.setPrefRowCount(10);
        HBox ab = UI.row(UI.btn("Estimated revenue", "secondary", () -> analytics.setText(rep.computeEstimatedRevenue())),
                UI.btn("High-loss crops", "secondary", () -> analytics.setText(rep.identifyHighLossCrops())),
                UI.btn("Slow-moving stock", "secondary", () -> analytics.setText(rep.identifySlowMoving())),
                UI.btn("Seasonal trends", "secondary", () -> analytics.setText(rep.analyzeSeasonalTrends())));
        getChildren().addAll(UI.card(UI.cardTitle("Report builder"), filters, table, totals),
                UI.card(UI.cardTitle("Analytics"), ab, analytics));
    }

    @Override public String title() { return "13  Reports & Analytics"; }
    @Override public Feature feature() { return Feature.REPORTS; }

    private void generate() {
        DynamicArray<ReportEntry> r = rep.generateReport(type.getValue(), crop.getValue(), farm.getValue(), batch.getText(), from.getValue(), to.getValue());
        UI.fill(table, r);
        double q = 0, v = 0; for (ReportEntry e : r) { q += e.qty; v += e.value; }
        totals.setText(r.size() + " entries | total quantity " + Fmt.kg(q) + " kg | total value " + Fmt.peso(v));
    }

    @Override public void refresh() {
        List<String> c = new ArrayList<>(); c.add("All"); for (Crop x : sys.crops) if (!c.contains(x.name)) c.add(x.name);
        List<String> f = new ArrayList<>(); f.add("All"); for (Farm x : sys.farms) f.add(x.name);
        UI.setItems(crop, c); UI.setItems(farm, f);
        if (crop.getValue() == null) crop.setValue("All");
        if (farm.getValue() == null) farm.setValue("All");
    }
}
