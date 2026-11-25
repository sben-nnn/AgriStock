package com.agristock.modules.Module3;

import com.agristock.service.AgriStockSystem;
import com.agristock.model.AgriException;
import com.agristock.model.Models.Batch;
import com.agristock.model.Models.Crop;
import com.agristock.model.Models.Feature;
import com.agristock.model.Models.Farm;
import com.agristock.ui.View;
import com.agristock.ui.UI;
import com.agristock.ui.Ctx;
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
    private final BatchService svc;
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
    private String editingFarmId, editingCropName, editingCropVariety, editingBatchId;

    // Default crop varieties offered as a selection. The combo is editable so a variety
    // not in the list can still be entered.
    private static final List<String> VARIETIES = List.of(
            "Jasmine", "Diamante", "Carabao", "Yellow", "Long Purple", "Lakatan",
            "IR64", "NSIC Rc 222", "Hass", "Kent", "Granny Smith", "Cavendish",
            "Sweet Corn", "Glutinous", "Other"
    );

    public BatchesView(Ctx ctx) { super(ctx); svc = new BatchService(sys); build(); }

    private void build() {
        header("3 - Farm, Crop & Batch Registration", "");

        // ---- Tab 1: Register Farm (form + list in one tab, nothing blocked) ----
        UI.Form f1 = new UI.Form();
        f1.wide(nextFarmId = new Label());
        farmName = f1.text("Name", "e.g. Sunrise Farm");
        farmLocation = f1.text("Location", "e.g. Brgy. San Isidro");
        farmSize = f1.text("Size (ha)", "e.g. 2.5");
        f1.wide(UI.buttons(
                UI.btn("Register Farm", "primary", this::registerFarm),
                UI.btn("Save Changes", "secondary", this::editFarm),
                UI.btn("Delete Farm", "danger", this::deleteFarm)));
        VBox farmPane = new VBox(12, f1.grid);
        farmPane.setPadding(new Insets(12));

        UI.col(farmTable, "ID", f -> f.id);
        UI.col(farmTable, "Name", f -> f.name);
        UI.col(farmTable, "Location", f -> f.location);
        UI.col(farmTable, "Size (ha)", f -> String.format("%.2f", f.size));
        farmTable.getSelectionModel().selectedItemProperty().addListener((o, a, f) -> {
            if (f == null) return;
            editingFarmId = f.id;
            farmName.setText(f.name); farmLocation.setText(f.location); farmSize.setText(String.valueOf(f.size));
        });
        VBox farmCard = UI.card(UI.cardTitle("Farms"), farmTable);
        VBox.setVgrow(farmTable, Priority.ALWAYS);
        VBox.setVgrow(farmCard, Priority.ALWAYS);
        VBox tab1 = new VBox(12, farmPane, farmCard);
        VBox.setVgrow(tab1, Priority.ALWAYS);

        // ---- Tab 2: Register Crop (form + list in one tab) ----
        UI.Form f2 = new UI.Form();
        cropName = f2.text("Crop Name", "e.g. Rice");
        cropVariety = new ComboBox<>(FXCollections.observableArrayList(VARIETIES));
        cropVariety.setEditable(true);
        cropVariety.setPromptText("Select or type a variety");
        f2.add("Variety", cropVariety);
        cropCategory = f2.combo("Category", Arrays.asList(AgriStockSystem.CATEGORIES));
        f2.wide(UI.buttons(
                UI.btn("Register Crop", "primary", this::registerCrop),
                UI.btn("Save Changes", "secondary", this::editCrop),
                UI.btn("Delete Crop", "danger", this::deleteCrop)));
        VBox cropPane = new VBox(12, f2.grid);
        cropPane.setPadding(new Insets(12));

        UI.col(cropTable, "Name", c -> c.name);
        UI.col(cropTable, "Variety", c -> c.variety);
        UI.col(cropTable, "Category", c -> c.category);
        cropTable.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> {
            if (c == null) return;
            editingCropName = c.name; editingCropVariety = c.variety;
            cropName.setText(c.name); cropVariety.setValue(c.variety); cropCategory.setValue(c.category);
        });
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
        batchWorker = f3.text("Worker", "e.g. Ramon Santos");
        batchHarvest = f3.date("Harvest Date", LocalDate.now());
        batchExpiry = f3.date("Expiry Date", LocalDate.now().plusDays(7));
        batchQty = f3.text("Qty", "e.g. 1000");
        batchUnit = f3.combo("Unit", Arrays.asList(AgriStockSystem.UNITS));
        batchUnit.setValue("kg");
        f3.wide(UI.buttons(
                UI.btn("Create Batch", "primary", this::createBatch),
                UI.btn("Save Changes", "secondary", this::editBatch),
                UI.btn("Delete Batch", "danger", this::deleteBatch)));
        VBox batchPane = new VBox(12, f3.grid);
        batchPane.setPadding(new Insets(12));

        UI.col(batchTable, "Batch ID", b -> b.batchId);
        UI.col(batchTable, "Farm", b -> b.farmId);
        UI.col(batchTable, "Crop", b -> b.crop);
        UI.col(batchTable, "Variety", b -> b.variety);
        UI.col(batchTable, "Qty (kg)", b -> String.format("%,.2f", b.quantityKg));
        UI.col(batchTable, "Harvest", b -> b.harvestDate.toString());
        UI.col(batchTable, "Expiry", b -> b.expiryDate.toString());
        batchTable.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b == null) return;
            editingBatchId = b.batchId;
            farmSelect.setValue(b.farmId);
            cropSelect.setValue(b.crop + " - " + b.variety);
            batchWorker.setText(b.worker);
            batchHarvest.setValue(b.harvestDate); batchExpiry.setValue(b.expiryDate);
            batchQty.setText(String.valueOf(b.originalQty)); batchUnit.setValue(b.unit);
        });
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
        svc.registerFarm(svc.nextFarmId(), farmName.getText(), farmLocation.getText(),
                UI.num(farmSize, "Size (ha)"));
        clearFarmForm(); refreshAll();
    }

    private void registerCrop() {
        String variety = cropVariety.getValue() == null ? "" : cropVariety.getValue().trim();
        if (variety.isEmpty()) throw new AgriException("A crop variety is required.");
        svc.registerCrop(cropName.getText(), variety, cropCategory.getValue());
        clearCropForm(); refreshAll();
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
        svc.createBatch(
                svc.nextBatchId(), fid, crop, variety,
                batchWorker.getText(),
                harvest, expiry,
                UI.num(batchQty, "Qty"), unit);
        clearBatchForm(); refreshAll();
    }

    private void clearFarmForm() {
        editingFarmId = null;
        farmName.clear(); farmLocation.clear(); farmSize.clear();
        farmTable.getSelectionModel().clearSelection();
    }

    private void clearCropForm() {
        editingCropName = editingCropVariety = null;
        cropName.clear(); cropVariety.setValue(null); cropCategory.setValue(null);
        cropTable.getSelectionModel().clearSelection();
    }

    private void clearBatchForm() {
        editingBatchId = null;
        batchWorker.clear(); batchQty.clear();
        batchHarvest.setValue(LocalDate.now()); batchExpiry.setValue(LocalDate.now().plusDays(7));
        batchTable.getSelectionModel().clearSelection();
    }

    private void editFarm() {
        if (editingFarmId == null) throw new AgriException("Select a farm to edit.");
        svc.editFarm(editingFarmId, farmName.getText(), farmLocation.getText(), UI.num(farmSize, "Size (ha)"));
        clearFarmForm(); refreshAll();
    }

    private void deleteFarm() {
        if (editingFarmId == null) throw new AgriException("Select a farm to delete.");
        if (!UI.confirm("Delete farm " + editingFarmId + "?")) return;
        svc.deleteFarm(editingFarmId);
        clearFarmForm(); refreshAll();
    }

    private void editCrop() {
        if (editingCropName == null) throw new AgriException("Select a crop to edit.");
        String variety = cropVariety.getValue() == null ? "" : cropVariety.getValue().trim();
        svc.editCrop(editingCropName, editingCropVariety, cropCategory.getValue(),
                cropName.getText(), variety, cropCategory.getValue());
        clearCropForm(); refreshAll();
    }

    private void deleteCrop() {
        if (editingCropName == null) throw new AgriException("Select a crop to delete.");
        if (!UI.confirm("Delete crop " + editingCropName + " - " + editingCropVariety + "?")) return;
        svc.deleteCrop(editingCropName, editingCropVariety);
        clearCropForm(); refreshAll();
    }

    private void editBatch() {
        if (editingBatchId == null) throw new AgriException("Select a batch to edit.");
        String fid = farmSelect.getValue();
        String sel = cropSelect.getValue();
        if (fid == null || sel == null) throw new AgriException("Select a farm and crop.");
        String unit = batchUnit.getValue();
        if (unit == null) throw new AgriException("Select a unit.");
        int sep = sel.indexOf(" - ");
        String crop = sep < 0 ? sel : sel.substring(0, sep);
        String variety = sep < 0 ? "" : sel.substring(sep + 3);
        svc.editBatch(editingBatchId, fid, crop, variety, batchWorker.getText(),
                batchHarvest.getValue(), batchExpiry.getValue(), UI.num(batchQty, "Qty"), unit);
        clearBatchForm(); refreshAll();
    }

    private void deleteBatch() {
        if (editingBatchId == null) throw new AgriException("Select a batch to delete.");
        if (!UI.confirm("Delete batch " + editingBatchId + "?")) return;
        svc.deleteBatch(editingBatchId);
        clearBatchForm(); refreshAll();
    }

    @Override public void refresh() {
        UI.fill(farmTable, svc.getAllFarms());
        UI.fill(cropTable, svc.getAllCrops());
        UI.fill(batchTable, svc.getAllBatches());

        List<String> fids = new ArrayList<>();
        for (Farm f : svc.getAllFarms()) fids.add(f.id);
        UI.setItems(farmSelect, fids);

        List<String> cids = new ArrayList<>();
        for (Crop c : svc.getAllCrops()) cids.add(c.name + " - " + c.variety);
        UI.setItems(cropSelect, cids);

        // System-generated IDs are shown read-only on their tabs.
        nextFarmId.setText("Next farm ID (system-generated): " + svc.nextFarmId());
        nextBatchId.setText("Next batch ID (system-generated): " + svc.nextBatchId());
    }
}
