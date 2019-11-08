package com.mengcraft.playersql;

import com.avaje.ebean.annotation.UpdatedTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;

import static com.mengcraft.playersql.PlayerData.TABLE_NAME;

/**
 * Created on 16-1-2.
 */
@Entity
@Table(name = TABLE_NAME)
public class PlayerData {

    public static final String TABLE_NAME = "PLAYERSQL";

    @Id
    private UUID uuid;

    private String name;

    private double health;

    private int food;

    private int hand;

    private int exp;

    @Column(columnDefinition = "LONGTEXT")
    private String inventory;

    @Column(columnDefinition = "TEXT")
    private String armor;

    @Column(columnDefinition = "LONGTEXT")
    private String chest;

    @Column(columnDefinition = "TEXT")
    private String effect;

    private boolean locked;

    @UpdatedTimestamp
    private Timestamp lastUpdate;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

}
