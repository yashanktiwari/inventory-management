# ==========================
# INPUT / OUTPUT
# ==========================

-injars target/inventory-management-1.0-SNAPSHOT.jar
-outjars target/inventory-management-obfuscated.jar

# ==========================
# SAFE SETTINGS
# ==========================

-dontshrink
-dontoptimize
-dontpreverify

# ==========================
# KEEP MAIN CLASS
# ==========================

-keep public class com.inventory.MainApp {
    public static void main(java.lang.String[]);
}

# ==========================
# KEEP JAVAFX APPLICATION
# ==========================

-keep class com.inventory.MainApp extends javafx.application.Application

# ==========================
# KEEP CONTROLLERS
# ==========================

-keep class com.inventory.ui.controller.** { *; }

# ==========================
# KEEP FXML FIELDS
# ==========================

-keepclassmembers class * {
    @javafx.fxml.FXML *;
}

# ==========================
# KEEP MODEL CLASSES
# ==========================

-keep class com.inventory.model.** { *; }

# ==========================
# KEEP DAO
# ==========================

-keep class com.inventory.dao.** { *; }

# ==========================
# KEEP ATTRIBUTES (IMPORTANT FOR JAVAFX)
# ==========================

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ==========================
# IGNORE LIBRARY WARNINGS
# ==========================

-dontwarn
-dontnote
