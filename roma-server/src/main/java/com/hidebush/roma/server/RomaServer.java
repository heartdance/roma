package com.hidebush.roma.server;

import com.hidebush.roma.server.listener.ClientListener;

/**
 * Created by htf on 2020/9/27.
 */
public class RomaServer {
    public static void main(String[] args) {
        ClientListener.INSTANCE.listen(9998);
    }
}
