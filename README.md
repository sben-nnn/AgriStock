# AgriStock - Agricultural Stock Management System
LBYCPDS Group 5 (Section E27) - Java 17 + JavaFX 21 + Maven

## Run in IntelliJ
1. File > Open > select the `AgriStock` folder (the one with `pom.xml`) > "Open as Project". Let Maven import finish.
2. Project SDK: JDK 17 or newer.
3. Run `src/main/java/com/agristock/Launcher.java` (right-click > Run 'Launcher.main()').
   (Or: Maven tool window > Plugins > javafx > `javafx:run`, or `mvn javafx:run` in a terminal.)
4. Log in with: `admin/admin123` (Owner), `manager/manager123`, `inventory/inventory123`, `sales/sales123`.
5. As admin click **Load sample data** (top bar) to fill the system with demo farmers, batches, sales and deliveries.

Data is saved to `data/agristock_state.csv` on exit, on logout and with the **Save data** button.

## Module -> Data structure -> Algorithm
| # | Module | Data structure | Algorithm | Class |
|---|--------|----------------|-----------|-------|
| 1 | User & Role Management | AVL Tree | Tree traversal, binary search | `AVLTree`, `UsersView` |
| 2 | Farmer Tracking | Linked List | Linear search | `SinglyLinkedList`, `FarmersView` |
| 3 | Batch Registration | AVL Tree (batch ID) | AVL insert/search + rotations | `AVLTree`, `BatchesView` |
| 4 | Harvest Stock Recording | Linked List | Linear search | `HarvestStockView` |
| 5 | Quality Inspection | Queue | FIFO scheduling | `LinkedQueue`, `InspectionView` |
| 6 | Storage & Location | Array + Queue | FIFO rotation | `StorageView` |
| 7 | Stock Monitoring | AVL Tree | In-order traversal, binary search | `StockView` |
| 8 | Expiry & Purge | Min-Heap | FEFO / extract-min | `MinHeap`, `ExpiryView` |
| 9 | Sales & Stock Release | Queue | FIFO processing | `SalesView` |
| 10 | Dispatch & Delivery | Priority Queue | Earliest Deadline First | `DeliveryPriorityQueue`, `DispatchView` |
| 11 | Spoilage, Loss & Adjustments | Linked List | Linear search | `SpoilageView` |
| 12 | Forecasting | Array | Moving average | `DynamicArray`, `ReportService`, `ForecastView` |
| 13 | Reports & Analytics | Array | Merge sort, binary search | `Sorts`, `ReportService`, `ReportsView` |
| 14 | Inventory Audit | Linked List (append-only) | Linear search | `AuditView` |
| 15 | System State Persistence | File stream (CSV) | Sequential read/write | `Persistence` |

All core structures are hand-written in `com.agristock.ds` (no `java.util` collections for the core logic).
