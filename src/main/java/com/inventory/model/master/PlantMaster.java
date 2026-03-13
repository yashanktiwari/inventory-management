package com.inventory.model.master;

public class PlantMaster {

    private String plantName;

    public PlantMaster() {}

    public PlantMaster(String plantName) {
        this.plantName = plantName;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }
}