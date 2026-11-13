package com.agristock;

import com.agristock.service.AgriStockSystem;
import com.agristock.model.Models.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Stage stage;
    private AgriStockSystem sys = new AgriStockSystem();

    @Override
    public void start(Stage primary) {
        stage = primary;
        stage.setTitle("AgriStock - Agricultural Stock Management System");
        stage.setMinWidth(1100); stage.setMinHeight(700);
        addSampleUsers();
        showLogin();
        stage.show();
    }

    // Add a few default users so login can be tested
    private void addSampleUsers() {
        sys.addUser("owner", "owner", "Farm Owner", Role.OWNER);
        sys.addUser("manager", "manager", "Farm Manager", Role.MANAGER);
        sys.addUser("staff", "staff", "Inventory Staff", Role.INVENTORY);
    }

    // login UI
    private void showLogin() {
        Label title = new Label("AgriStock");
        Label sub = new Label("Agricultural Stock Management System");
        TextField user = new TextField(); user.setPromptText("Username");
        PasswordField pass = new PasswordField(); pass.setPromptText("Password");
        Label msg = new Label(); msg.setWrapText(true);   // error message area
        msg.setStyle("-fx-text-fill: red;");
        Button login = new Button("Sign in");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setDefaultButton(true);

        // handle login attempt
        login.setOnAction(e -> {
            try {
                sys.login(user.getText(), pass.getText());
                msg.setText("");
                showMain();
            } catch (Exception ex) {
                msg.setText(ex.getMessage());
            }
        });

        VBox card = new VBox(12, title, sub, new Separator(), user, pass, login, msg);
        card.setMaxWidth(380);
        StackPane root = new StackPane(card);
        stage.setScene(new Scene(root, 1200, 780));
        user.requestFocus();
    }

    // main screen after successful login
    private void showMain() {
        User u = sys.getCurrentUser();
        Label welcome = new Label("Welcome, " + u.fullName + " (" + u.role + ")");
        Button logout = new Button("Sign out");
        logout.setOnAction(e -> {
            sys.logout();
            showLogin();
        });
        VBox root = new VBox(20, welcome, logout);
        root.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(root, 1200, 780));
    }

    // launches the app
    public static void main(String[] args) { launch(args); }
}
