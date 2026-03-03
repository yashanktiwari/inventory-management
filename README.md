The command to create package the project to .exe:
& "C:\Program Files\Java\jdk-24\bin\jpackage.exe" `
  --input target `
  --name InventoryManagementSystem `
  --main-jar inventory-management-1.0-SNAPSHOT.jar `
  --main-class com.inventory.MainApp `
  --type exe `
  --app-version 1.0 `
  --vendor "Yashank Tiwari" `
  --module-path "C:\Users\yasha\Downloads\openjfx-21.0.10_windows-x64_bin-jmods\javafx-jmods-21.0.10" `
  --add-modules javafx.controls,javafx.fxml,java.sql `
  --win-shortcut `
  --win-menu `
  --win-dir-chooser
