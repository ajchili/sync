package com.kirinpatel.util;

import java.text.DecimalFormat;

/**
 * @author Kirin Patel
 * @version 1.0.0
 * @date 6/17/17
 */
public class Debug {

    private static final long START_TIME = System.currentTimeMillis();

    public static void Log(String message, int type) {
        float time = (float) (System.currentTimeMillis() - Debug.START_TIME) / 1000;
        String header = '[' + new DecimalFormat("0000000.000").format(time) + "]: ";
        switch (type) {
            case 1:
                header += "(INFO) - ";
                break;
            case 2:
                header += "(ERROR) - ";
                break;
            case 3:
                header += "(UI) - ";
                break;
            case 4:
                header += "(NETWORK) - ";
                break;
            case 5:
                header += "(NETWORK ERROR) - ";
                break;
            default:
                break;
        }
        System.out.println(header + message);
    }
}
