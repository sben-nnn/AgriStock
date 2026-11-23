package com.agristock.modules.Module10;

import com.agristock.model.AgriException;
import com.agristock.model.Models.Delivery;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.Sale;
import com.agristock.ui.View;
import com.agristock.ui.UI;
import com.agristock.ui.Ctx;
import com.agristock.service.AgriStockSystem;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// MODULE 10: Dispatch & Delivery Monitoring (Priority Queue, Earliest Deadline First)
public class DispatchView extends View {
    private final TableView<Delivery> upcomingTable = UI.table();
    private final TableView<Delivery> historyTable = UI.table();
    private final ComboBox<String> saleSelect = new ComboBox<>();
    private final ComboBox<String> urgencySelect = new ComboBox<>();
    private final ComboBox<String> dispatchSelect = new ComboBox<>();
    private DatePicker deadlinePicker;
    private TextField vehicle, destination, receivedQty;

    public DispatchView(Ctx ctx) { super(ctx); build(); }

    private void build() {
        header("10 - Dispatch & Delivery Monitoring",
                "Schedule deliveries by earliest deadline (EDF) and track dispatch and receipt.");

        // ---------- Left panel: schedule delivery ----------
        UI.Form schedF = new UI.Form();
        schedF.add("Sale Order", saleSelect);
        deadlinePicker = schedF.date("Deadline", LocalDate.now().plusDays(2));
        vehicle = schedF.text("Vehicle", "Truck TRK-101");
        destination = schedF.text("Destination", "Metro Market, Quezon City");
        urgencySelect.getItems().addAll("1 - High", "2 - Normal", "3 - Low");
        urgencySelect.setValue("2 - Normal");
        schedF.add("Urgency", urgencySelect);
        schedF.wide(UI.btn("Schedule Delivery", "primary", this::scheduleDelivery));
        VBox schedCard = UI.card(UI.cardTitle("Schedule Delivery"), schedF.grid);
        schedCard.setPrefWidth(380);

        // ---------- Left panel: dispatch / receipt ----------
        UI.Form dispF = new UI.Form();
        dispF.add("Delivery", dispatchSelect);
        receivedQty = dispF.text("Received Qty (kg)", "98");
        dispF.wide(UI.btn("Record Dispatch (EDF)", "secondary", this::recordDispatch));
        dispF.wide(UI.btn("Confirm Receipt", "secondary", this::confirmReceipt));
        VBox dispCard = UI.card(UI.cardTitle("Dispatch / Receipt"), dispF.grid);
        dispCard.setPrefWidth(380);

        VBox left = new VBox(14, schedCard, dispCard);
        left.setPrefWidth(400);

        // ---------- Right panel: upcoming + history ----------
        UI.col(upcomingTable, "ID", d -> d.id);
        UI.col(upcomingTable, "Sale Ref", d -> d.saleRef);
        UI.col(upcomingTable, "Company", d -> d.company);
        UI.col(upcomingTable, "Item", d -> d.itemId);
        UI.col(upcomingTable, "Qty (kg)", d -> String.format("%,.2f", d.qty));
        UI.col(upcomingTable, "Deadline", d -> d.deadline.toString());
        UI.col(upcomingTable, "Vehicle", d -> d.vehicle);
        UI.col(upcomingTable, "Destination", d -> d.destination);
        UI.col(upcomingTable, "Urgency", d -> String.valueOf(d.urgency));
        UI.col(upcomingTable, "Status", d -> d.status);

        VBox upcomingCard = UI.card(UI.cardTitle("Upcoming Deliveries (earliest deadline first)"), upcomingTable);
        VBox.setVgrow(upcomingTable, Priority.ALWAYS);
        VBox.setVgrow(upcomingCard, Priority.ALWAYS);

        UI.col(historyTable, "ID", d -> d.id);
        UI.col(historyTable, "Sale Ref", d -> d.saleRef);
        UI.col(historyTable, "Company", d -> d.company);
        UI.col(historyTable, "Qty (kg)", d -> String.format("%,.2f", d.qty));
        UI.col(historyTable, "Dispatched (kg)", d -> String.format("%,.2f", d.dispatchedQty));
        UI.col(historyTable, "Received (kg)", d -> String.format("%,.2f", d.receivedQty));
        UI.col(historyTable, "Dispatch Date", d -> d.dispatchDate == null ? "-" : d.dispatchDate.toString());
        UI.col(historyTable, "Received Date", d -> d.receivedDate == null ? "-" : d.receivedDate.toString());
        UI.col(historyTable, "Status", d -> d.status);

        VBox historyCard = UI.card(UI.cardTitle("Delivery History"), historyTable);
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        VBox.setVgrow(historyCard, Priority.ALWAYS);

        VBox right = new VBox(14, upcomingCard, historyCard);
        VBox.setVgrow(right, Priority.ALWAYS);

        getChildren().addAll(UI.split(left, right, 400));
    }

