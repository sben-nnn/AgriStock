package com.agristock.ui;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** MODULE 8: Expiry & Purge (min-heap, FEFO / extract-min). */
public class ExpiryView extends View {
    private final TableView<StockItem> table = UI.table();
    private final ComboBox<String> cropFilter = new ComboBox<>();
    private final TextField fefoQty;

    public ExpiryView(Ctx ctx) {
        super(ctx);
        header("8 - Expiry & Purge", "FEFO (first-expired-first-out): purge expired stock from the min-heap, or dispatch stock starting from earliest expiry.");
        UI.Form f = new UI.Form();
        cropFilter.getItems().addAll("Any crop");
        for (StockItem s : sys.traverseInventory())
            if (!cropFilter.getItems().contains(s.crop)) cropFilter.getItems().add(s.crop);
        cropFilter.setValue("Any crop");
        f.add("Crop Filter", cropFilter);
        fefoQty = f.text("FEFO Dispatch Qty (kg)");
        f.wide(UI.btn("Check & Purge Expired", "primary", this::purge));
        f.wide(UI.btn("FEFO Dispatch", "secondary", this::fefoDispatch));
        UI.col(table, "Item ID", s -> s.itemId); UI.col(table, "Crop", s -> s.crop);
        UI.col(table, "Expiry Date", s -> s.expiryDate.toString());
        UI.col(table, "Remaining", s -> Fmt.kg(s.remaining)); UI.col(table, "Available", s -> Fmt.kg(s.available()));
        UI.col(table, "Flags", s -> sys.flagsOf(s));
        getChildren().add(UI.split(new VBox(14, UI.card(UI.cardTitle("Expiry Actions"), f.grid)),
                UI.card(UI.cardTitle("Expiring Stock (min-heap order)"), table), 340));
    }

    @Override public String title() { return "8  Expiry"; }
    @Override public Feature feature() { return Feature.EXPIRY; }

    private void purge() {
        List<StockItem> purged = sys.checkAndPurgeExpired();
        refresh();
        if (purged.isEmpty()) UI.info("Purge", "No expired stock found.");
        else {
            StringBuilder sb = new StringBuilder("Purged expired stock:\n");
            for (StockItem s : purged)
                sb.append("  ").append(s.itemId).append(" (").append(s.crop).append("): ").append(Fmt.kg(s.remaining)).append(" kg lost\n");
            UI.textDialog("Expired Stock Purged", sb.toString());
        }
    }

    private void fefoDispatch() {
        String crop = cropFilter.getValue();
        double q = UI.num(fefoQty, "Quantity");
        List<String> log = sys.dispatchFEFO(crop.equals("Any crop") ? null : crop, q);
        fefoQty.clear(); refresh();
        StringBuilder sb = new StringBuilder("FEFO Dispatch (" + q + " kg):\n");
        for (String line : log) sb.append("  ").append(line).append("\n");
        UI.textDialog("FEFO Dispatch Result", sb.toString());
    }

    @Override public void refresh() {
        List<StockItem> list = new ArrayList<>();
        for (StockItem s : sys.traverseInventory())
            if (s.remaining > 0) list.add(s);
        list.sort((a, b) -> a.expiryDate.compareTo(b.expiryDate));
        UI.fill(table, list);
        cropFilter.setValue(cropFilter.getValue() == null ? "Any crop" : cropFilter.getValue());
    }
}
