package com.agristock.modules.Module14;

import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import com.agristock.ui.Ctx;
import com.agristock.ui.View;
import com.agristock.ui.UI;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AuditView extends View {
    private final AuditService svc;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final TableView<AuditEntry> table = UI.table();
    private final ComboBox<String> item = new ComboBox<>();
    private final TextField physical, note, filter = new TextField();
    private final Label result = new Label("Enter a physical count and press Compare.");

    public AuditView(Ctx ctx) {
        super(ctx);
        svc = new AuditService(sys);
        header("14 - Inventory Audit & Validation", "Physical counts are compared with recorded counts. Every transaction is appended to an immutable audit trail; invalid movements are rejected and logged.");
        UI.Form f = new UI.Form();
        f.add("Batch", item);
        physical = f.text("Physical count (kg)", "e.g. 980");
        note = f.text("Note", "e.g. Counted during morning shift");
        result.setWrapText(true); result.getStyleClass().add("card-title");
        f.wide(result);

        Button reconcileBtn = UI.btn("Reconcile (apply)", "primary", this::reconcile);
        boolean canReconcile = sys.checkPermission(Feature.AUDIT_RECONCILE);
        reconcileBtn.setDisable(!canReconcile);

        f.wide(UI.buttons(UI.btn("Compare", "secondary", this::compare), UI.btn("Record difference", "secondary", this::record), reconcileBtn));
        f.wide(UI.hint(canReconcile
                ? "Recording keeps stock unchanged. Reconciling sets the recorded stock to the physical count and needs Owner/Manager authorisation."
                : "Recording keeps stock unchanged. Reconciling is disabled: it needs Farm Owner / Farm Manager authorisation."));
        filter.setPromptText("Batch / reference (linear search)");
        UI.col(table, "Time", e -> e.time.format(FMT)); UI.col(table, "Event", e -> e.type); UI.col(table, "Reference", e -> e.ref);
        UI.col(table, "User", e -> e.user); UI.col(table, "Recorded", e -> Fmt.kg(e.recorded)); UI.col(table, "Physical", e -> Fmt.kg(e.physical));
        UI.col(table, "Difference", e -> Fmt.kg(e.discrepancy)); UI.col(table, "Note", e -> e.note);
        VBox right = UI.card(UI.cardTitle("Audit trail (newest first)"), UI.row(filter, UI.btn("Search", "primary", this::refresh), UI.btn("Show all", "ghost", () -> { filter.clear(); refresh(); })), table);
        getChildren().add(UI.split(UI.card(UI.cardTitle("Physical count"), f.grid), right, 340));
    }

    @Override public String title() { return "14  Audit & Validation"; }
    @Override public Feature feature() { return Feature.AUDIT_VIEW; }

    private void compare() {
        double diff = svc.compareCounts(UI.idOf(item), UI.num(physical, "Physical count"));
        result.setText(Math.abs(diff) < 0.0001 ? "Counts match - no discrepancy." : String.format("Discrepancy: %+,.2f kg (%s)", diff, diff < 0 ? "shortage" : "surplus"));
    }
    private void record() { svc.recordDifference(UI.idOf(item), UI.num(physical, "Physical count"), note.getText()); compare(); refresh(); }
    private void reconcile() { svc.reconcile(UI.idOf(item), UI.num(physical, "Physical count"), note.getText()); result.setText("Reconciled - recorded stock now equals the physical count."); physical.clear(); refresh(); }

    @Override public void refresh() {
        UI.fill(table, svc.auditHistory(filter.getText()));
        List<String> l = new ArrayList<>();
        for (StockItem s : sys.traverseInventory()) l.add(s.itemId + " | " + s.crop + " (recorded " + Fmt.kg(s.remaining) + " kg)");
        UI.setItems(item, l);
    }
}