    @Override public String title() { return "10  Dispatch"; }
    @Override public Feature feature() { return Feature.DISPATCH; }

    private void scheduleDelivery() {
        String ref = saleSelect.getValue();
        if (ref == null) throw new AgriException("Select a sale order.");
        Sale sale = findSale(ref);
        if (sale == null) throw new AgriException("Sale order not found.");
        LocalDate deadline = deadlinePicker.getValue();
        if (deadline == null) throw new AgriException("Deadline is required.");
        String v = vehicle.getText();
        String dest = destination.getText();
        if (v.isEmpty() || dest.isEmpty()) throw new AgriException("Vehicle and destination are required.");
        String u = urgencySelect.getValue();
        int urgency = u == null ? 2 : Integer.parseInt(u.substring(0, 1));
        sys.scheduleDelivery(sale, deadline, v, dest, urgency);
        vehicle.clear(); destination.clear();
        refresh();
    }

    private Sale findSale(String ref) {
        for (Sale s : sys.sales) if (s.ref.equals(ref)) return s;
        return null;
    }

    private void recordDispatch() {
        if (sys.deliveryQueue.isEmpty()) {
            UI.info("Nothing scheduled", "There are no scheduled deliveries to dispatch.");
            return;
        }
        sys.recordDispatch();
        refresh();
    }

    private void confirmReceipt() {
        String id = dispatchSelect.getValue();
        if (id == null) throw new AgriException("Select a delivery.");
        Delivery d = findDelivery(id);
        if (d == null) throw new AgriException("Delivery not found.");
        if (!d.status.equals("DISPATCHED")) throw new AgriException("Select a dispatched delivery.");
        double qty = UI.num(receivedQty, "Received Qty");
        sys.confirmReceipt(d, qty);
        receivedQty.clear();
        refresh();
    }

    private Delivery findDelivery(String id) {
        for (Delivery dl : sys.deliveries) if (dl.id.equals(id)) return dl;
        return null;
    }

    @Override public void refresh() {
        // Confirmed sales that have no delivery scheduled yet.
        List<String> refs = new ArrayList<>();
        for (Sale s : sys.sales) {
            if (s.status.equals("CONFIRMED") && !s.deliveryScheduled) refs.add(s.ref);
        }
        UI.setItems(saleSelect, refs);

        // Deliveries not yet received (eligible for dispatch / receipt).
        List<String> ids = new ArrayList<>();
        for (Delivery d : sys.deliveries) {
            if (!d.status.equals("RECEIVED")) ids.add(d.id);
        }
        UI.setItems(dispatchSelect, ids);

        List<Delivery> upcoming = new ArrayList<>();
        for (Delivery d : sys.getUpcomingDeliveries()) upcoming.add(d);
        UI.fill(upcomingTable, upcoming);

        List<Delivery> history = new ArrayList<>();
        for (Delivery d : sys.deliveries) history.add(d);
        UI.fill(historyTable, history);
    }
}
