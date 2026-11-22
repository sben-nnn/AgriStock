# AgriStock — Step-by-Step Test Run Manual

This manual shows you how to open the program, click through every screen, and check that each part works. You do not need to know how to program.

**Project folder:** `D:\AgriStock`

---

## Contents

1. [Before you start](#1-before-you-start)
2. [Start the program](#2-start-the-program)
3. [Sign in](#3-sign-in)
4. [Quick demo using sample data (5 minutes)](#4-quick-demo-using-sample-data-5-minutes)
5. [Full test from scratch (type the data yourself)](#5-full-test-from-scratch-type-the-data-yourself)
6. [Test the user roles](#6-test-the-user-roles)
7. [Save and restart test](#7-save-and-restart-test)
8. [Test checklist](#8-test-checklist)
9. [Troubleshooting](#9-troubleshooting)
10. [Known quirks](#10-known-quirks)

---

## 1. Before you start

You need:

- A Windows PC.
- **Java 17 or newer.** To check, open Command Prompt and type:

  ```text
  java -version
  ```

  You should see `17` or a higher number.

- **IntelliJ IDEA** (recommended) or **Maven** installed.
- The project folder `D:\AgriStock`.

> **Important:** The program saves your data in `D:\AgriStock\data\agristock_state.csv`.
> For the full test in Part 5, start with this file removed or renamed so old test data does not interfere.

---

## 2. Start the program

### Way A — IntelliJ IDEA (easiest)

1. Open **IntelliJ IDEA**.
2. Click **File → Open**.
3. Select the folder `D:\AgriStock` and click **OK**.
4. Wait for the progress bar at the bottom to finish. The first time may take a few minutes because Maven downloads JavaFX.
5. On the right side, click **Maven** (or go to **View → Tool Windows → Maven**).
6. Expand **agristock → Plugins → javafx**.
7. Double-click **javafx:run**.
8. Wait for the **AgriStock** login window to appear.

### Way B — Command line

1. Open **PowerShell**.
2. Go to the project folder:

   ```powershell
   cd D:\AgriStock
   ```

3. Run:

   ```powershell
   mvn javafx:run
   ```

4. The first run may take a minute.

> If you see **“mvn is not recognized”**, use Way A, or install Maven and add it to your PATH.

---

## 3. Sign in

Use these default accounts:

| Username | Password | Role | What they can use |
|---|---|---|---|
| `admin` | `admin123` | Farm Owner | Everything |
| `manager` | `manager123` | Farm Manager | Everything except Users & Roles |
| `inventory` | `inventory123` | Inventory Staff | Farm, batch, stock, inspection, storage, expiry, spoilage, audit |
| `sales` | `sales123` | Sales / Dispatch | Sales, dispatch, expiry, spoilage |

**Steps:**

1. Type Username: `admin`
2. Type Password: `admin123`
3. Click **Sign in**.

**You should see:**
- The main window.
- A left menu with numbered screens (`0 Dashboard` up to `14 Audit & Validation`).
- Your name and role at the top right: `Farm Owner (default) • Farm Owner`.

> The Dashboard is currently a simple landing page. The real work happens in the numbered screens.

---

## 4. Quick demo using sample data (5 minutes)

Use this if you only want to see the screens working. This is the fastest way to test the whole program.

1. Sign in as `admin` / `admin123`.
2. Click **Load sample data** at the top right.
3. Click **Yes** on the confirmation.
4. You should see a message: **“Sample data loaded.”**
5. Click each menu item and check that the tables are not empty:

| Screen | What you should see |
|---|---|
| **2 Farmers** | 3 farmers: Juan Dela Cruz, Maria Santos, Pedro Reyes |
| **3 Batches** | 6 batches: B-1001 to B-1006 |
| **4 Harvest Stock** | 6 stock records |
| **5 Inspection** | B-1006 is still waiting in the queue |
| **6 Storage** | 4 storage entries |
| **7 Stock** | 5 stock items, including B-1005 |
| **8 Expiry** | B-1005 shows **EXPIRED** |
| **9 Sales** | SO-0001 and SO-0002 are confirmed; SO-0003 is pending |
| **10 Dispatch** | 2 upcoming deliveries |
| **11 Spoilage & Loss** | 1 approved loss |
| **12 Forecasting** | Rice and Tomato history records |
| **13 Reports & Analytics** | Click **Generate** to see report rows |
| **14 Audit & Validation** | The audit trail has entries |

**Try these quick actions:**

- **Expiry screen:** click **Check & Purge Expired Stock**, then **Yes**. B-1005 is removed because it is expired.
- **Inspection screen:** click **Inspect Front Batch** with status `ACCEPTED`, grade `A`, unit value `50`. B-1006 leaves the queue and appears in stock.
- **Forecasting screen:** choose crop `Rice`, window `3`, seed ratio `12`, seed cost `80`, then click **Forecast seed need**.
- **Reports screen:** choose report `Inventory`, then click **Generate**.

> **Warning:** **Load sample data replaces all current data**, even though the confirmation says “only works on an empty system.” Do not click it after you have typed your own data unless you want to erase it.

---

## 5. Full test from scratch (type the data yourself)

Follow these steps **in order**. Each screen uses data from the previous screen.

### Start clean first

1. Close AgriStock.
2. Open File Explorer and go to `D:\AgriStock\data`.
3. Rename `agristock_state.csv` to `agristock_state.backup.csv` (or delete it).
4. Start AgriStock again.
5. Sign in as `admin` / `admin123`.

The program will recreate the four default accounts.

> If your batch numbers do not start at `B-1001`, the data file was not empty. Close the program, remove the CSV, and start again.

---

### Step 1 — Users & Roles (screen 1)

**Add a test user**

| Field | Type this |
|---|---|
| Username | `tester` |
| Full name | `Test User` |
| Password | `test123` |
| Role | `Inventory Staff` |

Click **Add user**.

**You should see:** `tester` appears in the Registered users table with role `Inventory Staff` and status `Active`.

**Test update**

1. Click the `tester` row to select it.
2. Change **Full name** to `Test User Updated`.
3. Leave **Password** blank.
4. Click **Update details**.

**You should see:** the table shows `Test User Updated`.

**Test role change**

1. Select the `tester` row.
2. Choose **Role** = `Inventory Staff`.
3. Click **Assign role**.

**You should see:** the table shows `tester` with role `Inventory Staff`. You will use this account in the role tests in Part 6.

**Test deactivate / activate**

1. Select the `tester` row.
2. Click **Deactivate / Activate**.
3. The status changes to `Deactivated`.
4. Click **Deactivate / Activate** again to make it `Active`.

**Test delete protection**

1. Select the `admin` row.
2. Click **Delete user**.

**You should see:** an error saying you cannot delete your own account.

> Keep the `tester` account for the role tests in Part 6. Do not delete it yet.

---

### Step 2 — Farmers (screen 2)

**Register two farmers**

| Farmer ID | Name | Contact |
|---|---|---|
| `FR-2001` | `Juan Dela Cruz` | `0917-111-2222` |
| `FR-2002` | `Maria Santos` | `0918-333-4444` |

For each row: type the values and click **Register Farmer**.

**You should see:** both farmers in the Farmers table.

**Record harvests**

| Farmer ID | Crop | Qty (kg) | Date |
|---|---|---|---|
| `FR-2001` | `Rice` | `1000` | `2026-11-01` |
| `FR-2002` | `Tomato` | `300` | `2026-11-10` |

For each row:

1. Choose the **Farmer ID** in the Record Harvest section.
2. Type Crop, Qty, and Date.
3. Click **Record Harvest**.

**You should see:** the Harvest Logs table shows the crop, quantity, and date for the selected farmer. The Farmers table shows Total Harvest `1,000.00 kg` and `300.00 kg`.

**Test delete**

1. Register a temporary farmer `FR-2999` / `Delete Me` / `0000`.
2. Select `FR-2999` in the table.
3. Click **Delete Farmer**, then **Yes**.

**You should see:** `FR-2999` disappears.

> Dates must be typed as `YYYY-MM-DD`. Use a dot for decimals, for example `13.5`, not `13,5`.

---

### Step 3 — Batches (screen 3)

This screen has three tabs: **Register Farm**, **Register Crop**, and **Create Batch**.

#### Tab 1: Register Farm

| Field | Type this |
|---|---|
| Name | `Sunrise Farm` |
| Location | `Nueva Ecija` |
| Size (ha) | `3.5` |

Click **Register Farm**.

**You should see:** the Farm ID is generated automatically as `FARM-1001`. The Farms table shows Sunrise Farm.

#### Tab 2: Register Crop

Add two crops:

| Crop Name | Variety | Category |
|---|---|---|
| `Rice` | `Jasmine` | `Grain` |
| `Tomato` | `Diamante` | `Vegetable` |

For each row:

1. Type the Crop Name.
2. Choose or type the Variety.
3. Type the Category.
4. Click **Register Crop**.

**You should see:** both crops in the Crops table.

#### Tab 3: Create Batch

Create batch 1:

| Field | Type this |
|---|---|
| Farm ID | `FARM-1001` |
| Plot | `Plot 1` |
| Crop | `Rice - Jasmine` |
| Worker | `Ramon` |
| Harvest Date | `2026-11-01` |
| Expiry Date | `2035-12-31` |
| Qty | `1000` |
| Unit | `kg` |

Click **Create Batch**.

Create batch 2:

| Field | Type this |
|---|---|
| Farm ID | `FARM-1001` |
| Plot | `Plot 2` |
| Crop | `Tomato - Diamante` |
| Worker | `Liza` |
| Harvest Date | `2026-11-10` |
| Expiry Date | `2034-12-31` |
| Qty | `300` |
| Unit | `kg` |

Click **Create Batch**.

**You should see:** batches `B-1001` and `B-1002` in the Batches table.

**Test the rules**

- Try to create a batch with an expiry date **before** the harvest date.
  **You should see:** “Expiry date must be after the harvest date.”
- Try to create a batch with Qty `0`.
  **You should see:** “Quantity must be greater than zero.”

> The batch ID is generated by the system. Do not type it.

---

### Step 4 — Harvest Stock (screen 4)

Record stock for batch 1:

| Field | Type this |
|---|---|
| Batch ID | `B-1001` |
| Harvested Qty (kg) | `1000` |
| Rejected Qty (kg) | `50` |
| Grade | `A` |
| Ripeness | `N/A` |
| Moisture (%) | `13.5` |
| Reject Reason | `Broken grains` |

Click **Record Stock**.

Record stock for batch 2:

| Field | Type this |
|---|---|
| Batch ID | `B-1002` |
| Harvested Qty (kg) | `300` |
| Rejected Qty (kg) | `0` |
| Grade | `A` |
| Ripeness | `Ripe` |
| Moisture (%) | `90` |
| Reject Reason | *(leave blank)* |

Click **Record Stock**.

**You should see:**

| Batch | Harvested | Rejected | Saleable | Grade | Moisture |
|---|---|---|---|---|---|
| B-1001 | 1,000.00 | 50.00 | 950.00 | A | 13.5 |
| B-1002 | 300.00 | 0.00 | 300.00 | A | 90.0 |

**Test the rules**

- Try to record `B-1001` again.
  **You should see:** “Batch stock entry already logged.”
- Try rejected quantity greater than harvested quantity.
  **You should see:** “Rejected quantity cannot exceed harvested quantity.”
- Try rejected quantity above zero with a blank reason.
  **You should see:** “Enter a reason for the rejected/damaged produce.”

> Recording stock automatically puts the batch in the inspection queue.

---

### Step 5 — Inspection (screen 5)

The queue works **first in, first out**: the batch that entered first is inspected first.

**Inspect B-1001**

| Field | Type this |
|---|---|
| Status | `ACCEPTED` |
| Grade | `A` |
| Notes | *(leave blank)* |
| Unit Value (per kg) | `60` |

Click **Inspect Front Batch**.

**Inspect B-1002**

| Field | Type this |
|---|---|
| Status | `ACCEPTED` |
| Grade | `A` |
| Notes | *(leave blank)* |
| Unit Value (per kg) | `90` |

Click **Inspect Front Batch**.

**You should see:**

- The Inspection Queue becomes empty.
- The Inspected Batches table shows B-1001 and B-1002 as `ACCEPTED` with grade `A`.
- The Stock screen now has B-1001 and B-1002.

**Test the rules**

- Click **Inspect Front Batch** again with an empty queue.
  **You should see:** “The inspection queue is empty.”
- Choose `DOWNGRADED` or `REJECTED` and leave Notes blank.
  **You should see:** “A justification is required for downgraded or rejected batches.”
- Choose `ACCEPTED` and leave Unit Value blank.
  **You should see:** “Enter an estimated unit value (per kg) greater than zero.”

> `ACCEPTED` and `DOWNGRADED` batches go into stock. `REJECTED` batches do not.

---

### Step 6 — Storage (screen 6)

Assign B-1001:

| Field | Type this |
|---|---|
| Batch / Item | `B-1001` |
| Location | `1. Cold Room A` |
| Qty (kg) | `900` |
| Storage Date | leave the default (today) |
| Conditions | `22°C, 60% RH` |

Click **Assign Location**.

Assign B-1002:

| Field | Type this |
|---|---|
| Batch / Item | `B-1002` |
| Location | `2. Cold Room B` |
| Qty (kg) | `285` |
| Storage Date | leave the default (today) |
| Conditions | `10°C, 90% RH` |

Click **Assign Location**.

**You should see:**

- Storage Entries table: B-1001 at Cold Room A, 900 kg; B-1002 at Cold Room B, 285 kg.
- Stock Storage Status: B-1001 stored 900, remaining 950; B-1002 stored 285, remaining 300.

**Test FIFO rotation**

1. Click **Rotate Stock FIFO (oldest first)**.
2. Click **Yes**.

**You should see:** B-1001 moves to the back of the table and its Rotations becomes `1`. B-1002 is now first.

**Test the storage limit**

- Try to store `100` kg of B-1001.
  **You should see:** “Only 50.00 kg of this batch is not yet stored.”

---

### Step 7 — Stock (screen 7)

**Check the stock table**

You should see:

| Item | Crop | Remaining | Available | Unit Value | Flags | Location |
|---|---|---|---|---|---|---|
| B-1001 | Rice | 950.00 | 950.00 | 60.00 | Aging | Cold Room A |
| B-1002 | Tomato | 300.00 | 300.00 | 90.00 | *(blank)* | Cold Room B |

**Test search**

1. Type `B-1001` in the **Search** box.
2. Click **Apply Filters**.

**You should see:** only B-1001.

Clear the search box and click **Apply Filters** again.

**Test category filter**

1. Choose **Category** = `Grain`.
2. Click **Apply Filters**.

**You should see:** only B-1001 (Rice is Grain).

Set Category back to `All` and click **Apply Filters**.

**Test reserve / release**

1. Choose **Item** = `B-1001`.
2. Type Reserve `50`.
3. Click **Reserve**.

**You should see:** Available becomes `900.00`.

4. Type Release `50`.
5. Click **Release**.

**You should see:** Available returns to `950.00`.

**Test the reserve limit**

- Try to reserve `100000`.
  **You should see:** “Only 950.00 kg available to reserve.”

**Add a manual stock item**

| Field | Type this |
|---|---|
| Item ID | `M-2001` |
| Crop | `Corn` |
| Category | `Grain` |
| Qty (kg) | `100` |
| Expiry (YYYY-MM-DD) | `2035-01-01` |
| Unit Value | `25` |

Click **Add Stock Item**.

**You should see:** `M-2001` appears with remaining 100, available 100, unit value 25.

**Test the rules**

- Try to add the same `M-2001` again.
  **You should see:** “Item ID already exists in stock.”
- Try an expiry date in the past.
  **You should see:** “Enter an expiry date that is not in the past.”

---

### Step 8 — Expiry (screen 8)

**Check the tables**

The **Stock by Expiry (earliest first)** table should show:

| Item | Crop | Expiry | Remaining | Status |
|---|---|---|---|---|
| B-1002 | Tomato | 2034-12-31 | 300.00 | Fresh |
| M-2001 | Corn | 2035-01-01 | 100.00 | Fresh |
| B-1001 | Rice | 2035-12-31 | 950.00 | Fresh |

The **Expired Stock** table should be empty.

**Test purge with no expired stock**

1. Click **Check & Purge Expired Stock**.
2. Click **Yes**.

**You should see:** “Purged expired stock: (none)”.

**Rebuild the expiry heap**

- Click **Rebuild Expiry Heap**. Nothing visible changes; this only rebuilds the internal expiry order.

> To test purging real expired stock, use the sample data in Part 4. Batch B-1005 is already expired.

---

### Step 9 — Sales (screen 9)

**Create sale order 1**

| Field | Type this |
|---|---|
| Buyer | `Metro Market` |
| Item / Batch | `B-1001` |
| Qty (kg) | `200` |
| Unit Price | `62` |
| Reference No. | *(leave blank)* |

Click **Create Sale Order**.

**Create sale order 2**

| Field | Type this |
|---|---|
| Buyer | `Fresh Mart` |
| Item / Batch | `B-1002` |
| Qty (kg) | `80` |
| Unit Price | `95` |
| Reference No. | *(leave blank)* |

Click **Create Sale Order**.

**You should see:**

- Pending Sales Queue shows `SO-0001` and `SO-0002`.
- SO-0001 total is `12,400.00`; SO-0002 total is `7,600.00`.
- B-1001 available drops to 750; B-1002 available drops to 220 (the stock is reserved, not yet sold).

**Process the queue**

1. Click **Process All Pending (FIFO)**.

**You should see:**

- Message: “Confirmed 2 sale order(s) and released stock.”
- Pending queue is empty.
- Sales Records shows both orders as `CONFIRMED`.
- B-1001 remaining becomes 750; B-1002 remaining becomes 220.

**Test the rules**

- Create a sale for B-1001 with Qty `99999`.
  **You should see:** “Only 750.00 kg of B-1001 is available (rest is reserved or sold).”
- Create a sale with Reference No. `SO-0001`.
  **You should see:** “Reference number already used.”

---

### Step 10 — Dispatch (screen 10)

**Schedule delivery for SO-0001**

| Field | Type this |
|---|---|
| Sale Order | `SO-0001` |
| Deadline | pick a date 2 days from today |
| Vehicle | `Truck TRK-101` |
| Destination | `Metro Market, Quezon City` |
| Urgency | `1 - High` |

Click **Schedule Delivery**.

**Schedule delivery for SO-0002**

| Field | Type this |
|---|---|
| Sale Order | `SO-0002` |
| Deadline | pick a date 5 days from today |
| Vehicle | `Van VAN-205` |
| Destination | `Fresh Mart, Manila` |
| Urgency | `2 - Normal` |

Click **Schedule Delivery**.

**You should see:**

- Upcoming Deliveries shows `DL-0001` first (earlier deadline), then `DL-0002`.
- Both have status `SCHEDULED`.

**Test receipt before dispatch**

1. Choose Delivery `DL-0002`.
2. Type Received Qty `80`.
3. Click **Confirm Receipt**.

**You should see:** “Select a dispatched delivery.”

**Dispatch the earliest delivery**

1. Click **Record Dispatch (EDF)**.

**You should see:** `DL-0001` becomes `DISPATCHED`, with dispatched quantity 200 and today’s date. It moves to Delivery History.

**Confirm receipt for DL-0001**

1. Choose Delivery `DL-0001`.
2. Type Received Qty `190`.
3. Click **Confirm Receipt**.

**You should see:** `DL-0001` becomes `RECEIVED`, received quantity 190. Because 10 kg is missing, the program creates an approved loss record (SP-0001, “Delivery shortage”).

**Finish DL-0002**

1. Click **Record Dispatch (EDF)** again.
2. Choose Delivery `DL-0002`.
3. Type Received Qty `80`.
4. Click **Confirm Receipt**.

**You should see:** `DL-0002` becomes `RECEIVED`. Upcoming Deliveries is empty; Delivery History shows both deliveries.

> **Record Dispatch always takes the delivery with the earliest deadline.** It does not use the selected row.

---

### Step 11 — Spoilage & Loss (screen 11)

**Log a loss**

| Field | Type this |
|---|---|
| Batch | `B-1002` |
| Quantity (kg) | `12` |
| Reason | `Spoiled during transport` |
| Reporter | `admin` |

Click **Log spoilage / request adjustment**.

**You should see:**

- Message: “SP-0002 is APPROVED.”
- The table shows SP-0002 with status `APPROVED` and loss value `₱1,080.00` (12 kg × ₱90).
- B-1002 remaining drops from 220 to 208.

> SP-0001 came from the delivery shortage in Step 10. If you received the full 200 kg instead, this new entry would be SP-0001.

**View the financial loss report**

- Click **Financial loss report**.

**You should see:**

| Loss | Quantity | Value |
|---|---|---|
| SP-0001 (delivery shortage) | 10.00 kg | ₱600.00 |
| SP-0002 (spoilage) | 12.00 kg | ₱1,080.00 |
| **Total** | **22.00 kg** | **₱1,680.00** |

**Test the rules**

- Try Quantity `99999`.
  **You should see:** “Only 208.00 kg of B-1002 is available (rest is reserved or sold).”
- Try a blank reason.
  **You should see:** “A reason is required.”

> Owners and managers approve their own requests immediately. Inventory and Sales users create `PENDING` requests that an owner or manager must approve.

---

### Step 12 — Forecasting (screen 12)

**Add three Rice history records**

| Crop | Season | Yield (kg) | Demand (kg) |
|---|---|---|---|
| `Rice` | `2024 Dry` | `4200` | `4000` |
| `Rice` | `2024 Wet` | `4500` | `4300` |
| `Rice` | `2025 Dry` | `4100` | `4400` |

For each row:

1. Type or choose the Crop.
2. Type Season, Yield, and Demand.
3. Click **Add history record**.

**Run the forecast**

| Field | Type this |
|---|---|
| Crop | `Rice` |
| Window (seasons) | `3` |
| Seed ratio (%) | `12` |
| Seed cost / kg | `80` |

Click **Forecast seed need**.

**You should see:**

```text
Crop: Rice | seasons on file: 3
Moving average yield (3-season window): 4,266.67 kg
Projected seed need (12%): 512.00 kg
Seasonal seed budget: ₱40,960.00
Moving average demand: 4,233.33 kg
```

The chart should show a Yield line and a Demand line for the three seasons.

**Test the rule**

- Change Window to `5` and click **Forecast seed need**.
  **You should see:** “Insufficient historical data points for a window of 5 seasons.”

Set Window back to `3`.

---

### Step 13 — Reports & Analytics (screen 13)

For each report:

1. Choose the **Report** type.
2. Leave Crop and Farm as `All`.
3. Leave Batch blank and From/To blank.
4. Click **Generate**.

**Expected results**

| Report | Entries | Total Quantity | Total Value |
|---|---|---|---|
| Harvest | 2 | 1,300.00 kg | ₱87,000.00 |
| Inventory | 3 | 1,058.00 kg | ₱66,220.00 |
| Sales | 2 | 280.00 kg | ₱20,000.00 |
| Dispatch | 2 | 280.00 kg | ₱0.00 |
| Loss | 2 | 22.00 kg | ₱1,680.00 |

**Test the date filter**

- Choose Report `Sales`.
- Set **From** = the date you created the sales (today).
- Set **To** = today.
- Click **Generate**.

**You should see:** the two sales rows.

**Test the analytics buttons**

| Button | What you should see |
|---|---|
| **Estimated revenue** | Confirmed sales `₱20,000.00`; pending `₱0.00`; unreserved inventory `₱66,220.00`; projected total `₱86,220.00` |
| **High-loss crops** | Rice 5.0%, Tomato 4.0%, Corn 0.0% |
| **Slow-moving stock** | M-2001 0%, B-1001 20%, B-1002 26.7% |
| **Seasonal trends** | November: harvested 1,300 kg, sold 280 kg |

> If you change stock after this step, the totals will change too.

---

### Step 14 — Audit & Validation (screen 14)

**Compare a physical count**

| Field | Type this |
|---|---|
| Batch | `B-1001` |
| Physical count (kg) | `745` |
| Note | `5 kg missing` |

Click **Compare**.

**You should see:** “Discrepancy: -5.00 kg (shortage)”.

**Record the difference**

- Click **Record difference**.

**You should see:** a new row in the Audit trail with event `AUDIT_COUNT`, recorded `750.00`, physical `745.00`, difference `-5.00`.

**Apply the correction**

- Click **Reconcile (apply)**.

**You should see:** “Reconciled — recorded stock now equals the physical count.”

**Verify**

1. Open the **7 Stock** screen.
2. Find B-1001.

**You should see:** Remaining and Available are now `745.00`.

**Test the authorization rule**

1. Log out and sign in as `inventory` / `inventory123`.
2. Open **14 Audit & Validation**.
3. Select `B-1001`, type a physical count, and click **Reconcile (apply)**.

**You should see:** “Authorization required for inventory count reconciliation (Farm Owner / Farm Manager).”

> Reconcile needs Farm Owner or Farm Manager permission. Sign back in as `admin` to continue.

---

## 6. Test the user roles

Log out and sign in as each account. Check the left menu.

### Manager — `manager` / `manager123`

- **Should see:** all screens **except** `1 Users & Roles`.
- **Top bar:** **Save data** is available. **Load sample data** is not (Owner only).
- **Spoilage:** can approve requests.
- **Audit:** can reconcile.

### Inventory — `inventory` / `inventory123`

- **Should see:** Dashboard, Farmers, Batches, Harvest Stock, Inspection, Storage, Stock, Expiry, Spoilage & Loss, Audit & Validation.
- **Should not see:** Users & Roles, Sales, Dispatch, Forecasting, Reports & Analytics.
- **Top bar:** no **Save data**, no **Load sample data**.
- **Spoilage:** can log a request, but it stays `PENDING`. Clicking **Approve** shows an access-denied error.
- **Audit:** can compare and record, but **Reconcile** shows an authorization error.

### Sales — `sales` / `sales123`

- **Should see:** Dashboard, Expiry, Sales, Dispatch, Spoilage & Loss.
- **Should not see:** Users & Roles, Farmers, Batches, Harvest Stock, Inspection, Storage, Stock, Forecasting, Reports & Analytics.
- **Can:** create sales, process the queue, schedule deliveries, record dispatch, confirm receipt, use FEFO, purge expired stock, log spoilage.
- **Cannot:** approve spoilage or reconcile inventory.

### Tester — `tester` / `test123`

- If you assigned **Inventory Staff**, the menu should match the Inventory account.
- If you deactivated the account, signing in should fail with “Invalid credentials or deactivated account.”
- Sign back in as `admin` and reactivate the account if needed.

**Test the pending approval flow**

1. Sign in as `inventory`.
2. Go to **11 Spoilage & Loss**.
3. Log a loss for `B-1002`, quantity `5`, reason `Damaged in handling`.
4. The new row should be `PENDING`.
5. Log out and sign in as `admin`.
6. Go to **11 Spoilage & Loss**, select the pending row, and click **Approve**.

**You should see:** the row becomes `APPROVED`, and B-1002 remaining drops by 5.

> This changes the totals from Step 13. That is expected.

---

## 7. Save and restart test

1. Sign in as `admin` or `manager`.
2. Click **Save data** at the top right.

   **You should see:** “System state written to data\agristock_state.csv.”

3. Click **Log out**.
4. Sign in again.

   **You should see:** your data is still there.

5. Close the window with the **X** button. The program saves automatically on exit.
6. Start the program again and sign in.

   **You should see:** all your data is still there.

**Reset to a clean system**

1. Close AgriStock.
2. Go to `D:\AgriStock\data`.
3. Delete or rename `agristock_state.csv`.
4. Start AgriStock again.

   **You should see:** only the four default accounts, with no farmers, batches, or stock.

---

## 8. Test checklist

Copy this table into your report and mark each item **Pass** or **Fail**.

| # | Test | Expected result | Pass/Fail |
|---|---|---|---|
| 1 | Login with `admin` / `admin123` | Main window opens | |
| 2 | Wrong password | Error message, no login | |
| 3 | Add user `tester` | User appears in table | |
| 4 | Update user details | Table shows new name | |
| 5 | Assign role | Table shows new role | |
| 6 | Deactivate / activate user | Status toggles | |
| 7 | Delete own account | Error: cannot delete self | |
| 8 | Register farmer | Farmer appears in list | |
| 9 | Record harvest | Harvest log appears; total updates | |
| 10 | Delete farmer | Farmer disappears | |
| 11 | Register farm | Farm ID `FARM-1001` generated | |
| 12 | Register crop | Crop appears in list | |
| 13 | Create batch | Batch ID `B-1001` generated | |
| 14 | Expiry before harvest | Error message | |
| 15 | Record harvest stock | Saleable quantity computed | |
| 16 | Duplicate stock record | Error message | |
| 17 | Inspect accepted batch | Batch enters stock | |
| 18 | Inspect rejected batch | Batch does not enter stock | |
| 19 | Assign storage location | Storage entry appears | |
| 20 | Over-store a batch | Error message | |
| 21 | Rotate stock FIFO | Oldest lot moves to back; rotations increase | |
| 22 | Search stock | Only matching rows shown | |
| 23 | Reserve and release stock | Available quantity changes correctly | |
| 24 | Add manual stock item | Item appears in stock | |
| 25 | Duplicate stock item | Error message | |
| 26 | Expiry list order | Earliest expiry first | |
| 27 | Purge with no expired stock | “(none)” message | |
| 28 | Create sale order | Pending order appears; stock reserved | |
| 29 | Process pending sales | Orders confirmed; stock reduced | |
| 30 | Over-sell | Error message | |
| 31 | Schedule delivery | Delivery appears in upcoming list | |
| 32 | Record dispatch | Earliest deadline dispatched | |
| 33 | Confirm receipt with shortage | Loss record created | |
| 34 | Log spoilage as owner | Auto-approved; stock reduced | |
| 35 | Log spoilage as inventory | Stays pending | |
| 36 | Approve pending spoilage as owner | Approved; stock reduced | |
| 37 | Financial loss report | Totals match approved losses | |
| 38 | Add forecast history | Rows appear in table | |
| 39 | Run forecast | Average, seed need, and budget shown | |
| 40 | Forecast with too-large window | Error message | |
| 41 | Generate each report | Rows and totals match expected values | |
| 42 | Run analytics buttons | Reports appear | |
| 43 | Audit compare | Discrepancy shown | |
| 44 | Record difference | Audit row added | |
| 45 | Reconcile as owner | Stock changes to physical count | |
| 46 | Reconcile as inventory | Authorization error | |
| 47 | Role menus | Each role sees only allowed screens | |
| 48 | Save data | Confirmation message | |
| 49 | Logout and login | Data persists | |
| 50 | Close and reopen | Data persists | |

---

## 9. Troubleshooting

| Problem | What to do |
|---|---|
| `mvn` is not recognized | Use IntelliJ’s Maven panel (Way A), or install Maven and add it to PATH. |
| Java error when starting | Install Java 17 or newer. Check with `java -version`. |
| Login fails | Use the exact default accounts. Passwords are case-sensitive. If the CSV was deleted, the defaults are recreated on the next start. |
| “Saved data could not be read” | Close the app, rename or delete `data\agristock_state.csv`, then start again. |
| A button seems to do nothing | An error popup may be behind the main window. Check the taskbar. The action may also be blocked for your role. |
| “Access denied” | Your role is not allowed to do that action. Sign in as `admin` or `manager`. |
| “Only … kg available” | The quantity is larger than the available stock. Stock may be reserved or already sold. |
| “Expiry date must be after the harvest date” | Fix the dates. Expiry must be later than harvest. |
| “Select a dispatched delivery” | Click **Record Dispatch** before **Confirm Receipt**. |
| Batch ID is not `B-1001` | The data file was not empty. Close the app, remove the CSV, and start again. |
| Dates will not accept | Text fields need `YYYY-MM-DD`. DatePicker fields need a date chosen from the calendar. |
| Decimals are wrong | Use a dot, for example `13.5`, not `13,5`. |
| Window is too big | Maximize the window or set Windows display scaling to 100%. |

---

## 10. Known quirks

These are current behaviors, not necessarily bugs:

1. **The Dashboard is a simple landing page.** It does not show summary statistics yet.
2. **“Load sample data” replaces all current data.** The confirmation says “only works on an empty system,” but the code clears existing data first.
3. **The Save data button is only for Owners and Managers.** The program also saves automatically on logout and exit.
4. **Record Dispatch always uses the earliest deadline.** It does not dispatch the row you selected.
5. **Inspection is first-in, first-out.** You cannot choose which batch to inspect next.
6. **A rejected inspection does not create stock.** An accepted or downgraded inspection does.
7. **Manual stock items are not linked to a farm or batch.** Some report columns may show `-` for them.
8. **Spoilage approvals:** Owners and managers auto-approve their own requests. Inventory and Sales requests stay pending.

---

**End of manual.**
