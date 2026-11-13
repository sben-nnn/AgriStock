package com.agristock.ui;

import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class DashboardView extends View {
    private final FlowPane kpis = new FlowPane(14, 14);
    private final ListView<String> alerts = new ListView<>();

    public DashboardView(Ctx ctx) {
        super(ctx);
        header("Dashboard", "Live overview of stock, alerts and pending work.");
        alerts.getStyleClass().add("alert-list");
        alerts.setPlaceholder(new Label("No alerts - everything looks healthy."));
        VBox alertCard = UI.card(UI.cardTitle("Alerts"), alerts);
        VBox.setVgrow(alerts, Priority.ALWAYS);
        VBox.setVgrow(alertCard, Priority.ALWAYS);
        getChildren().addAll(kpis, alertCard);
    }

    @Override public String title() { return "Dashboard"; }

    private VBox kpi(String title, String value, String color) {
        Label v = new Label(value); v.getStyleClass().add("kpi-value"); v.setStyle("-fx-text-fill:" + color + ";");
        Label t = new Label(title); t.getStyleClass().add("kpi-title");
        VBox b = UI.card(v, t);
        b.setPrefWidth(235);
        return b;
    }

    @Override public void refresh() {
        double available = 0, value = 0; int low = 0, near = 0, expired = 0, aging = 0;
        List<String> list = new ArrayList<>();
        for (StockItem s : sys.traverseInventory()) {
            available += Math.max(0, s.available()); value += s.remaining * s.unitValue;
            if (sys.flagExpired(s)) { expired++; list.add("EXPIRED   " + s.itemId + " (" + s.crop + ") expired on " + s.expiryDate + " - " + Fmt.kg(s.remaining) + " kg to purge"); }
            if (sys.flagNearExpiry(s)) { near++; list.add("NEAR-EXPIRY   " + s.itemId + " (" + s.crop + ") expires " + s.expiryDate + " - use first (FEFO)"); }
            if (sys.flagLowStock(s)) { low++; list.add("LOW STOCK   " + s.itemId + " (" + s.crop + ") only " + Fmt.kg(s.available()) + " kg available"); }
            if (sys.flagAgingStock(s)) { aging++; list.add("AGING   " + s.itemId + " (" + s.crop + ") harvested " + s.harvestDate); }
        }
        int pendingAdj = 0;
        for (Spoilage sp : sys.spoilage) if (sp.status.equals("PENDING")) pendingAdj++;
        if (pendingAdj > 0) list.add("APPROVAL   " + pendingAdj + " spoilage adjustment(s) waiting for approval");
        for (Delivery d : sys.deliveries)
            if (!d.status.equals("RECEIVED") && d.deadline.isBefore(sys.today())) list.add("LATE DELIVERY   " + d.id + " to " + d.company + " was due " + d.deadline);
        if (sys.inspectionQueue.size() > 0) list.add("INSPECTION   " + sys.inspectionQueue.size() + " batch(es) awaiting inspection");

        kpis.getChildren().setAll(
                kpi("Stock items", String.valueOf(sys.stock.size()), "#1b5e20"),
                kpi("Available stock (kg)", Fmt.kg(available), "#2e7d32"),
                kpi("Inventory value", Fmt.peso(value), "#00695c"),
                kpi("Awaiting inspection", String.valueOf(sys.inspectionQueue.size()), "#f9a825"),
                kpi("Pending sales", String.valueOf(sys.pendingSales.size()), "#ef6c00"),
                kpi("Scheduled deliveries", String.valueOf(sys.deliveryQueue.size()), "#1565c0"),
                kpi("Low-stock items", String.valueOf(low), "#ef6c00"),
                kpi("Near-expiry items", String.valueOf(near), "#f9a825"),
                kpi("Expired items", String.valueOf(expired), "#c62828"),
                kpi("Aging items", String.valueOf(aging), "#6d4c41"));
        alerts.setItems(FXCollections.observableArrayList(list));
    }
}
