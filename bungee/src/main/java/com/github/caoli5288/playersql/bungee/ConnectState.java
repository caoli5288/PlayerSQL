package com.github.caoli5288.playersql.bungee;

import com.github.caoli5288.playersql.bungee.protocol.DataSupply;
import net.md_5.bungee.api.config.ServerInfo;

public class ConnectState {

    private ServerInfo connect;
    private DataSupply contents;

    public void setConnect(ServerInfo connect) {
        this.connect = connect;
    }

    public ServerInfo getConnect() {
        return connect;
    }

    public void setContents(DataSupply contents) {
        this.contents = contents;
    }

    public DataSupply getContents() {
        return contents;
    }
}
