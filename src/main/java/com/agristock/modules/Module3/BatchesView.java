package com.agristock.modules.Module3;

import com.agristock.service.AgriStockSystem;
import com.agristock.model.AgriException;
import com.agristock.model.Models.Batch;
import com.agristock.model.Models.Crop;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.Farm;
import com.agristock.ds.DynamicArray;
import com.agristock.ui.View;
import com.agristock.ui.UI;
import com.agristock.ui.Ctx;
import com.agristock.service.AgriStockSystem;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// MODULE 3: Farm, Crop & Harvest Batch Registration (AVL Tree by batch ID)
public class BatchesView extends View {
    private final TableView<Farm> farmTable = UI.table();
    private final TableView<Crop> cropTable = UI.table();
    private final TableView<Batch> batchTable = UI.table();
    private final ComboBox<String> farmSelect = new ComboBox<>();
    private final ComboBox<String> cropSelect = new ComboBox<>();
    private TextField farmName, farmLocation, farmSize;
    private TextField cropName;
    private ComboBox<String> cropCategory;
    private ComboBox<String> cropVariety; // variety chosen from a selection
    private TextField batchWorker, batchQty;
    private ComboBox<String> batchUnit;
    private DatePicker batchHarvest, batchExpiry;
    private Label nextFarmId, nextBatchId;

    // Default crop varieties offered as a selection. The combo is editable so a variety
    // not in the list can still be entered.
    private static final List<String> VARIETIES = List.of(
            "Jasmine", "Diamante", "Carabao", "Yellow", "Long Purple", "Lakatan",
            "IR64", "NSIC Rc 222", "Hass", "Kent", "Granny Smith", "Cavendish",
            "Sweet Corn", "Glutinous", "Other"
    );

    public BatchesView(Ctx ctx) { super(ctx); build(); }

    private void build() {
        header("3 - Farm, Crop & Batch Registration", "");

        // ---- Tab 1: Register Farm (form + list in one tab, nothing blocked) ----
        UI.Form f1 = new UI.Form();
        f1.wide(nextFarmId = new Label());
        farmName = f1.text("Name");
        farmLocation = f1.text("Location");
        farmSize = f1.text("Size (ha)");
        f1.wide(UI.btn("Register Farm", "primary", this::registerFarm));
        VBox farmPane = new VBox(12, f1.grid);
        farmPane.setPadding(new Insets(12));

        UI.col(farmTable, "ID", f -> f.id);
        UI.col(farmTable, "Name", f -> f.name);
        UI.col(farmTable, "Location", f -> f.location);
        UI.col(farmTable, "Size (ha)", f -> String.format("%.2f", f.size));
        VBox farmCard = UI.card(UI.cardTitle("Farms"), farmTable);
        VBox.setVgrow(farmTable, Priority.ALWAYS);
        VBox.setVgrow(farmCard, Priority.ALWAYS);
        VBox tab1 = new VBox(12, farmPane, farmCard);
        VBox.setVgrow(tab1, Priority.ALWAYS);

        // ---- Tab 2: Register Crop (form + list in one tab) ----
        UI.Form f2 = new UI.Form();
        cropName = f2.text("Crop Name");
        cropVariety = new ComboBox<>(FXCollections.observableArrayList(VARIETIES));
        cropVariety.setEditable(true);
        cropVariety.setPromptText("Select or type a variety");
        f2.add("Variety", cropVariety);
        cropCategory = f2.combo("Category", Arrays.asList(AgriStockSystem.CATEGORIES));
        f2.wide(UI.btn("Register Crop", "secondary", this::registerCrop));
        VBox cropPane = new VBox(12, f2.grid);
        cropPane.setPadding(new Insets(12));

        UI.col(cropTable, "Name", c -> c.name);
        UI.col(cropTable, "Variety", c -> c.variety);
        UI.col(cropTable, "Category", c -> c.category);
        VBox cropCard = UI.card(UI.cardTitle("Crops"), cropTable);
        VBox.setVgrow(cropTable, Priority.ALWAYS);
        VBox.setVgrow(cropCard, Priority.ALWAYS);
        VBox tab2 = new VBox(12, cropPane, cropCard);
        VBox.setVgrow(tab2, Priority.ALWAYS);

        // ---- Tab 3: Create Batch (form + list in one tab) ----
        UI.Form f3 = new UI.Form();
        f3.wide(nextBatchId = new Label());
        f3.add("Farm ID", farmSelect);
        f3.add("Crop", cropSelect);
        batchWorker = f3.text("Worker");
        batchHarvest = f3.date("Harvest Date", LocalDate.now());
        batchExpiry = f3.date("Expiry Date", LocalDate.now().plusDays(7));
        batchQty = f3.text("Qty");
        batchUnit = f3.combo("Unit", Arrays.asList(AgriStockSystem.UNITS));
        batchUnit.setValue("kg");
        f3.wide(UI.btn("Create Batch", "primary", this::createBatch));
        VBox batchPane = new VBox(12, f3.grid);
        batchPane.setPadding(new Insets(12));

        UI.col(batchTable, "Batch ID", b -> b.batchId);
        UI.col(batchTable, "Farm", b -> b.farmId);
        UI.col(batchTable, "Crop", b -> b.crop);
        UI.col(batchTable, "Variety", b -> b.variety);
        UI.col(batchTable, "Qty (kg)", b -> String.format("%,.2f", b.quantityKg));
        UI.col(batchTable, "Harvest", b -> b.harvestDate.toString());
        UI.col(batchTable, "Expiry", b -> b.expiryDate.toString());
        VBox batchCard = UI.card(UI.cardTitle("Batches"), batchTable);
        VBox.setVgrow(batchTable, Priority.ALWAYS);
        VBox.setVgrow(batchCard, Priority.ALWAYS);
        VBox tab3 = new VBox(12, batchPane, batchCard);
        VBox.setVgrow(tab3, Priority.ALWAYS);

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("tab-pane");
        tabs.getTabs().addAll(
                new Tab("Register Farm", tab1),
                new Tab("Register Crop", tab2),
                new Tab("Create Batch", tab3)
        );
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        getChildren().addAll(tabs);
    }

