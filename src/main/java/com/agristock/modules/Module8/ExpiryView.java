package com.agristock.modules.Module8;

import com.agristock.model.AgriException;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.StockItem;
import com.agristock.ui.View;
import com.agristock.ui.UI;
import com.agristock.ui.Ctx;
import com.agristock.model.Models.Spoilage;
import com.agristock.service.AgriStockSystem;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// MODULE 8: Expiry & Purge (Min-Heap, FEFO / extract-min)
public class ExpiryView extends View {
    private final TableView<StockItem> expiryTable = UI.table();
    private final TableView<StockItem> expiredTable = UI.table();
    private final ExpiryService svc;
    private final ComboBox<String> cropSelect = new ComboBox<>();
    private TextField fefoQty;

    public ExpiryView(Ctx ctx) { super(ctx); svc = new ExpiryService(sys); build(); }

    private void build() {
        header("8 - Expiry & Purge",
                "Always use the stock that expires soonest first. Expired stock can be safely removed as a loss.");

        // ---------- Left panel: FEFO dispatch ----------
        UI.Form fefoF = new UI.Form();
        fefoF.add("Crop", cropSelect);
        fefoQty = fefoF.text("Qty (kg)", "e.g. 100");
        fefoF.wide(UI.btn("Dispatch FEFO", "primary", this::dispatchFefo));
        VBox fefoCard = UI.card(UI.cardTitle("FEFO Dispatch"), fefoF.grid);
        fefoCard.setPrefWidth(380);

        // ---------- Left panel: expiry actions ----------
        VBox actions = new VBox(10,
                UI.btn("Check & Purge Expired Stock", "danger", this::purgeExpired),
                UI.btn("Rebuild Expiry Heap", "secondary", () -> { svc.rebuildHeap(); refreshAll(); }),
                UI.btn("Refresh", "ghost", this::refreshAll)
        );
        actions.setMaxWidth(Double.MAX_VALUE);
        for (Object o : actions.getChildren()) {
            if (o instanceof Button) ((Button) o).setMaxWidth(Double.MAX_VALUE);
        }
        VBox actionCard = UI.card(UI.cardTitle("Expiry Actions"), actions);
        actionCard.setPrefWidth(380);

        VBox left = new VBox(14, fefoCard, actionCard);
        left.setPrefWidth(400);

        // ---------- Right panel: stock by expiry + expired stock ----------
        UI.col(expiryTable, "Item ID", s -> s.itemId);
        UI.col(expiryTable, "Crop", s -> s.crop);
        UI.col(expiryTable, "Expiry", s -> s.expiryDate == null ? "-" : s.expiryDate.toString());
        UI.col(expiryTable, "Remaining (kg)", s -> String.format("%,.2f", s.remaining));
        UI.col(expiryTable, "Status", s -> {
            if (s.totalStock() <= 0) return "Empty";
            if (svc.isExpired(s)) return "EXPIRED";
            if (svc.isNearExpiry(s)) return "Near-expiry";
            return "Fresh";
        });

        VBox expiryCard = UI.card(UI.cardTitle("Stock by Expiry (earliest first)"), expiryTable);
        VBox.setVgrow(expiryTable, Priority.ALWAYS);
        VBox.setVgrow(expiryCard, Priority.ALWAYS);

        UI.col(expiredTable, "Item ID", s -> s.itemId);
        UI.col(expiredTable, "Crop", s -> s.crop);
        UI.col(expiredTable, "Expiry", s -> s.expiryDate == null ? "-" : s.expiryDate.toString());
        UI.col(expiredTable, "Remaining (kg)", s -> String.format("%,.2f", s.remaining));
        UI.col(expiredTable, "Days Overdue", s -> {
            if (s.expiryDate == null) return "-";
            return String.valueOf(java.time.temporal.ChronoUnit.DAYS.between(s.expiryDate, svc.today()));
        });

        VBox expiredCard = UI.card(UI.cardTitle("Expired Stock"), expiredTable);
        VBox.setVgrow(expiredTable, Priority.ALWAYS);
        VBox.setVgrow(expiredCard, Priority.ALWAYS);

        VBox right = new VBox(14, expiryCard, expiredCard);
        VBox.setVgrow(right, Priority.ALWAYS);

        getChildren().addAll(UI.split(left, right, 400));
    }

    @Override public String title() { return "8  Expiry"; }
    @Override public Feature feature() { return Feature.EXPIRY; }

    private void dispatchFefo() {
        String crop = cropSelect.getValue();
        if (crop == null || crop.equals("Any crop")) crop = null;
        double qty = UI.num(fefoQty, "Qty");
        List<String> log = svc.dispatchFEFO(crop, qty);
        StringBuilder sb = new StringBuilder("FEFO dispatch completed:\n");
        for (String line : log) sb.append("• ").append(line).append("\n");
        UI.textDialog("FEFO Dispatch Result", sb.toString());
        fefoQty.clear();
        refreshAll();
    }

    private void purgeExpired() {
        if (!UI.confirm("Permanently purge all expired stock? This cannot be undone.")) return;
        List<Spoilage> purged = svc.purgeExpired();
        StringBuilder sb = new StringBuilder("Purged expired stock:\n");
        if (purged.isEmpty()) sb.append("(none)");
        for (Spoilage sp : purged) {
            StockItem s = svc.find(sp.itemId);
            sb.append("• ").append(sp.itemId)
                    .append(s == null ? "" : " (" + s.crop + ")")
                    .append(" - ").append(String.format("%,.2f", sp.qty)).append(" kg\n");
        }
        UI.textDialog("Purge Result", sb.toString());
        refreshAll();
    }

    @Override public void refresh() {
        UI.setItems(cropSelect, svc.cropNames());
        if (cropSelect.getValue() == null) cropSelect.setValue("Any crop");

        UI.fill(expiryTable, svc.byExpiry());
        UI.fill(expiredTable, svc.expired());
    }
}
