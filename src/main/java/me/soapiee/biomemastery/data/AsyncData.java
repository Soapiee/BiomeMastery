package me.soapiee.biomemastery.data;

import lombok.Getter;

public class AsyncData {

    @Getter private final int level;
    @Getter private final int progress;

    public AsyncData(int level, int progress){
        this.level = level;
        this.progress = progress;
    }
}
