package com.hidebush.roma.client;

/**
 * Created by htf on 2021/8/12.
 */
public class RomaClientExample {

    public static void main(String[] args) {
        RomaClient.main("-h localhost -p 9989 -proxy 80-localhost:8080,81-127.0.0.1:8081".split("\\s+"));
    }
}
