package com.agristock.ui;

import com.agristock.ds.DynamicArray;
import com.agristock.model.Models.Delivery;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.Spoilage;
import com.agristock.model.Models.StockItem;
import com.agristock.service.Fmt;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

// Dashboard - shows quick summary stats
public class DashboardView extends View {
    public DashboardView(Ctx ctx) { super(ctx); }

    @Override public String title() { return "0  Dashboard"; }
    @Override public Feature feature() { return null; }   // visible to every role

    @Override public void refresh() {
        getChildren().clear();

        String who = sys.session == null ? "" : "Welcome, " + sys.session.fullName + "  \u2022  ";
        header("Dashboard", who + sys.today() + "   |   A quick snapshot of your stock, items needing attention, and upcoming deliveries");

        double totalKg = 0, totalValue = 0;
        int expired = 0, nearExpiry = 0, low = 0, aging = 0;
        List<StockItem> attention = new ArrayList<>();
        for (StockItem s : sys.traverseInventory()) {
            if (s.totalStock() > 0) {
                totalKg += s.totalStock();
                totalValue += s.totalStock() * s.unitValue;
            }
            if (sys.flagExpired(s)) expired++;
            if (sys.flagNearExpiry(s)) nearExpiry++;
            if (sys.flagLowStock(s)) low++;
            if (sys.flagAgingStock(s)) aging++;
            if (!sys.flagsOf(s).isEmpty()) attention.add(s);
        }

        int pendingAdjustments = 0;
        for (Spoilage sp : sys.spoilage) if (sp.status.equals("PENDING")) pendingAdjustments++;

        FlowPane tiles = new FlowPane(14, 14,
                tile("Stock items", String.valueOf(sys.stock.size())),
                tile("Stock on hand (kg)", Fmt.kg(totalKg)),
                tile("Inventory value", Fmt.peso(totalValue)),
                tile("Awaiting inspection", String.valueOf(sys.inspectionQueue.size())),
                tile("Pending sales orders", String.valueOf(sys.pendingSales.size())),
                tile("Scheduled deliveries", String.valueOf(sys.deliveryQueue.size())),
                tile("Adjustments to approve", String.valueOf(pendingAdjustments)),
                tile("Expired batches", String.valueOf(expired)),
                tile("Near-expiry batches", String.valueOf(nearExpiry)),
                tile("Low-stock batches", String.valueOf(low)),
                tile("Aging batches", String.valueOf(aging)));

        TableView<StockItem> attentionTable = UI.table();
        UI.col(attentionTable, "Item ID", s -> s.itemId);
        UI.col(attentionTable, "Crop", s -> s.crop);
        UI.col(attentionTable, "Available (kg)", s -> Fmt.kg(s.available()));
        UI.col(attentionTable, "Expiry", s -> s.expiryDate == null ? "-" : s.expiryDate.toString());
        UI.col(attentionTable, "Flags", s -> sys.flagsOf(s));
        UI.fill(attentionTable, attention);
        attentionTable.setPrefHeight(220);
        VBox attentionCard = UI.card(UI.cardTitle("Stock needing attention"), attentionTable);
        VBox.setVgrow(attentionCard, Priority.ALWAYS);

        TableView<Delivery> deliveryTable = UI.table();
        UI.col(deliveryTable, "ID", d -> d.id);
        UI.col(deliveryTable, "Company", d -> d.company);
        UI.col(deliveryTable, "Qty (kg)", d -> Fmt.kg(d.qty));
        UI.col(deliveryTable, "Deadline", d -> d.deadline == null ? "-" : d.deadline.toString());
        UI.col(deliveryTable, "Destination", d -> d.destination);
        List<Delivery> next = new ArrayList<>();
        DynamicArray<Delivery> upcoming = sys.getUpcomingDeliveries();
        for (int i = 0; i < upcoming.size() && next.size() < 5; i++) next.add(upcoming.get(i));
        UI.fill(deliveryTable, next);
        deliveryTable.setPrefHeight(200);
        VBox deliveryCard = UI.card(UI.cardTitle("Next deliveries (earliest deadline first)"), deliveryTable);
        VBox.setVgrow(deliveryCard, Priority.ALWAYS);

        getChildren().addAll(tiles, attentionCard, deliveryCard);
    }

    private VBox tile(String label, String value) {
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        Label l = new Label(label);
        l.getStyleClass().add("hint");
        VBox t = UI.card(v, l);
        t.setPrefWidth(190);
        return t;
    }
}