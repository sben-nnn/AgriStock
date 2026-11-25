package com.agristock.modules.Module10;

import com.agristock.ds.DynamicArray;
import com.agristock.model.Models.Delivery;
import com.agristock.model.Models.Sale;
import com.agristock.service.AgriStockSystem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Service for managing deliveries. Delegates to the shared system structures
// (heap-backed priority queue ordered by earliest deadline / EDF).
public class DispatchService {
    private final AgriStockSystem sys;

    public DispatchService(AgriStockSystem sys) { this.sys = sys; }

    public Delivery schedule(Sale sale, LocalDate deadline, String vehicle, String destination, int urgency) {
        return sys.scheduleDelivery(sale, deadline, vehicle, destination, urgency);
    }

    public Delivery recordDispatch() { return sys.recordDispatch(); }

    public void confirmReceipt(Delivery d, double receivedQty) { sys.confirmReceipt(d, receivedQty); }

    public boolean queueEmpty() { return sys.deliveryQueue.isEmpty(); }

    public Sale findSale(String ref) {
        for (Sale s : sys.sales) if (s.ref.equals(ref)) return s;
        return null;
    }

    public Delivery findDelivery(String id) {
        for (Delivery d : sys.deliveries) if (d.id.equals(id)) return d;
        return null;
    }

    /** Confirmed sales that have no delivery scheduled yet. */
    public List<String> eligibleSaleRefs() {
        List<String> refs = new ArrayList<>();
        for (Sale s : sys.sales)
            if (s.status.equals("CONFIRMED") && !s.deliveryScheduled) refs.add(s.ref);
        return refs;
    }

    /** Deliveries not yet received (eligible for dispatch / receipt). */
    public List<String> activeDeliveryIds() {
        List<String> ids = new ArrayList<>();
        for (Delivery d : sys.deliveries)
            if (!d.status.equals("RECEIVED")) ids.add(d.id);
        return ids;
    }

    public DynamicArray<Delivery> upcoming() { return sys.getUpcomingDeliveries(); }

    public List<Delivery> history() {
        List<Delivery> out = new ArrayList<>();
        for (Delivery d : sys.deliveries) out.add(d);
        return out;
    }
}
