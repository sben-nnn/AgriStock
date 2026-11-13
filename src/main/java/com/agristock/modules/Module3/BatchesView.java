package com.agristock.modules.Module3;

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
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

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
    private TextField farmId, farmName, farmLocation;
    private TextField cropName, cropVariety, cropCategory;
    private TextField batchId, batchPlot, batchCrop, batchVariety, batchWorker, batchFarmerId;
    private TextField batchHarvest, batchExpiry, batchQty, batchUnit;

    public BatchesView(Ctx ctx) { super(ctx); build(); }

    private void build() {
        header("3 - Farm, Crop & Batch Registration", "");
        UI.Form f1 = new UI.Form();
        farmId = f1.text("Farm ID");
        farmName = f1.text("Name");
        farmLocation = f1.text("Location");
        f1.wide(UI.btn("Register Farm", "primary", this::registerFarm));

        UI.Form f2 = new UI.Form();
        cropName = f2.text("Crop Name");
        cropVariety = f2.text("Variety");
        cropCategory = f2.text("Category");
        f2.wide(UI.btn("Register Crop", "secondary", this::registerCrop));

        UI.Form f3 = new UI.Form();
        batchId = f3.text("Batch ID");
        f3.add("Farm ID", farmSelect);
        batchPlot = f3.text("Plot");
        f3.add("Crop", cropSelect);
        batchVariety = f3.text("Variety");
        batchWorker = f3.text("Worker");
        batchFarmerId = f3.text("Farmer ID");
        batchHarvest = f3.text("Harvest Date");
        batchExpiry = f3.text("Expiry Date");
        batchQty = f3.text("Qty");
        batchUnit = f3.text("Unit");
        batchUnit.setText("kg");
        f3.wide(UI.btn("Create Batch", "primary", this::createBatch));

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

        getChildren().addAll(
                UI.card(UI.cardTitle("Register Farm"), f1.grid),
                UI.card(UI.cardTitle("Register Crop"), f2.grid),
                UI.card(UI.cardTitle("Create Batch"), f3.grid),
                UI.card(UI.cardTitle("Farms"), farmTable),
                UI.card(UI.cardTitle("Crops"), cropTable),
                UI.card(UI.cardTitle("Batches (AVL tree)"), batchTable)
        );
    }

    @Override public String title() { return "3  Batches"; }
    @Override public Feature feature() { return Feature.BATCHES; }

    private void registerFarm() {
        sys.registerFarm(farmId.getText(), farmName.getText(), farmLocation.getText());
        farmId.clear(); farmName.clear(); farmLocation.clear();
        refresh();
    }

    private void registerCrop() {
        sys.registerCrop(cropName.getText(), cropVariety.getText(), cropCategory.getText());
        cropName.clear(); cropVariety.clear(); cropCategory.clear();
        refresh();
    }

    private void createBatch() {
        String fid = farmSelect.getValue();
        String cid = cropSelect.getValue();
        if (fid == null || cid == null) throw new AgriException("Select a farm and crop.");
        sys.createHarvestBatch(
                batchId.getText(), fid, batchPlot.getText(), cid, batchVariety.getText(),
                batchWorker.getText(), batchFarmerId.getText(),
                LocalDate.parse(batchHarvest.getText()), LocalDate.parse(batchExpiry.getText()),
                UI.num(batchQty, "Qty"), batchUnit.getText()
        );
        batchId.clear(); batchPlot.clear(); batchVariety.clear(); batchWorker.clear();
        batchFarmerId.clear(); batchHarvest.clear(); batchExpiry.clear(); batchQty.clear();
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
    }
}
