package com.agristock.ui;

import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** MODULE 8: Expiry & Purge (min-heap, FEFO extract-min). */
public class ExpiryView extends View {
    private final TableView<StockItem> table = UI.table();
    private final Label root = new Label();
    private final ComboBox<String> crop = new ComboBox<>();
    private final TextField qty = new TextField();
    private final TextArea log = new TextArea();

    public ExpiryView(Ctx ctx) {
        super(ctx);
        header("8 - Expiry & Purge", "A min-heap keeps the earliest-expiring batch at the root. FEFO dispatch repeatedly extracts the minimum; purging removes expired stock.");
        root.getStyleClass().add("card-title"); root.setWrapText(true);
        qty.setPromptText("Quantity (kg)");
        crop.setMaxWidth(Double.MAX_VALUE);
        log.setEditable(false); log.setPrefRowCount(8); log.getStyleClass().add("mono");
        UI.Form f = new UI.Form();
        f.wide(root);
        f.wide(UI.btn("Check & purge expired stock", "danger", this::purge));
        f.wide(new Separator());
        f.add("Crop", crop);
        f.add("Quantity", qty);
        f.wide(UI.btn("Dispatch using FEFO", "primary", this::dispatch));
        f.wide(UI.hint("Expired stock is skipped. Reserved stock is never taken."));
        f.wide(log);

        UI.col(table, "FEFO order", s -> String.valueOf(rank(s)));
        UI.col(table, "Batch", s -> s.itemId);
        UI.col(table, "Crop", s -> s.crop);
        UI.col(table, "Expiry", s -> s.expiryDate.toString());
        UI.col(table, "Days left", s -> String.valueOf(ChronoUnit.DAYS.between(sys.today(), s.expiryDate)));
        UI.col(table, "Remaining (kg)", s -> Fmt.kg(s.remaining));
        UI.col(table, "Available (kg)", s -> Fmt.kg(s.available()));
        UI.col(table, "Status", s -> sys.flagExpired(s) ? "EXPIRED" : sys.flagNearExpiry(s) ? "Near-expiry" : "Fresh");
        getChildren().add(UI.split(UI.card(UI.cardTitle("FEFO control"), f.grid), UI.card(UI.cardTitle("Batches in expiry order (heap snapshot)"), table), 340));
    }

    private int rank(StockItem s) { int i = 1; for (StockItem x : sys.expiryHeap.sortedSnapshot()) { if (x == s) return i; i++; } return 0; }

    @Override public String title() { return "8  Expiry & Purge"; }
    @Override public Feature feature() { return Feature.EXPIRY; }

    private void purge() {
        List<StockItem> p = sys.checkAndPurgeExpired();
        if (p.isEmpty()) UI.info("Expiry check", "No expired stock found.");
        else {
            StringBuilder sb = new StringBuilder("Purged expired batches:\n");
            for (StockItem s : p) sb.append(" - ").append(s.itemId).append(" (").append(s.crop).append("), expired ").append(s.expiryDate).append(", ").append(Fmt.kg(s.damaged)).append(" kg total damaged\n");
            log.setText(sb.toString());
        }
        refresh();
    }

    private void dispatch() {
        List<String> lines = sys.dispatchFEFO(crop.getValue(), UI.num(qty, "Quantity"));
        StringBuilder sb = new StringBuilder("FEFO dispatch - earliest expiry first:\n");
        for (String l : lines) sb.append(" > ").append(l).append("\n");
        log.setText(sb.toString()); qty.clear(); refresh();
    }

    @Override public void refresh() {
        var snap = sys.expiryHeap.sortedSnapshot();
        List<StockItem> live = new ArrayList<>();
        for (StockItem s : snap) if (s.remaining > 0) live.add(s);
        UI.fill(table, live);
        StockItem top = live.isEmpty() ? null : live.get(0);
        root.setText(top == null ? "Heap root: no perishable stock." : "Heap root (expires first): " + top.itemId + " - " + top.crop + ", " + top.expiryDate);
        List<String> crops = new ArrayList<>(); crops.add("Any crop");
        for (StockItem s : sys.traverseInventory()) if (!crops.contains(s.crop)) crops.add(s.crop);
        UI.setItems(crop, crops);
        if (crop.getValue() == null) crop.setValue("Any crop");
    }
}
