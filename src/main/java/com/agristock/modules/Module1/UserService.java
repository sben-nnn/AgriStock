package com.agristock.modules.Module1;

import com.agristock.ds.DynamicArray;
import com.agristock.model.Models.Role;
import com.agristock.model.Models.User;
import com.agristock.service.AgriStockSystem;

// Service for managing users. Delegates to the shared system data structures (AVL tree).
public class UserService {
    private final AgriStockSystem sys;

    public UserService(AgriStockSystem sys) { this.sys = sys; }

    public void add(String username, String fullName, String password, Role role) {
        sys.addUser(username, fullName, password, role);
    }

    public void edit(String username, String fullName, String password) {
        sys.editUser(username, fullName, password);
    }

    public void assign(String username, Role role) {
        sys.assignRole(username, role);
    }

    public void setActive(String username, boolean active) {
        sys.setUserActive(username, active);
    }

    public void delete(String username) {
        sys.deleteUser(username);
    }

    public DynamicArray<User> getAll() {
        return sys.users.toSortedArray();
    }

    public User findByUsername(String username) {
        return sys.users.search(username == null ? "" : username);
    }
}
