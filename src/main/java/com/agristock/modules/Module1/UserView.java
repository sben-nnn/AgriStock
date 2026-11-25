package com.agristock.modules.Module1;

import com.agristock.model.AgriException;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.Role;
import com.agristock.model.Models.User;
import com.agristock.ui.Ctx;
import com.agristock.ui.UI;
import com.agristock.ui.View;
import javafx.scene.control.*;

// User & Role Management
public class UserView extends View {
    private final TableView<User> table = UI.table();
    private final UserService svc;
    private TextField username, fullName;
    private PasswordField password;
    private final ComboBox<Role> role = new ComboBox<>();

    public UserView(Ctx ctx) { super(ctx); svc = new UserService(sys); build(); }

    private void build() {
        header("1 - User & Role Management",
                "Manage who can use the system. Passwords are stored as secure codes and every change is saved to an activity log.");
        UI.Form f = new UI.Form();
        username = f.text("Username", "e.g. jdoe");
        fullName = f.text("Full name", "e.g. Juan Dela Cruz");
        password = f.password("Password");
        password.setPromptText("••••••••");
        role.getItems().addAll(Role.values()); role.setValue(Role.INVENTORY);
        f.add("Role", role);
        f.wide(UI.buttons(
                UI.btn("Add user", "primary", this::add),
                UI.btn("Update details", "secondary", this::edit),
                UI.btn("Assign role", "secondary", this::assign),
                UI.btn("Deactivate / Activate", "danger", this::toggle),
                UI.btn("Delete user", "danger", this::delete)));
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
        svc.add(username.getText(), fullName.getText(), password.getText(), role.getValue());
        password.clear(); refreshAll();
    }
    private void edit() {
        User u = table.getSelectionModel().getSelectedItem();
        if (u == null) throw new AgriException("Select a user first.");
        svc.edit(u.username, fullName.getText(), password.getText());
        password.clear(); refreshAll();
    }
    private void assign() {
        User u = table.getSelectionModel().getSelectedItem();
        if (u == null) throw new AgriException("Select a user first.");
        svc.assign(u.username, role.getValue()); refreshAll();
    }
    private void toggle() {
        User u = table.getSelectionModel().getSelectedItem();
        if (u == null) throw new AgriException("Select a user first.");
        svc.setActive(u.username, !u.active); refreshAll();
    }
    private void delete() {
        User u = table.getSelectionModel().getSelectedItem();
        if (u == null) throw new AgriException("Select a user first.");
        if (!UI.confirm("Delete user '" + u.username + "'?")) return;
        svc.delete(u.username); refreshAll();
    }

    @Override public void refresh() {
        UI.fill(table, svc.getAll());
    }
}
