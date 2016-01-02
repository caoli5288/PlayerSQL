package com.mengcraft.playersql;

import com.avaje.ebean.annotation.UpdatedTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created on 16-1-2.
 */
@Entity
@Table(name = "PLAYERSQL_USER")
public class User {

    @Id
    private UUID uuid;

    private double health;

    private int food;

    private int hand;

    private int exp;

    @Column(columnDefinition = "TEXT")
    private String inventory;

    @Column(columnDefinition = "TEXT")
    private String armor;

    @Column(columnDefinition = "TEXT")
    private String chest;

    @Column(columnDefinition = "TEXT")
    private String effect;

    private boolean locked;

    @UpdatedTimestamp
    private Timestamp date;

    public UUID getUuid() {
        return uuid;
    }

    public User setUuid(UUID uuid) {
        synchronized (this) {
            this.uuid = uuid;
        }
        return this;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getHand() {
        return hand;
    }

    public void setHand(int hand) {
        this.hand = hand;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public String getInventory() {
        return inventory;
    }

    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    public String getArmor() {
        return armor;
    }

    public void setArmor(String armor) {
        this.armor = armor;
    }

    public String getChest() {
        return chest;
    }

    public void setChest(String chest) {
        this.chest = chest;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public boolean isLocked() {
        return locked;
    }

    public User setLocked(boolean locked) {
        synchronized (this) {
            this.locked = locked;
        }
        return this;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

}
