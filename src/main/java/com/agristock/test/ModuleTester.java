package com.agristock.test;

import com.agristock.modules.Module11.SpoilageView;
import com.agristock.modules.Module12.ForecastView;
import com.agristock.modules.Module13.ReportsView;
import com.agristock.modules.Module14.AuditView;
import com.agristock.service.AgriStockSystem;
import com.agristock.ui.Ctx;
import com.agristock.ui.UI;
import com.agristock.ui.View;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Stand-alone tester for modules 11-14 (Spoilage, Forecasting, Reports, Audit).
 *
 * - No login screen, no saved file: the system is filled from {@link TestData} on start-up.
 * - Pick the signed-in role from the drop-down to test permissions (views are rebuilt, data is kept).
 * - "Reset test data" throws everything away and reloads the hard-coded data.
 * - Nothing is ever written to data/agristock_state.csv.
 *
 * Run {@link TesterLauncher} (IntelliJ: right-click -> Run), or see the README note for the Maven command.
 */
public class ModuleTester extends Application {
    private static final String[] ROLE_LABELS = {"Farm Owner", "Farm Manager", "Inventory Staff", "Sales / Dispatch"};
    private static final String[] ROLE_USERS = {"admin", "manager", "inventory", "sales"};

    private final ComboBox<String> roleBox = new ComboBox<>();
    private final TabPane tabs = new TabPane();
    private final List<View> views = new ArrayList<>();
    private AgriStockSystem sys;

    @Override
    public void start(Stage stage) {
        stage.setTitle("AgriStock - Module 11-14 Tester (hard-coded data)");

        roleBox.getItems().addAll(ROLE_LABELS);
        roleBox.setValue(ROLE_LABELS[0]);          // set the value first, then attach the listener
        sys = newSystem();
        roleBox.setOnAction(e -> switchRole());

        Label title = new Label("Module tester:  11 Spoilage  |  12 Forecasting  |  13 Reports  |  14 Audit");
        title.getStyleClass().add("card-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(10, title, spacer, new Label("Signed in as:"), roleBox,
                UI.btn("Reset test data", "danger", this::resetData));
        top.getStyleClass().add("topbar");
        top.setAlignment(Pos.CENTER_LEFT);

        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        BorderPane root = new BorderPane(tabs);
        root.setTop(top);
        build();

        Rectangle2D vb = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, Math.min(1300, vb.getWidth()), Math.min(820, vb.getHeight() - 60));
        scene.getStylesheets().add(UI.css());
        stage.setScene(scene);
        stage.show();
    }

    // ------------------------------------------------------------------ data / role handling

    private String selectedUsername() {
        int i = roleBox.getItems().indexOf(roleBox.getValue());
        return ROLE_USERS[Math.max(0, i)];
    }

    /** Fresh system, hard-coded data, signed in as the role currently chosen in the drop-down. */
    private AgriStockSystem newSystem() {
        AgriStockSystem s = new AgriStockSystem();
        TestData.load(s);
        s.session = s.users.search(selectedUsername());
        return s;
    }

    private void switchRole() {
        sys.session = sys.users.search(selectedUsername());
        build();                                   // views read permissions when they are created
    }

    private void resetData() {
        if (!UI.confirm("Discard all changes and reload the hard-coded test data?")) return;
        sys = newSystem();
        build();
    }

    // ------------------------------------------------------------------ views

    private void build() {
        int keep = Math.max(0, tabs.getSelectionModel().getSelectedIndex());
        tabs.getTabs().clear();
        views.clear();

        Ctx ctx = new Ctx(sys, this::refreshAll);
        addTab(new SpoilageView(ctx));
        addTab(new ForecastView(ctx));
        addTab(new ReportsView(ctx));
        addTab(new AuditView(ctx));

        tabs.getSelectionModel().select(keep);
        refreshAll();
    }

    private void addTab(View v) {
        ScrollPane sp = new ScrollPane(v);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        // The main app hides screens a role may not use; here they stay visible (marked) so the
        // "Access denied" messages from the services can be tested.
        boolean allowed = v.feature() == null || sys.checkPermission(v.feature());
        Tab tab = new Tab(v.title() + (allowed ? "" : "  (no access)"), sp);
        tab.setOnSelectionChanged(e -> { if (tab.isSelected()) v.refresh(); });
        tabs.getTabs().add(tab);
        views.add(v);
    }

    private void refreshAll() {
        for (View v : views) v.refresh();
    }

    public static void main(String[] args) { launch(args); }
}