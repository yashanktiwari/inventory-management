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
