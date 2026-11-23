package com.agristock;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.service.AgriStockSystem;
import com.agristock.service.Persistence;
import com.agristock.service.SampleData;
import com.agristock.ui.*;
import com.agristock.modules.Module1.UserView;
import com.agristock.modules.Module3.BatchesView;
import com.agristock.modules.Module4.HarvestStockView;
import com.agristock.modules.Module5.InspectionView;
import com.agristock.modules.Module6.StorageView;
import com.agristock.modules.Module7.StockView;
import com.agristock.modules.Module8.ExpiryView;
import com.agristock.modules.Module9.SalesView;
import com.agristock.modules.Module10.DispatchView;
import com.agristock.modules.Module11.SpoilageView;
import com.agristock.modules.Module12.ForecastView;
import com.agristock.modules.Module13.ReportsView;
import com.agristock.modules.Module14.AuditView;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

// AgriStock - Agricultural Stock Management System (JavaFX front end)
public class MainApp extends Application {
    private final AgriStockSystem sys = new AgriStockSystem();
    private final Ctx ctx = new Ctx(sys, this::refreshAll);
    private Stage stage;
    private boolean loadFailed;
    private ListView<View> nav;
    private StackPane content;
    private List<View> allViews;

    @Override
    public void start(Stage primary) {
        stage = primary;
        stage.setTitle("AgriStock - Agricultural Stock Management System");
        stage.setMinWidth(1100); stage.setMinHeight(700);
        try { Persistence.load(sys); }
        catch (AgriException e) { loadFailed = true; UI.error(e.getMessage()); }
        if (sys.users.size() == 0) sys.seedDefaultUsers();
        stage.setOnCloseRequest(e -> saveOnExit());
        showLogin();
        stage.show();
    }

    private void saveOnExit() {
        if (loadFailed) return;
        try { if (sys.session != null) sys.logout(); Persistence.save(sys); }
        catch (AgriException ex) { UI.error(ex.getMessage()); }
    }

    // Opens the scene, never larger than the visible screen area. */
    private void setScene(Parent root, double w, double h) {
        Rectangle2D vb = Screen.getPrimary().getVisualBounds();
        Scene s = new Scene(root, Math.min(w, vb.getWidth()), Math.min(h, vb.getHeight() - 60));
        s.getStylesheets().add(UI.css());
        stage.setScene(s);
    }

    // ------------------------------------------------------------------ login
    private void showLogin() {
        Label title = new Label("AgriStock"); title.getStyleClass().add("login-title");
        Label sub = new Label("Agricultural Stock Management System"); sub.getStyleClass().add("page-sub");
        TextField user = new TextField(); user.setPromptText("Username");
        PasswordField pass = new PasswordField(); pass.setPromptText("Password");
        Label msg = new Label(); msg.getStyleClass().add("status-bad"); msg.setWrapText(true);
        Button login = new Button("Sign in"); login.getStyleClass().add("primary"); login.setMaxWidth(Double.MAX_VALUE); login.setDefaultButton(true);
        login.setOnAction(e -> {
            try { sys.login(user.getText(), pass.getText()); showMain(); }
            catch (Exception ex) { msg.setText(ex.getMessage()); pass.clear(); }
        });
        Label hint = UI.hint("Default accounts (first run): admin/admin123 \u2022 manager/manager123 \u2022 inventory/inventory123 \u2022 sales/sales123");
        VBox card = new VBox(12, title, sub, new Separator(), user, pass, login, msg, hint);
        card.getStyleClass().add("login-card"); card.setMaxWidth(380);
        StackPane root = new StackPane(card); root.getStyleClass().add("login-bg");
        setScene(root, 1200, 780);
        user.requestFocus();
    }

    // ------------------------------------------------------------------ main window
    private void showMain() {
        List<View> all = new ArrayList<>(List.of(
                new DashboardView(ctx), new UserView(ctx), new BatchesView(ctx), new HarvestStockView(ctx),
                new InspectionView(ctx), new StorageView(ctx), new StockView(ctx), new ExpiryView(ctx), new SalesView(ctx),
                new DispatchView(ctx), new SpoilageView(ctx), new ForecastView(ctx), new ReportsView(ctx), new AuditView(ctx)));
        allViews = all;
        // Each process (module) is hidden unless the signed-in user's role is permitted to use it.
        // Dashboard (feature == null) is visible to everyone; every other screen is gated by its
        // Feature so a role never sees processes it must not use.
        List<View> allowed = new ArrayList<>();
        for (View v : all) if (v.feature() == null || sys.checkPermission(v.feature())) allowed.add(v);

        Label brand = new Label("\uD83C\uDF3E AgriStock"); brand.getStyleClass().add("brand");
        Label brandSub = new Label("Farm inventory made simple"); brandSub.getStyleClass().add("brand-sub");
        VBox head = new VBox(2, brand, brandSub); head.setPadding(new Insets(20, 18, 12, 18));
        nav = new ListView<>(FXCollections.observableArrayList(allowed));
        nav.getStyleClass().add("nav-list");
        nav.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(View v, boolean empty) { super.updateItem(v, empty); setText(empty || v == null ? null : v.title()); }
        });
        VBox.setVgrow(nav, Priority.ALWAYS);
        VBox sidebar = new VBox(head, nav); sidebar.getStyleClass().add("sidebar"); sidebar.setPrefWidth(235);

        content = new StackPane();
        ScrollPane sp = new ScrollPane(content); sp.setFitToWidth(true); sp.setFitToHeight(true);
        nav.getSelectionModel().selectedItemProperty().addListener((o, a, v) -> { if (v != null) { content.getChildren().setAll(v); v.refresh(); } });

        Label chip = new Label(sys.session.fullName + "  \u2022  " + sys.session.role.label); chip.getStyleClass().add("user-chip");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(10, spacer, chip);
        top.getStyleClass().add("topbar"); top.setAlignment(Pos.CENTER_RIGHT);
        if (sys.checkPermission(Feature.PERSISTENCE)) top.getChildren().add(UI.btn("\uD83D\uDCBE Save data", "secondary", this::save));
        if (sys.session.role == Role.OWNER) top.getChildren().add(UI.btn("Load sample data", "ghost", this::loadSample));
        top.getChildren().add(UI.btn("Log out", "danger", this::logout));

        BorderPane root = new BorderPane(sp);
        root.setLeft(sidebar); root.setTop(top);
        setScene(root, 1366, 820);
        nav.getSelectionModel().selectFirst();
    }

    /** Refresh all module views so every table reflects the latest data immediately. */
    private void refreshAll() {
        if (allViews == null) return;
        for (View v : allViews) v.refresh();
    }

    private void save() {
        if (loadFailed)
            throw new AgriException("Saving is disabled: the saved file could not be read and would be overwritten. "
                    + "Fix or rename data/agristock_state.csv and restart the app.");
        Persistence.save(sys);
        UI.info("Saved", "System state written to " + Persistence.FILE + " (it is also saved automatically on exit).");
    }

    private void loadSample() {
        if (!UI.confirm("Load demo farmers, batches, sales and deliveries?\n\nThis REPLACES all existing farms, crops, batches, stock, sales, deliveries and spoilage records with demo data. User accounts and the audit trail are kept.")) return;
        SampleData.load(sys);
        refreshAll();
        UI.info("Sample data", "Demo data loaded. Browse the modules to explore it.");
    }

    private void logout() {
        sys.logout();
        if (!loadFailed) Persistence.save(sys);
        showLogin();
    }

    public static void main(String[] args) { launch(args); }
}