    @Override public String title() { return "3  Batches"; }
    @Override public Feature feature() { return Feature.BATCHES; }

    private void registerFarm() {
        // Farm ID is generated by the system.
        sys.registerFarm(sys.nextFarmId(), farmName.getText(), farmLocation.getText(),
                UI.num(farmSize, "Size (ha)"));
        farmName.clear(); farmLocation.clear(); farmSize.clear();
        refresh();
    }

    private void registerCrop() {
        String variety = cropVariety.getValue() == null ? "" : cropVariety.getValue().trim();
        if (variety.isEmpty()) throw new AgriException("A crop variety is required.");
        sys.registerCrop(cropName.getText(), variety, cropCategory.getValue());
        cropName.clear(); cropVariety.setValue(null); cropCategory.setValue(null);
        refresh();
    }

    private void createBatch() {
        String fid = farmSelect.getValue();
        String sel = cropSelect.getValue();
        if (fid == null || sel == null) throw new AgriException("Select a farm and crop.");
        LocalDate harvest = batchHarvest.getValue();
        LocalDate expiry = batchExpiry.getValue();
        if (harvest == null) throw new AgriException("Harvest date is required.");
        if (expiry == null) throw new AgriException("Expiry date is required.");
        String unit = batchUnit.getValue();
        if (unit == null) throw new AgriException("Select a unit.");
        // Variety comes from the selected crop ("Name - Variety"); no separate variety input.
        int sep = sel.indexOf(" - ");
        String crop = sep < 0 ? sel : sel.substring(0, sep);
        String variety = sep < 0 ? "" : sel.substring(sep + 3);
        sys.createHarvestBatch(
                sys.nextBatchId(), fid, "", crop, variety,
                batchWorker.getText(), "",
                harvest, expiry,
                UI.num(batchQty, "Qty"), unit);
        batchWorker.clear(); batchQty.clear();
        batchHarvest.setValue(LocalDate.now());
        batchExpiry.setValue(LocalDate.now().plusDays(7));
        refresh();
    }

    @Override public void refresh() {
        List<Farm> farms = new ArrayList<>();
        for (Farm f : sys.farms) farms.add(f);
        UI.fill(farmTable, farms);

        List<Crop> crops = new ArrayList<>();
        for (Crop c : sys.crops) crops.add(c);
        UI.fill(cropTable, crops);

        DynamicArray<Batch> batches = sys.batches.toSortedArray();
        UI.fill(batchTable, batches);

        List<String> fids = new ArrayList<>();
        for (Farm f : sys.farms) fids.add(f.id);
        UI.setItems(farmSelect, fids);

        List<String> cids = new ArrayList<>();
        for (Crop c : sys.crops) cids.add(c.name + " - " + c.variety);
        UI.setItems(cropSelect, cids);

        // System-generated IDs are shown read-only on their tabs.
        nextFarmId.setText("Next farm ID (system-generated): " + sys.nextFarmId());
        nextBatchId.setText("Next batch ID (system-generated): " + sys.nextBatchId());
    }
}
