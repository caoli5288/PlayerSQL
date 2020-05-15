package com.mengcraft.playersql;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(of = "id")
public class LocalData {

    @Id
    private UUID id;

    private String inventory;
}
