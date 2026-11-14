package com.agristock.modules.Module12;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import com.agristock.service.ReportService;
import com.agristock.ui.Ctx;
import com.agristock.ui.View;
import com.agristock.ui.UI;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** MODULE 12: Forecasting (array + moving average). */
public class ForecastView extends View {
    private final TableView<YieldRecord> table = UI.table();
    private final ComboBox<String> crop = new ComboBox<>();
    private final TextField season, yield, demand, window, ratio, seedCost;
    private final TextArea out = new TextArea();
    private final LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), new NumberAxis());

    public ForecastView(Ctx ctx) {
        super(ctx);
        header("12 - Forecasting", "Historical yields are kept in an array; a moving average over the latest seasons smooths variance and projects seed requirements.");
        crop.setEditable(true);
        UI.Form h = new UI.Form();
        h.add("Crop", crop);
        season = h.text("Season", "e.g. 2026 Wet");
        yield = h.text("Yield (kg)");
        demand = h.text("Demand (kg)", "optional");
        h.wide(UI.btn("Add history record", "secondary", this::addHistory));
        UI.Form f = new UI.Form();
        window = f.text("Window (seasons)"); window.setText("3");
        ratio = f.text("Seed ratio (%)"); ratio.setText("12");
        seedCost = f.text("Seed cost / kg", "\u20B1"); seedCost.setText("80");
        f.wide(UI.btn("Forecast seed need", "primary", this::forecast));
        out.setEditable(false); out.setPrefRowCount(8); out.getStyleClass().add("mono");
        f.wide(out);
        chart.setAnimated(false); chart.setPrefHeight(230); chart.setCreateSymbols(true);
        UI.col(table, "Crop", y -> y.crop); UI.col(table, "Season", y -> y.season);
        UI.col(table, "Yield (kg)", y -> Fmt.kg(y.yield)); UI.col(table, "Demand (kg)", y -> Fmt.kg(y.demand));
        crop.valueProperty().addListener((o, a, v) -> showChart());
        VBox right = new VBox(12, UI.card(UI.cardTitle("Historical data (array)"), table), UI.card(UI.cardTitle("Yield vs. demand trend"), chart));
        VBox.setVgrow(right.getChildren().get(0), javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(UI.split(new VBox(14, UI.card(UI.cardTitle("Historical yields"), h.grid), UI.card(UI.cardTitle("Forecast"), f.grid)), right, 340));
    }

    @Override public String title() { return "12  Forecasting"; }
    @Override public Feature feature() { return Feature.FORECAST; }

    private String cropName() { String c = crop.getEditor().getText(); return c == null ? "" : c.trim(); }

    private void addHistory() {
        double d = demand.getText().trim().isEmpty() ? 0 : UI.num(demand, "Demand");
        rep.addYieldRecord(cropName(), season.getText(), UI.num(yield, "Yield"), d);
        season.clear(); yield.clear(); demand.clear(); refresh(); showChart();
    }

    private void forecast() {
        String c = cropName();
        if (c.isEmpty()) throw new AgriException("Choose a crop.");
        double[] y = rep.yieldsFor(c, false), dm = rep.yieldsFor(c, true);
        int w = (int) UI.num(window, "Window");
        double r = UI.num(ratio, "Seed ratio") / 100.0, cost = UI.num(seedCost, "Seed cost");
        double avg = ReportService.movingAverage(y, w), seed = ReportService.forecastSeedNeed(y, w, r);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Crop: %s | seasons on file: %d%n", c, y.length));
        sb.append(String.format("Moving average yield (%d-season window): %,.2f kg%n", w, avg));
        sb.append(String.format("Projected seed need (%.0f%%): %,.2f kg%n", r * 100, seed));
        sb.append(String.format("Seasonal seed budget: %s%n", Fmt.peso(seed * cost)));
        boolean hasDemand = false; for (double v : dm) if (v > 0) hasDemand = true;
        if (hasDemand && dm.length >= w) sb.append(String.format("Moving average demand: %,.2f kg%n", ReportService.movingAverage(dm, w)));
        out.setText(sb.toString());
    }

    private void showChart() {
        chart.getData().clear();
        String c = cropName();
        if (c.isEmpty()) return;
        XYChart.Series<String, Number> ys = new XYChart.Series<>(), ds = new XYChart.Series<>();
        ys.setName("Yield"); ds.setName("Demand");
        for (YieldRecord y : sys.yields) if (y.crop.equalsIgnoreCase(c)) { ys.getData().add(new XYChart.Data<>(y.season, y.yield)); ds.getData().add(new XYChart.Data<>(y.season, y.demand)); }
        chart.getData().add(ys); chart.getData().add(ds);
    }

    @Override public void refresh() {
        UI.fill(table, sys.yields);
        List<String> names = new ArrayList<>();
        for (Crop c : sys.crops) if (!names.contains(c.name)) names.add(c.name);
        for (YieldRecord y : sys.yields) if (!names.contains(y.crop)) names.add(y.crop);
        UI.setItems(crop, names);
    }
}
