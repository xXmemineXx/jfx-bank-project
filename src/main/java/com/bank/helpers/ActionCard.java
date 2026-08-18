package com.bank.helpers;

public interface ActionCard {
    // This allows any action controller to handle a data injection loop
    void populateCardData(Object dataRecord);
}
