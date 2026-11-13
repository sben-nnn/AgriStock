package com.agristock.ui;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** MODULE 10: Dispatch & Delivery Monitoring (priority queue, Earliest Deadline First). */
public class DispatchView extends View {
    private final TableView<Delivery> upcoming = UI.table(), history = UI.table();
    private final ComboBox<String> sale, urgency;
    private final DatePicker deadline;
    private final TextField vehicle, destination, received;

    public DispatchView(Ctx ctx) {
        super(ctx);
        header("10 - Dispatch & Delivery Monitoring", "Scheduled deliveries sit in a priority queue ordered by deadline (earliest first, then urgency). The heap root is always dispatched next.");
        UI.Form f = new UI.Form();
        sale = f.combo("Confirmed sale", new ArrayList<>());
        deadline = f.date("Deadline", LocalDate.now().plusDays(3));
        vehicle = f.text("Vehicle ref.", "e.g. Truck TRK-101");
        destination = f.text("Destination");
        urgency = f.combo("Urgency", Arrays.asList("1 - High", "2 - Normal", "3 - Low")); urgency.setValue("2 - Normal");
        f.wide(UI.btn("Schedule delivery", "primary", this::schedule));
        UI.Form a = new UI.Form();
        a.wide(UI.btn("Dispatch next (earliest deadline)", "accent", () -> {
            Delivery d = sys.recordDispatch(); refresh();
            UI.info("Dispatched", d.id + " to " + d.company + " (" + Fmt.kg(d.dispatchedQty) + " kg) left via " + d.vehicle + "."); }));
        a.wide(new Separator());
        received = a.text("Received (kg)");
        a.wide(UI.hint("Select a dispatched delivery below, enter what the buyer received. Shortages are logged as losses."));
        a.wide(UI.btn("Confirm receipt", "primary", this::confirm));

        UI.col(upcoming, "Priority", d -> String.valueOf(pos(d)));
        UI.col(upcoming, "Delivery", d -> d.id); UI.col(upcoming, "Company", d -> d.company);
        UI.col(upcoming, "Batch", d -> d.itemId); UI.col(upcoming, "Qty (kg)", d -> Fmt.kg(d.qty));
        UI.col(upcoming, "Deadline", d -> d.deadline.toString() + (d.deadline.isBefore(sys.today()) ? "  (LATE)" : ""));
        UI.col(upcoming, "Urgency", d -> d.urgency == 1 ? "High" : d.urgency == 2 ? "Normal" : "Low");
        UI.col(upcoming, "Vehicle", d -> d.vehicle); UI.col(upcoming, "Destination", d -> d.destination);
        UI.col(history, "Delivery", d -> d.id); UI.col(history, "Company", d -> d.company); UI.col(history, "Status", d -> d.status);
        UI.col(history, "Dispatched (kg)", d -> Fmt.kg(d.dispatchedQty)); UI.col(history, "Received (kg)", d -> d.status.equals("RECEIVED") ? Fmt.kg(d.receivedQty) : "");
        UI.col(history, "Shortage (kg)", d -> d.status.equals("RECEIVED") ? Fmt.kg(d.dispatchedQty - d.receivedQty) : "");
        UI.col(history, "Dispatched on", d -> UI.d(d.dispatchDate)); UI.col(history, "Deadline", d -> d.deadline.toString());
        VBox right = new VBox(12, UI.card(UI.cardTitle("Upcoming deliveries (priority queue order)"), upcoming),
                UI.card(UI.cardTitle("Dispatched & received"), history));
        VBox.setVgrow(right.getChildren().get(0), javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(right.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(UI.split(new VBox(14, UI.card(UI.cardTitle("Schedule delivery"), f.grid), UI.card(UI.cardTitle("Dispatch & receipt"), a.grid)), right, 340));
    }

    private int pos(Delivery t) { int i = 1; for (Delivery d : sys.getUpcomingDeliveries()) { if (d == t) return i; i++; } return 0; }

    @Override public String title() { return "10  Dispatch & Delivery"; }
    @Override public Feature feature() { return Feature.DISPATCH; }

    private void schedule() {
        String ref = UI.idOf(sale);
        Sale s = ref == null ? null : sys.sales.linearSearch(x -> x.ref.equals(ref));
        sys.scheduleDelivery(s, deadline.getValue(), vehicle.getText(), destination.getText(), Integer.parseInt(urgency.getValue().substring(0, 1)));
        vehicle.clear(); destination.clear(); refresh();
    }

    private void confirm() {
        Delivery d = history.getSelectionModel().getSelectedItem();
        if (d == null) throw new AgriException("Select a dispatched delivery in the lower table.");
        sys.confirmReceipt(d, UI.num(received, "Received quantity")); received.clear(); refresh();
    }

    @Override public void refresh() {
        UI.fill(upcoming, sys.getUpcomingDeliveries());
        List<Delivery> done = new ArrayList<>();
        for (Delivery d : sys.deliveries) if (!d.status.equals("SCHEDULED")) done.add(d);
        UI.fill(history, done);
        List<String> l = new ArrayList<>();
        for (Sale s : sys.sales) if (s.status.equals("CONFIRMED") && !s.deliveryScheduled) l.add(s.ref + " | " + s.buyer + " (" + Fmt.kg(s.qty) + " kg)");
        UI.setItems(sale, l);
    }
}
