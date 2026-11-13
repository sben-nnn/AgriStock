package com.agristock.modules.Module1;

import com.agristock.ui.Ctx;
import com.agristock.ui.View;
import com.agristock.model.Models.Feature;

public class UserView extends View {

    public UserView(Ctx ctx) {
        super(ctx);
        header("User & Role Management", "Manage system users and access roles");
    }

    @Override
    public String title() {
        return "1 • Users";
    }

    @Override
    public Feature feature() {
        return Feature.USERS;
    }

    @Override
    public void refresh() {

    }
}