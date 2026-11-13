package com.agristock.ui;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** MODULE 9: Sales & Stock Release (FIFO queue of pending transactions). */
public class SalesView extends View {
    private final TableView<Sale> pending = UI.table(), records = UI.table();
    private final ComboBox<String> item = new ComboBox<>();
    private final TextField buyer, qty, price, ref;
    private final Label avail = UI.hint("");

    public SalesView(Ctx ctx) {
        super(ctx);
        header("9 - Sales & Stock Release", "New orders reserve stock and wait in a FIFO queue. Stock is reduced only when an order is confirmed.");
        UI.Form f = new UI.Form();
        buyer = f.text("Buyer");
        f.add("Batch", item);
        f.wide(avail);
        qty = f.text("Quantity (kg)"); price = f.text("Unit price", "\u20B1 per kg"); ref = f.text("Reference no.", "blank = auto");
        f.wide(UI.btn("Create sale (check availability)", "primary", this::create));
        item.valueProperty().addListener((o, a, v) -> {
            StockItem s = v == null ? null : sys.searchStock(UI.idOf(item));
            if (s != null) { avail.setText("Available " + Fmt.kg(s.available()) + " kg, expires " + s.expiryDate); if (price.getText().isEmpty()) price.setText(String.valueOf(s.unitValue)); }
        });
        UI.col(pending, "Queue #", s -> String.valueOf(pos(s)));
        UI.col(pending, "Ref", s -> s.ref); UI.col(pending, "Buyer", s -> s.buyer); UI.col(pending, "Batch", s -> s.itemId);
        UI.col(pending, "Qty (kg)", s -> Fmt.kg(s.qty)); UI.col(pending, "Total", s -> Fmt.peso(s.total()));
        UI.col(records, "Ref", s -> s.ref); UI.col(records, "Date", s -> s.date.toString()); UI.col(records, "Buyer", s -> s.buyer);
        UI.col(records, "Batch", s -> s.itemId); UI.col(records, "Qty (kg)", s -> Fmt.kg(s.qty)); UI.col(records, "Price/kg", s -> Fmt.peso(s.unitPrice));
        UI.col(records, "Total", s -> Fmt.peso(s.total())); UI.col(records, "Status", s -> s.status);
        VBox pq = new VBox(10, UI.row(UI.btn("Confirm next (FIFO)", "primary", () -> { Sale s = sys.confirmSale(); refresh(); UI.textDialog("Receipt " + s.ref, sys.generateReceipt(s)); }),
                UI.btn("Process all pending", "secondary", () -> { int n = sys.processPendingSales(); refresh(); UI.info("Sales", n + " order(s) confirmed in FIFO order."); }),
                UI.btn("Cancel selected", "danger", () -> { Sale s = pending.getSelectionModel().getSelectedItem(); if (s == null) throw new AgriException("Select a pending order."); sys.cancelSale(s); refresh(); })), pending);
        VBox rc = new VBox(10, UI.row(UI.btn("Generate receipt", "primary", () -> {
            Sale s = records.getSelectionModel().getSelectedItem(); if (s == null) throw new AgriException("Select a sale record.");
            UI.textDialog("Receipt " + s.ref, sys.generateReceipt(s)); })), records);
        VBox.setVgrow(pending, javafx.scene.layout.Priority.ALWAYS); VBox.setVgrow(records, javafx.scene.layout.Priority.ALWAYS);
        TabPane tabs = new TabPane(new Tab("Pending queue (FIFO)", pq), new Tab("Sales records", rc));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabs, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(UI.split(UI.card(UI.cardTitle("New sales order"), f.grid), UI.card(tabs), 340));
    }

    private int pos(Sale t) { int i = 1; for (Sale s : sys.pendingSales) { if (s == t) return i; i++; } return 0; }

    @Override public String title() { return "9  Sales & Release"; }
    @Override public Feature feature() { return Feature.SALES; }

    private void create() {
        Sale s = sys.createSale(buyer.getText(), UI.idOf(item), UI.num(qty, "Quantity"), UI.num(price, "Unit price"), ref.getText());
        buyer.clear(); qty.clear(); price.clear(); ref.clear(); refresh();
        UI.info("Order queued", "Order " + s.ref + " is pending. " + Fmt.kg(s.qty) + " kg are reserved until it is confirmed or cancelled.");
    }

    @Override public void refresh() {
        UI.fill(pending, sys.pendingSales); UI.fill(records, sys.sales);
        List<String> l = new ArrayList<>();
        for (StockItem s : sys.traverseInventory()) if (s.available() > 0 && !sys.flagExpired(s)) l.add(s.itemId + " | " + s.crop + " (" + Fmt.kg(s.available()) + " kg)");
        UI.setItems(item, l);
    }
}
