package com.study;

import com.study.connector.Connector;

public class MyWasMain {
    public static void main(String[] args) {
        Connector connector = new Connector(8080, 100);
        connector.start();
    }
}
