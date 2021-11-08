package com.github.caoli5288.playersql.bungee;

import com.github.caoli5288.playersql.bungee.protocol.DataSupply;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.config.ServerInfo;

@Getter
@Setter
public class ConnectState {

    private ServerInfo connect;
    private DataSupply contents;
}
