package com.agristock.ui;

import com.agristock.model.AgriException;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.Role;
import com.agristock.model.Models.User;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

// User & Role Management
public class UsersView extends View {
    private final TableView<User> table = UI.table();
    private TextField username, fullName;
    private PasswordField password;
    private final ComboBox<Role> role = new ComboBox<>();

    public UsersView(Ctx ctx) { super(ctx); build(); }

    private void build() {
        header("1 - User & Role Management", "Add, edit, and manage user accounts. Role-based access controls what each user can do.");
        UI.Form f = new UI.Form();
        username = f.text("Username");
        fullName = f.text("Full name");
        password = f.password("Password");
        role.getItems().addAll(Role.values()); role.setValue(Role.INVENTORY);
        f.add("Role", role);
        f.wide(UI.buttons(
                UI.btn("Add user", "primary", this::add),
                UI.btn("Update details", "secondary", this::edit),
                UI.btn("Assign role", "secondary", this::assign),
                UI.btn("Deactivate / Activate", "danger", this::toggle)));
        UI.col(table, "Username", u -> u.username);
        UI.col(table, "Full name", u -> u.fullName);
        UI.col(table, "Role", u -> u.role.label);
        UI.col(table, "Status", u -> u.active ? "Active" : "Deactivated");
        getChildren().addAll(
                UI.card(UI.cardTitle("User account"), f.grid),
                UI.card(UI.cardTitle("Registered users"), table));
    }

    @Override public String title() { return "1  Users & Roles"; }
    @Override public Feature feature() { return Feature.USERS; }

    private void add() {
        sys.addUser(username.getText(), fullName.getText(), password.getText(), role.getValue());
        password.clear(); refresh();
    }
    private void edit() {
        User u = table.getSelectionModel().getSelectedItem();
        if (u == null) throw new AgriException("Select a user first.");
        sys.editUser(u.username, fullName.getText(), password.getText());
        password.clear(); refresh();
    }
    private void assign() {
        User u = table.getSelectionModel().getSelectedItem();
        if (u == null) throw new AgriException("Select a user first.");
        sys.assignRole(u.username, role.getValue()); refresh();
    }
    private void toggle() {
        User u = table.getSelectionModel().getSelectedItem();
        if (u == null) throw new AgriException("Select a user first.");
        sys.setUserActive(u.username, !u.active); refresh();
    }

    @Override public void refresh() {
        UI.fill(table, sys.users.toSortedArray());
    }
}
