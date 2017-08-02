package com.kirinpatel;

public final class sync {

    final static int VERSION = 1;
    final static int BUILD = 6;
    final static int REVISION = 0;
    public static long deSyncWarningTime = 1000;
    public static long deSyncTime = 5000;

    public static void main(String[] args) {
        Launcher.setInstance();
    }
}
