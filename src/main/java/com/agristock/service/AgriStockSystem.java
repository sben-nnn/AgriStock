package com.agristock.service;

import com.agristock.model.AgriException;
import com.agristock.model.Models.*;
import com.agristock.ds.DynamicArray;

// Login and user management for AgriStock
public class AgriStockSystem {
    public DynamicArray<User> users = new DynamicArray<>();
    private User currentUser = null;

    public void addUser(String username, String fullName, String password, Role role) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).username.equals(username)) {
                throw new AgriException("User already exists.");
            }
        }
        users.add(new User(username, password, fullName, role));
    }

    // Log in a user with username and password
    public User login(String username, String password) {
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.username.equals(username) && u.passwordHash.equals(password)) {
                if (!u.active) {
                    throw new AgriException("Account is deactivated.");
                }
                currentUser = u;
                return u;
            }
        }
        throw new AgriException("Invalid username or password.");
    }

    // Log out the current user
    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Check if current user can use a feature
    public boolean checkPermission(Feature f) {
        if (currentUser == null) {
            return false;
        }
        return f.allows(currentUser.role);
    }

    public void editUser(String username, String fullName, String password) {
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.username.equals(username)) {
                u.fullName = fullName;
                if (password != null && !password.isEmpty()) {
                    u.passwordHash = password;
                }
                return;
            }
        }
        throw new AgriException("User not found.");
    }

    public void assignRole(String username, Role role) {
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.username.equals(username)) {
                u.role = role;
                return;
            }
        }
        throw new AgriException("User not found.");
    }

    public void setUserActive(String username, boolean active) {
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.username.equals(username)) {
                u.active = active;
                return;
            }
        }
        throw new AgriException("User not found.");
    }
}
//