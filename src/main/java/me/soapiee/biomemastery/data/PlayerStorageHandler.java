package me.soapiee.biomemastery.data;

import java.util.concurrent.CompletableFuture;

public interface PlayerStorageHandler {

    CompletableFuture<PlayerData> readData();
    void saveData(boolean async);
}
