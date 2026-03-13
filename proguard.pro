-injars target/inventory-management-1.0-SNAPSHOT.jar
-outjars target/inventory-obfuscated.jar

-libraryjars <java.home>/jmods

-dontwarn
-dontnote
-dontoptimize

# Keep main class
-keep public class com.inventory.MainApp {
    public static void main(java.lang.String[]);
}

# Keep JavaFX framework
-keep class javafx.** { *; }

# Keep all controllers completely (important for FXML)
-keep class com.inventory.ui.controller.** { *; }

# Keep models
-keep class com.inventory.model.** { *; }

# Keep all FXML injected fields and methods
-keepclassmembers class * {
    @javafx.fxml.FXML *;
}

# Keep ALL controller methods (event handlers like handleBackupDatabase)
-keepclassmembers class com.inventory.ui.controller.** {
    *;
}

# Keep FXML loader reflection
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod