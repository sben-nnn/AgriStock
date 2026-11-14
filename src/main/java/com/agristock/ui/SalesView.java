package com.agristock.ui;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** MODULE 9: Sales & Stock Release (queue, FIFO processing). */
public class SalesView extends View {
    private final TableView<Sale> pendingTable = UI.table(), salesTable = UI.table();
    private final ComboBox<String> item = new ComboBox<>();
    private final TextField buyer, qty, price, ref;
    private final Label pendingLabel = new Label();

    public SalesView(Ctx ctx) {
        super(ctx);
        header("9 - Sales & Stock Release", "Create sales orders (queued FIFO). Confirming the front order reduces stock. Releases reservation on cancel.");
        UI.Form f = new UI.Form();
        f.add("Batch", item);
        buyer = f.text("Buyer");
        qty = f.text("Qty (kg)");
        price = f.text("Unit Price (/kg)");
        ref = f.text("Ref No (optional)");
        f.wide(UI.btn("Create Sale (queue)", "primary", this::create));
        f.wide(UI.btn("Confirm Next (FIFO)", "secondary", this::confirm));
        f.wide(UI.btn("Cancel Selected", "danger", this::cancel));
        pendingLabel.getStyleClass().add("card-title");
        f.wide(pendingLabel);
        UI.col(pendingTable, "Ref", s -> s.ref); UI.col(pendingTable, "Buyer", s -> s.buyer);
        UI.col(pendingTable, "Item", s -> s.itemId); UI.col(pendingTable, "Qty", s -> Fmt.kg(s.qty));
        UI.col(pendingTable, "Price", s -> Fmt.peso(s.unitPrice)); UI.col(pendingTable, "Date", s -> s.date.toString());
        UI.col(salesTable, "Ref", s -> s.ref); UI.col(salesTable, "Buyer", s -> s.buyer);
        UI.col(salesTable, "Item", s -> s.itemId); UI.col(salesTable, "Qty", s -> Fmt.kg(s.qty));
        UI.col(salesTable, "Status", s -> s.status); UI.col(salesTable, "Date", s -> s.date.toString());
        getChildren().add(UI.split(new VBox(14, UI.card(UI.cardTitle("New Sale"), f.grid), UI.card(UI.cardTitle("Pending Queue"), pendingLabel, pendingTable)),
                new VBox(10, UI.card(UI.cardTitle("Confirmed Sales"), salesTable)), 340));
    }

    @Override public String title() { return "9  Sales"; }
    @Override public Feature feature() { return Feature.SALES; }

    private void create() {
        Sale s = sys.createSale(buyer.getText(), UI.idOf(item), UI.num(qty, "Qty"),
                UI.num(price, "Price"), ref.getText());
        buyer.clear(); qty.clear(); price.clear(); ref.clear(); refresh();
        UI.info("Sale Created", s.ref + " queued for " + s.buyer + " (" + s.qty + " kg of " + s.itemId + ")");
    }

    private void confirm() {
        sys.confirmSale();
        refresh();
        UI.info("Confirmed", "Next pending sale confirmed - stock reduced.");
    }

    private void cancel() {
        Sale s = pendingTable.getSelectionModel().getSelectedItem();
        if (s == null) throw new AgriException("Select a pending sale to cancel.");
        sys.cancelSale(s);
        refresh();
        UI.info("Cancelled", "Sale " + s.ref + " cancelled - reservation released.");
    }

    @Override public void refresh() {
        List<Sale> pending = new ArrayList<>();
        for (Sale s : sys.pendingSales) pending.add(s);
        UI.fill(pendingTable, pending);
        pendingLabel.setText("Pending Queue (" + pending.size() + " order(s)) - next to confirm:");
        List<Sale> confirmed = new ArrayList<>();
        for (Sale s : sys.sales) if (s.status.equals("CONFIRMED")) confirmed.add(s);
        UI.fill(salesTable, confirmed);
        List<String> items = new ArrayList<>();
        for (StockItem s : sys.traverseInventory())
            if (s.available() > 0) items.add(s.itemId + " | " + s.crop + " (" + Fmt.kg(s.available()) + " kg available)");
        UI.setItems(item, items);
    }
}
