package com.agristock.modules.Module1;

import com.agristock.model.Models.User;
import com.agristock.ds.DynamicArray;

// Service for managing users
public class UserService {
    private DynamicArray<User> users = new DynamicArray<>();

    // Add a user
    public void add(User user) {
        users.add(user);
    }

    // Get all users
    public DynamicArray<User> getAll() {
        return users;
    }

    // Find user by username
    public User findByUsername(String username) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).username.equalsIgnoreCase(username)) {
                return users.get(i);
            }
        }
        return null;
    }

    // Deactivate a user
    public void setActive(String username, boolean active) {
        User u = findByUsername(username);
        if (u != null) {
            u.active = active;
        }
    }
}