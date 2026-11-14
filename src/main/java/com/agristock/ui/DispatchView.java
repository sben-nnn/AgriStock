package com.agristock.ui;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** MODULE 10: Dispatch & Delivery (priority queue, earliest deadline first). */
public class DispatchView extends View {
    private final TableView<Delivery> schedTable = UI.table(), histTable = UI.table();
    private final ComboBox<String> saleRef = new ComboBox<>();
    private final TextField deadline, vehicle, destination, urgency;

    public DispatchView(Ctx ctx) {
        super(ctx);
        header("10 - Dispatch & Delivery", "Schedule deliveries by earliest deadline (EDF priority queue). Dispatch the root, then confirm receipt.");
        UI.Form f = new UI.Form();
        f.add("Sale Ref", saleRef);
        deadline = f.text("Deadline (YYYY-MM-DD)");
        vehicle = f.text("Vehicle");
        destination = f.text("Destination");
        urgency = f.text("Urgency (1=high, 3=low)"); urgency.setText("2");
        f.wide(UI.btn("Schedule Delivery", "primary", this::schedule));
        f.wide(UI.btn("Dispatch Next (EDF)", "secondary", this::dispatch));
        f.wide(UI.btn("Confirm Receipt", "ghost", this::receipt));
        UI.col(schedTable, "ID", d -> d.id); UI.col(schedTable, "Sale Ref", d -> d.saleRef);
        UI.col(schedTable, "Company", d -> d.company); UI.col(schedTable, "Item", d -> d.itemId);
        UI.col(schedTable, "Qty", d -> Fmt.kg(d.qty)); UI.col(schedTable, "Deadline", d -> d.deadline.toString());
        UI.col(schedTable, "Vehicle", d -> d.vehicle); UI.col(schedTable, "Status", d -> d.status);
        UI.col(histTable, "ID", d -> d.id); UI.col(histTable, "Sale Ref", d -> d.saleRef);
        UI.col(histTable, "Company", d -> d.company); UI.col(histTable, "Item", d -> d.itemId);
        UI.col(histTable, "Dispatched", d -> Fmt.kg(d.dispatchedQty)); UI.col(histTable, "Received", d -> Fmt.kg(d.receivedQty));
        UI.col(histTable, "Deadline", d -> d.deadline.toString()); UI.col(histTable, "Status", d -> d.status);
        getChildren().add(UI.split(new VBox(14, UI.card(UI.cardTitle("Schedule Delivery"), f.grid)),
                new VBox(10,
                        UI.card(UI.cardTitle("Scheduled Deliveries (priority queue)"), schedTable),
                        UI.card(UI.cardTitle("Delivery History"), histTable)), 340));
    }

    @Override public String title() { return "10  Dispatch"; }
    @Override public Feature feature() { return Feature.DISPATCH; }

    private void schedule() {
        Sale s = findSale(UI.idOf(saleRef));
        if (s == null) throw new AgriException("Select a confirmed sale.");
        LocalDate dl = parseDate(deadline.getText());
        if (dl == null) throw new AgriException("Enter a valid deadline date.");
        int urg = Integer.parseInt(urgency.getText().trim());
        Delivery d = sys.scheduleDelivery(s, dl, vehicle.getText(), destination.getText(), urg);
        saleRef.getSelectionModel().clearSelection(); deadline.clear(); vehicle.clear(); destination.clear(); urgency.setText("2");
        refresh();
        UI.info("Scheduled", d.id + " for " + d.company + " by " + d.deadline);
    }

    private void dispatch() {
        Delivery d = sys.recordDispatch();
        refresh();
        UI.info("Dispatched", d.id + " dispatched to " + d.company + " via " + d.vehicle);
    }

    private void receipt() {
        Delivery d = schedTable.getSelectionModel().getSelectedItem();
        if (d == null) throw new AgriException("Select a dispatched delivery.");
        TextInputDialog dialog = new TextInputDialog(String.valueOf(d.dispatchedQty));
        dialog.setTitle("Confirm Receipt");
        dialog.setHeaderText("Received quantity for " + d.id + " (" + d.company + ")");
        dialog.setContentText("Qty received (kg):");
        dialog.showAndWait().ifPresent(val -> {
            try {
                sys.confirmReceipt(d, Double.parseDouble(val));
                refresh();
                UI.info("Received", "Receipt confirmed for " + d.id);
            } catch (NumberFormatException e) {
                UI.error("Invalid quantity.");
            } catch (AgriException e) {
                UI.error(e.getMessage());
            }
        });
    }

    private Sale findSale(String ref) {
        for (Sale s : sys.sales) if (s.ref.equalsIgnoreCase(ref) && s.status.equals("CONFIRMED") && !s.deliveryScheduled) return s;
        return null;
    }

    private LocalDate parseDate(String s) {
        try { return s == null || s.isEmpty() ? null : LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    @Override public void refresh() {
        List<Delivery> sched = new ArrayList<>();
        for (Delivery d : sys.getUpcomingDeliveries()) sched.add(d);
        UI.fill(schedTable, sched);
        List<Delivery> hist = new ArrayList<>();
        for (Delivery d : sys.deliveries) if (!d.status.equals("SCHEDULED") && !d.status.equals("DISPATCHED")) hist.add(d);
        UI.fill(histTable, hist);
        List<String> refs = new ArrayList<>();
        for (Sale s : sys.sales) if (s.status.equals("CONFIRMED") && !s.deliveryScheduled) refs.add(s.ref);
        UI.setItems(saleRef, refs);
    }
}
