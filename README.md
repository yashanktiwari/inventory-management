## Feature List (Till now):
1. Client-Server architecture
2. Add transactions
3. Buy/Sell only
4. Issued/Returned and Scrapped
5. All the columns added
6. Search by almost all the column
7. Refresh
8. Export to excel (all + single item)
9. Export to pdf (all + single item)
10. Sorting by columns
11. Backup db
12. Restore db
13. Item history page
14. Column reordering and persistence
15. Freezing N number of columns (Scrolling simultaneously)
16. Filtering for columns
17. Persistence of filters
18. Reset all filters button
19. Item count and unit column
20. DB connection indicator

## To run the application:
- mvn clean javafx:run

## For packaging:
- mvn clean package

## For MySQL:
- CREATE USER 'inventory_user'@'%' IDENTIFIED BY 'StrongPassword123'; 
- GRANT ALL PRIVILEGES ON inventory_db.* TO 'inventory_user'@'%'; 
- FLUSH PRIVILEGES;


## The command to create package the project to .exe:

& "C:\Program Files\Java\jdk-24\bin\jpackage.exe" `
--input target `
--name InventoryManagementSystem `
--main-jar inventory-management-1.0-SNAPSHOT.jar `
--main-class com.inventory.MainApp `
--type exe `
--app-version 1.0 `
--vendor "Yashank Tiwari" `
--module-path "C:\Users\yasha\Downloads\openjfx-21.0.10_windows-x64_bin-jmods\javafx-jmods-21.0.10" `
--add-modules javafx.controls,javafx.fxml,java.sql,java.naming `
--win-shortcut `
--win-menu `
--win-dir-chooser


## Command to fill database with random 100 values for testing:
INSERT INTO transactions (
    buy_sell,
    plant,
    department,
    location,
    employee_id,
    employee_name,
    ip_address,
    item_code,
    item_name,
    item_make,
    item_model,
    item_serial,
    imei_no,
    sim_no,
    po_no,
    party_name,
    status,
    issued_datetime,
    returned_datetime,
    remarks
)
SELECT
    IF(RAND() > 0.5, 'Buy', 'Sell'),
    CONCAT('Plant-', FLOOR(RAND()*5)+1),
    CONCAT('Dept-', FLOOR(RAND()*10)+1),
    CONCAT('Location-', FLOOR(RAND()*5)+1),

    CONCAT('EMP', LPAD(n,4,'0')),
    CONCAT('Employee ', n),

    CONCAT('192.168.1.', FLOOR(RAND()*255)),

    CONCAT('ITEM', LPAD(n,4,'0')),
    CONCAT('Item ', n),
    CONCAT('Make-', FLOOR(RAND()*5)+1),
    CONCAT('Model-', FLOOR(RAND()*10)+1),
    CONCAT('SERIAL', LPAD(n,6,'0')),

    CONCAT('IMEI', FLOOR(100000000000000 + RAND()*900000000000000)),
    CONCAT('SIM', FLOOR(1000000000 + RAND()*9000000000)),

    CONCAT('PO-', FLOOR(RAND()*1000)),
    CONCAT('Party-', FLOOR(RAND()*20)),

    IF(RAND()>0.5,'Issued','In Stock'),

    NOW() - INTERVAL FLOOR(RAND()*30) DAY,

    IF(RAND()>0.5,
        NOW() - INTERVAL FLOOR(RAND()*10) DAY,
        NULL
    ),

    CONCAT('Test remarks ', n)

FROM (
    SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) a
CROSS JOIN (
    SELECT 0 b UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
    UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
) b;
