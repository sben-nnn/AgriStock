package com.agristock.modules.Module11;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.service.Fmt;
import com.agristock.ui.Ctx;
import com.agristock.ui.View;
import com.agristock.ui.UI;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpoilageView extends View {
    private final SpoilageService svc;
    private final TextField search = new TextField();
    private final TableView<Spoilage> table = UI.table();
    private final ComboBox<String> item = new ComboBox<>(), reason = new ComboBox<>();
    private final TextField qty, reporter;
    private final Label total = new Label();

    public SpoilageView(Ctx ctx) {
        super(ctx);
        svc = new SpoilageService(sys);
        header("11 - Spoilage, Loss & Adjustments", "Losses are appended to a linked list and never overwritten. An authorised user (owner/manager) must approve an adjustment before stock changes.");
        UI.Form f = new UI.Form();
        f.add("Batch", item);
        qty = f.text("Quantity (kg)", "e.g. 10");
        reason.setEditable(true); reason.getItems().addAll(Arrays.asList("Spoiled / rotten", "Damaged in handling", "Pest damage", "Contaminated", "Missing / theft", "Spoiled during transport"));
        f.add("Reason", reason);
        reporter = f.text("Reporter", "e.g. Juan Dela Cruz");
        f.wide(UI.btn("Log spoilage / request adjustment", "primary", this::log));
        f.wide(UI.hint("Owners and managers are authorised, so their requests are approved immediately."));

        Button approveBtn = UI.btn("Approve", "primary", () -> act(true));
        Button rejectBtn = UI.btn("Reject", "danger", () -> act(false));
        boolean canApprove = sys.checkPermission(Feature.SPOILAGE_APPROVE);
        approveBtn.setDisable(!canApprove);
        rejectBtn.setDisable(!canApprove);

        UI.Form a = new UI.Form();
        a.wide(UI.hint(canApprove ? "Select a pending request in the table:" : "Only the Farm Owner or Farm Manager can approve or reject requests."));
        a.wide(UI.buttons(approveBtn, rejectBtn));
        a.wide(UI.btn("Financial loss report", "accent", () -> UI.textDialog("Financial loss report", svc.getFinancialLossReport())));
        total.getStyleClass().add("card-title");
        a.wide(total);

        UI.col(table, "ID", s -> s.id); UI.col(table, "Date", s -> s.date.toString()); UI.col(table, "Batch", s -> s.itemId);
        UI.col(table, "Qty (kg)", s -> Fmt.kg(s.qty)); UI.col(table, "Type", s -> s.kind); UI.col(table, "Reason", s -> s.reason);
        UI.col(table, "Reporter", s -> s.reporter); UI.col(table, "Status", s -> s.status); UI.col(table, "Approver", s -> s.approver);
        UI.col(table, "Loss value", s -> Fmt.peso(s.lossValue));
        search.setPromptText("Batch ID (linear search)");
        HBox searchRow = UI.row(search, UI.btn("Search", "primary", this::refresh), UI.btn("Show all", "ghost", () -> { search.clear(); refresh(); }));
        getChildren().add(UI.split(new VBox(14, UI.card(UI.cardTitle("Report a loss"), f.grid), UI.card(UI.cardTitle("Approval & reports"), a.grid)),
                UI.card(UI.cardTitle("Spoilage & adjustment log (newest first)"), searchRow, table), 340));
    }

    @Override public String title() { return "11  Spoilage & Loss"; }
    @Override public Feature feature() { return Feature.SPOILAGE_LOG; }

    private void log() {
        Spoilage s = svc.requestAdjustment(UI.idOf(item), UI.num(qty, "Quantity"), reason.getEditor().getText(), reporter.getText());
        qty.clear(); reporter.clear(); refreshAll();
        UI.info("Logged", s.id + " is " + s.status + (s.status.equals("PENDING") ? " - waiting for an authorised user." : "."));
    }

    private void act(boolean approve) {
        Spoilage s = table.getSelectionModel().getSelectedItem();
        if (s == null) throw new AgriException("Select a request first.");
        if (approve) svc.approveAdjustment(s); else svc.rejectAdjustment(s);
        refreshAll();
    }

    @Override public void refresh() {
        UI.fill(table, svc.findByBatch(search.getText()));
        List<String> l = new ArrayList<>();
        for (StockItem s : sys.traverseInventory()) if (s.remaining > 0) l.add(s.itemId + " | " + s.crop + " (" + Fmt.kg(s.available()) + " kg available)");
        UI.setItems(item, l);
        double q = 0, v = 0;
        for (Spoilage s : sys.spoilage) if (s.status.equals("APPROVED")) { q += s.qty; v += s.lossValue; }
        total.setText("Approved losses: " + Fmt.kg(q) + " kg / " + Fmt.peso(v));
        if (reporter.getText().isEmpty() && sys.session != null) reporter.setText(sys.session.username);
    }
}