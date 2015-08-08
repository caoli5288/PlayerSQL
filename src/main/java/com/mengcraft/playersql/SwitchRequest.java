package com.mengcraft.playersql;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class SwitchRequest {

    public static final Manager MANAGER = new Manager();

    private UUID player;
    private String target;

    public UUID getPlayer() {
        return player;
    }

    public void setPlayer(UUID player) {
        this.player = player;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public static class Manager {

        private final Queue<SwitchRequest> queue;

        private Manager() {
            this.queue = new LinkedBlockingQueue<>();
        }

        public SwitchRequest poll() {
            return queue.poll();
        }

        public void offer(SwitchRequest request) {
            queue.offer(request);
        }

        public Queue<SwitchRequest> getQueue() {
            return queue;
        }

    }

}
