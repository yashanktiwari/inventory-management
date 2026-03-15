package com.inventory.model.master;

public class PartyMaster {

    private String partyName;

    public PartyMaster() {}

    public PartyMaster(String partyName) {
        this.partyName = partyName;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }
}