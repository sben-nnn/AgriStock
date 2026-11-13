package com.agristock.ui;

import com.agristock.model.Models.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Arrays;

/** MODULE 1: User & Role Management (AVL tree). */
public class UsersView extends View {
    private final TableView<User> table = UI.table();
    private final TextField username, fullName;
    private final PasswordField password;
    private final ComboBox<Role> role = new ComboBox<>();

    public UsersView(Ctx ctx) {
        super(ctx);
        header("1 - User & Role Management", "Users are stored in an AVL tree keyed by username; login uses binary search, listings use in-order traversal.");
        UI.Form f = new UI.Form();
        username = f.text("Username");
        fullName = f.text("Full name");
        password = f.password("Password");
        role.getItems().addAll(Role.values()); role.setValue(Role.INVENTORY); f.add("Role", role);
        f.wide(UI.hint("To edit a user, select a row first. Leave the password blank to keep the current one."));
        f.wide(UI.buttons(
                UI.btn("Add user", "primary", this::add),
                UI.btn("Update details", "secondary", this::edit),
                UI.btn("Assign role", "secondary", this::assign),
                UI.btn("Deactivate / Activate", "danger", this::toggle)));
        VBox left = UI.card(UI.cardTitle("User account"), f.grid);

        UI.col(table, "Username", u -> u.username);
        UI.col(table, "Full name", u -> u.fullName);
        UI.col(table, "Role", u -> u.role.label);
        UI.col(table, "Status", u -> u.active ? "Active" : "Deactivated");
        table.getSelectionModel().selectedItemProperty().addListener((o, a, u) -> {
            if (u != null) { username.setText(u.username); fullName.setText(u.fullName); role.setValue(u.role); password.clear(); }
        });
        Label perms = UI.hint("Permissions - Owner: everything | Manager: all except user admin | Inventory Staff: batches, harvest, inspection, storage, stock, audit counts | Sales/Dispatch: sales, deliveries, FEFO dispatch.");
        getChildren().add(UI.split(left, UI.card(UI.cardTitle("Registered users (in-order traversal)"), table, perms), 340));
    }

    @Override public String title() { return "1  Users & Roles"; }
    @Override public Feature feature() { return Feature.USERS; }

    private void add() { sys.addUser(username.getText(), fullName.getText(), password.getText(), role.getValue()); password.clear(); refresh(); }
    private void edit() { sys.editUser(username.getText(), fullName.getText(), password.getText()); password.clear(); refresh(); }
    private void assign() { sys.assignRole(username.getText(), role.getValue()); refresh(); }
    private void toggle() {
        User u = table.getSelectionModel().getSelectedItem();
        if (u == null) throw new com.agristock.model.AgriException("Select a user in the table.");
        sys.setUserActive(u.username, !u.active); refresh();
    }

    @Override public void refresh() { UI.fill(table, sys.users.toSortedArray()); }
}
