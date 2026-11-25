package com.agristock.testprogrammod2to5.modules.Module3;

import com.agristock.testprogrammod2to5.model.AgriException;
import com.agristock.testprogrammod2to5.model.Models.Batch;
import com.agristock.testprogrammod2to5.model.Models.Crop;
import com.agristock.testprogrammod2to5.model.Models.Feature;
import com.agristock.testprogrammod2to5.model.Models.Farm;
import com.agristock.testprogrammod2to5.ds.DynamicArray;
import com.agristock.testprogrammod2to5.ui.View;
import com.agristock.testprogrammod2to5.ui.UI;
import com.agristock.testprogrammod2to5.ui.Ctx;
import com.agristock.testprogrammod2to5.service.AgriStockSystem;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// MODULE 3: Farm, Crop & Harvest Batch Registration (AVL Tree by batch ID)
public class BatchesView extends View {
    private final TableView<Farm> farmTable = UI.table();
    private final TableView<Crop> cropTable = UI.table();
    private final TableView<Batch> batchTable = UI.table();
    private final ComboBox<String> farmSelect = new ComboBox<>();
    private final ComboBox<String> cropSelect = new ComboBox<>();
    private TextField farmName, farmLocation;
    private TextField cropName, cropVariety, cropCategory;
    private TextField batchPlot, batchWorker;
    private TextField batchHarvest, batchExpiry, batchQty, batchUnit;
    private Label nextFarmId, nextBatchId;

    public BatchesView(Ctx ctx) { super(ctx); build(); }

    private void build() {
        header("3 - Farm, Crop & Batch Registration",
                "AVL Tree (batches) for O(log n) search; Singly Linked Lists (farms and crops); system-generated IDs.");

        // ---- Tab 1: Register Farm ----
        UI.Form f1 = new UI.Form();
        f1.wide(nextFarmId = new Label());
        farmName = f1.text("Name");
        farmLocation = f1.text("Location");
        f1.wide(UI.btn("Register Farm", "primary", this::registerFarm));
        f1.wide(UI.btn("Save Changes", "secondary", this::saveChanges));
        f1.wide(UI.btn("Delete Farm", "danger", this::deleteFarm));
        VBox farmPane = new VBox(12, f1.grid);
        farmPane.setPadding(new Insets(12));

        // ---- Tab 2: Register Crop ----
        UI.Form f2 = new UI.Form();
        cropName = f2.text("Crop Name");
        cropVariety = f2.text("Variety");
        cropCategory = f2.text("Category");
        f2.wide(UI.btn("Register Crop", "secondary", this::registerCrop));
        VBox cropPane = new VBox(12, f2.grid);
        cropPane.setPadding(new Insets(12));

        // ---- Tab 3: Create Batch ----
        UI.Form f3 = new UI.Form();
        f3.wide(nextBatchId = new Label());
        f3.add("Farm ID", farmSelect);
        batchPlot = f3.text("Plot");
        f3.add("Crop", cropSelect);
        batchWorker = f3.text("Worker");
        batchHarvest = f3.text("Harvest Date");
        batchExpiry = f3.text("Expiry Date");
        batchQty = f3.text("Qty");
        batchUnit = f3.text("Unit");
        batchUnit.setText("kg");
        f3.wide(UI.btn("Create Batch", "primary", this::createBatch));
        VBox batchPane = new VBox(12, f3.grid);
        batchPane.setPadding(new Insets(12));

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("tab-pane");
        tabs.getTabs().addAll(
                new Tab("Register Farm", farmPane),
                new Tab("Register Crop", cropPane),
                new Tab("Create Batch", batchPane)
        );
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setPrefHeight(360);
        tabs.setMaxHeight(360);

        UI.col(farmTable, "ID", f -> f.id);
        UI.col(farmTable, "Name", f -> f.name);
        UI.col(farmTable, "Location", f -> f.location);

        UI.col(cropTable, "Name", c -> c.name);
        UI.col(cropTable, "Variety", c -> c.variety);
        UI.col(cropTable, "Category", c -> c.category);

        UI.col(batchTable, "Batch ID", b -> b.batchId);
        UI.col(batchTable, "Farm", b -> b.farmId);
        UI.col(batchTable, "Crop", b -> b.crop);
        UI.col(batchTable, "Variety", b -> b.variety);
        UI.col(batchTable, "Qty (kg)", b -> String.format("%,.2f", b.quantityKg));
        UI.col(batchTable, "Harvest", b -> b.harvestDate.toString());
        UI.col(batchTable, "Expiry", b -> b.expiryDate.toString());

        VBox farmCard = UI.card(UI.cardTitle("Farms"), farmTable);
        VBox cropCard = UI.card(UI.cardTitle("Crops"), cropTable);
        VBox batchCard = UI.card(UI.cardTitle("Batches (AVL tree)"), batchTable);
        VBox.setVgrow(farmCard, Priority.ALWAYS);
        VBox.setVgrow(cropCard, Priority.ALWAYS);
        VBox.setVgrow(batchCard, Priority.ALWAYS);
        getChildren().addAll(tabs, farmCard, cropCard, batchCard);
    }

    @Override public String title() { return "3  Batches"; }
    @Override public Feature feature() { return Feature.BATCHES; }

    private void registerFarm() {
        // Farm ID is generated by the system.
        sys.registerFarm(sys.nextFarmId(), farmName.getText(), farmLocation.getText());
        farmName.clear(); farmLocation.clear();
        refresh();
    }

    private void saveChanges() {
        UI.info("Save Changes", "Farm changes saved (persistence auto-saves on exit).");
    }

    private void deleteFarm() {
        UI.info("Delete Farm", "Delete farm functionality would remove the selected farm.");
    }

    private void registerCrop() {
        sys.registerCrop(cropName.getText(), cropVariety.getText(), cropCategory.getText());
        cropName.clear(); cropVariety.clear(); cropCategory.clear();
        refresh();
    }

    private void createBatch() {
        String fid = farmSelect.getValue();
        String sel = cropSelect.getValue();
        if (fid == null || sel == null) throw new AgriException("Select a farm and crop.");
        // Variety comes from the selected crop ("Name - Variety"); no separate variety input.
        int sep = sel.indexOf(" - ");
        String crop = sep < 0 ? sel : sel.substring(0, sep);
        String variety = sep < 0 ? "" : sel.substring(sep + 3);
        // Batch ID is generated by the system; farmer linkage is system-managed.
        sys.createHarvestBatch(
                sys.nextBatchId(), fid, batchPlot.getText(), crop, variety,
                batchWorker.getText(), "",
                LocalDate.parse(batchHarvest.getText()), LocalDate.parse(batchExpiry.getText()),
                UI.num(batchQty, "Qty"), batchUnit.getText()
        );
        batchPlot.clear(); batchWorker.clear();
        batchHarvest.clear(); batchExpiry.clear(); batchQty.clear();
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
