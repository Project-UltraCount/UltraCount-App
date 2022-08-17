package com.hci.ireye.data.type;

//
// Created by Lithops on 2020/6/7, 20:04.
//

import java.io.Serializable;

public class FtpInfo implements Serializable {

    public String server;
    public int port;
    public String username;
    public String password;

    public FtpInfo(String server, int port, String username, String password) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
    }
}
