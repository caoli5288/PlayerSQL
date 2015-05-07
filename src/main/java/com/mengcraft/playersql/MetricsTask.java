package com.mengcraft.playersql;

import java.io.IOException;

import org.mcstats.Metrics;

public class MetricsTask implements Runnable {
    
    private final Main main;

    public MetricsTask(Main main) {
        this.main = main;
    }

    @Override
    public void run() {
        try {
            new Metrics(main).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
