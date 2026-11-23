package com.agristock.modules.Module9;

import com.agristock.model.AgriException;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.Sale;
import com.agristock.model.Models.StockItem;
import com.agristock.service.Fmt;
import com.agristock.ui.Ctx;
import com.agristock.ui.UI;
import com.agristock.ui.View;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

// MODULE 9: Sales & Stock Release (Queue, FIFO processing)
public class SalesView extends View {
    private final TableView<Sale> pendingTable = UI.table();
    private final TableView<Sale> salesTable = UI.table();
    private final ComboBox<String> itemSelect = new ComboBox<>();
    private TextField buyer, saleQty, unitPrice;

    public SalesView(Ctx ctx) {
        super(ctx);
        build();
        refresh();
    }

    private void build() {
        header("9 - Sales & Stock Release",
                "Create sales orders and confirm them to release stock.");

        // ---------- Left panel: create sale ----------
        UI.Form f = new UI.Form();
        buyer = f.text("Buyer", "e.g. Metro Market");
        itemSelect.setMaxWidth(Double.MAX_VALUE);
        f.add("Item / Batch", itemSelect);
        saleQty = f.text("Qty (kg)", "e.g. 100");
        unitPrice = f.text("Unit Price", "e.g. 60.00");
        f.wide(UI.btn("Create Sale Order", "primary", this::createSale));

        VBox createCard = UI.card(UI.cardTitle("Create Sale Order"), f.grid);
        createCard.setPrefWidth(380);

        // ---------- Left panel: queue actions ----------
        VBox actions = new VBox(10,
                UI.btn("Process All Pending (FIFO)", "secondary", this::processPending),
                UI.btn("Refresh", "ghost", this::refresh)
        );
        actions.setMaxWidth(Double.MAX_VALUE);
        for (Object o : actions.getChildren()) {
            if (o instanceof Button) ((Button) o).setMaxWidth(Double.MAX_VALUE);
        }

        VBox actionCard = UI.card(UI.cardTitle("Queue Actions"), actions);
        actionCard.setPrefWidth(380);

        VBox left = new VBox(14, createCard, actionCard);
        left.setPrefWidth(400);

        // ---------- Right panel: pending queue + sales records ----------
        UI.col(pendingTable, "Ref", s -> s.ref);
        UI.col(pendingTable, "Buyer", s -> s.buyer);
        UI.col(pendingTable, "Item", s -> s.itemId);
        UI.col(pendingTable, "Crop", s -> s.crop);
        UI.col(pendingTable, "Qty (kg)", s -> String.format("%,.2f", s.qty));
        UI.col(pendingTable, "Unit Price", s -> String.format("%,.2f", s.unitPrice));
        UI.col(pendingTable, "Total", s -> String.format("%,.2f", s.total()));
        UI.col(pendingTable, "Date", s -> s.date.toString());
        UI.col(pendingTable, "Status", s -> s.status);

        VBox pendingCard = UI.card(UI.cardTitle("Pending Sales Queue (FIFO)"), pendingTable);
        VBox.setVgrow(pendingTable, Priority.ALWAYS);
        VBox.setVgrow(pendingCard, Priority.ALWAYS);

        UI.col(salesTable, "Ref", s -> s.ref);
        UI.col(salesTable, "Buyer", s -> s.buyer);
        UI.col(salesTable, "Item", s -> s.itemId);
        UI.col(salesTable, "Qty (kg)", s -> String.format("%,.2f", s.qty));
        UI.col(salesTable, "Total", s -> String.format("%,.2f", s.total()));
        UI.col(salesTable, "Date", s -> s.date.toString());
        UI.col(salesTable, "Status", s -> s.status);
        UI.col(salesTable, "Delivery", s -> s.deliveryScheduled ? "Scheduled" : "-");

        VBox salesCard = UI.card(UI.cardTitle("Sales Records"), salesTable);
        VBox.setVgrow(salesTable, Priority.ALWAYS);
        VBox.setVgrow(salesCard, Priority.ALWAYS);

        VBox right = new VBox(14, pendingCard, salesCard);
        VBox.setVgrow(right, Priority.ALWAYS);

        getChildren().addAll(UI.split(left, right, 400));
    }

    @Override public String title() { return "9  Sales"; }
    @Override public Feature feature() { return Feature.SALES; }

    private void createSale() {
        try {
            String b = buyer.getText().trim();
            if (b.isEmpty()) throw new AgriException("Enter a buyer name.");
            String iid = UI.idOf(itemSelect);
            if (iid == null) throw new AgriException("Select an item / batch.");
            double qty = UI.num(saleQty, "Qty");
            double price = UI.num(unitPrice, "Unit Price");

            sys.createSale(b, iid, qty, price);
            buyer.clear();
            saleQty.clear();
            unitPrice.clear();
            refreshAll();
        } catch (AgriException ex) {
            UI.error(ex.getMessage());
        }
    }

    private void processPending() {
        if (sys.pendingSales.isEmpty()) {
            UI.info("Nothing pending", "There are no pending sales orders.");
            return;
        }
        try {
            int n = sys.processPendingSales();
            UI.info("Processed", "Confirmed " + n + " sale order(s) and released stock.");
            refreshAll();
        } catch (AgriException ex) {
            UI.error(ex.getMessage());
            refreshAll();
        }
    }

    @Override public void refresh() {
        List<String> ids = new ArrayList<>();
        for (StockItem s : sys.traverseInventory()) {
            if (s.remaining > 0.0001 && s.available() > 0.0001 && !s.expiryDate.isBefore(sys.today())) {
                ids.add(s.itemId + " | " + s.crop + " (" + Fmt.kg(s.available()) + " available)");
            }
        }
        UI.setItems(itemSelect, ids);

        List<Sale> pending = new ArrayList<>();
        for (Sale s : sys.pendingSales) pending.add(s);
        UI.fill(pendingTable, pending);

        List<Sale> records = new ArrayList<>();
        for (Sale s : sys.sales) records.add(s);
        UI.fill(salesTable, records);
    }
}