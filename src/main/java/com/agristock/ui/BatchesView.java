package com.agristock.ui;

import com.agristock.model.Models.*;
import com.agristock.service.AgriStockSystem;
import com.agristock.service.Fmt;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** MODULE 3: Farm, Crop & Harvest Batch Registration (AVL tree keyed by batch ID). */
public class BatchesView extends View {
    private final TableView<Batch> batches = UI.table();
    private final TableView<Farm> farms = UI.table();
    private final TableView<Crop> crops = UI.table();
    private final TextField batchId, plot, worker, qty, farmId, farmName, farmLoc, cropName, cropVar;
    private final ComboBox<String> farm, crop, farmer, unit, cropCat;
    private final DatePicker harvest, expiry;
    private final Label preview = UI.hint("");

    public BatchesView(Ctx ctx) {
        super(ctx);
        header("3 - Farm, Crop & Harvest Batch Registration", "Batch IDs are unique: the AVL tree searches for duplicates, inserts, and rebalances with rotations.");

        UI.Form b = new UI.Form();
        batchId = b.text("Batch ID", "e.g. B-1007");
        b.wide(UI.btn("Generate next ID", "ghost", () -> batchId.setText(sys.nextBatchId())));
        farm = b.combo("Farm / field", new ArrayList<>());
        plot = b.text("Plot");
        crop = b.combo("Crop", new ArrayList<>());
        farmer = b.combo("Farmer", new ArrayList<>());
        worker = b.text("Worker");
        harvest = b.date("Harvest date", LocalDate.now());
        expiry = b.date("Expiry date", LocalDate.now().plusDays(7));
        qty = b.text("Quantity");
        unit = b.combo("Unit", Arrays.asList(AgriStockSystem.UNITS)); unit.setValue("kg");
        b.wide(preview);
        qty.textProperty().addListener((o, a, v) -> updatePreview());
        unit.valueProperty().addListener((o, a, v) -> updatePreview());
        b.wide(UI.btn("Create harvest batch", "primary", this::create));

        UI.Form fm = new UI.Form();
        farmId = fm.text("Farm ID", "e.g. FARM-C"); farmName = fm.text("Name"); farmLoc = fm.text("Location");
        fm.wide(UI.btn("Register farm", "primary", () -> { sys.registerFarm(farmId.getText(), farmName.getText(), farmLoc.getText()); farmId.clear(); farmName.clear(); farmLoc.clear(); refresh(); }));

        UI.Form cm = new UI.Form();
        cropName = cm.text("Crop name"); cropVar = cm.text("Variety");
        cropCat = cm.combo("Category", Arrays.asList("Grain", "Vegetable", "Fruit", "Seed", "Other"));
        cm.wide(UI.btn("Register crop", "primary", () -> { sys.registerCrop(cropName.getText(), cropVar.getText(), cropCat.getValue()); cropName.clear(); cropVar.clear(); refresh(); }));

        Accordion acc = new Accordion(new TitledPane("Create harvest batch", b.grid), new TitledPane("Register farm / field", fm.grid), new TitledPane("Register crop", cm.grid));
        acc.setExpandedPane(acc.getPanes().get(0));

        UI.col(batches, "Batch ID", x -> x.batchId);
        UI.col(batches, "Crop", x -> x.crop + " - " + x.variety);
        UI.col(batches, "Farm", x -> x.farmId);
        UI.col(batches, "Plot", x -> x.plot);
        UI.col(batches, "Worker", x -> x.worker);
        UI.col(batches, "Harvested", x -> x.harvestDate.toString());
        UI.col(batches, "Expiry", x -> x.expiryDate.toString());
        UI.col(batches, "Qty (kg)", x -> Fmt.kg(x.quantityKg));
        UI.col(batches, "Status", x -> x.status.name());
        UI.col(farms, "Farm ID", x -> x.id); UI.col(farms, "Name", x -> x.name); UI.col(farms, "Location", x -> x.location);
        UI.col(crops, "Crop", x -> x.name); UI.col(crops, "Variety", x -> x.variety); UI.col(crops, "Category", x -> x.category);
        TabPane tabs = new TabPane(new Tab("Batches (sorted by ID)", batches), new Tab("Farms", farms), new Tab("Crops", crops));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox right = UI.card(tabs);
        VBox.setVgrow(tabs, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(UI.split(new VBox(acc), right, 360));
    }

    @Override public String title() { return "3  Batch Registration"; }
    @Override public Feature feature() { return Feature.BATCHES; }

    private void updatePreview() {
        try {
            double q = Double.parseDouble(qty.getText().trim());
            preview.setText("= " + Fmt.kg(AgriStockSystem.convertUnit(q, unit.getValue() == null ? "kg" : unit.getValue())) + " kg after unit conversion");
        } catch (NumberFormatException e) { preview.setText(""); }
    }

    private void create() {
        String id = batchId.getText();
        Crop c = findCrop(crop.getValue());
        String farmerId = farmer.getValue() == null || farmer.getValue().startsWith("(none") ? "" : UI.idOf(farmer);
        sys.createHarvestBatch(id, UI.idOf(farm), plot.getText(), c == null ? null : c.name, c == null ? "" : c.variety, worker.getText(),
                farmerId, harvest.getValue(), expiry.getValue(), UI.num(qty, "Quantity"), unit.getValue());
        batchId.clear(); qty.clear(); plot.clear(); worker.clear();
        refresh();
        UI.info("Batch registered", "Batch " + id.trim() + " was inserted into the AVL tree (tree height now " + sys.batches.height() + ").");
    }

    private Crop findCrop(String label) {
        if (label == null) return null;
        for (Crop c : sys.crops) if (c.toString().equals(label)) return c;
        return null;
    }

    @Override public void refresh() {
        UI.fill(batches, sys.batches.toSortedArray());
        UI.fill(farms, sys.farms); UI.fill(crops, sys.crops);
        List<String> f = new ArrayList<>(); for (Farm x : sys.farms) f.add(x.id + " | " + x.name);
        List<String> c = new ArrayList<>(); for (Crop x : sys.crops) c.add(x.toString());
        List<String> fr = new ArrayList<>(); fr.add("(none)"); for (Farmer x : sys.farmers) fr.add(x.id + " | " + x.name);
        UI.setItems(farm, f); UI.setItems(crop, c); UI.setItems(farmer, fr);
    }
}
