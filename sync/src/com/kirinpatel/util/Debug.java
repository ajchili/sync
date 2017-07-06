package com.kirinpatel.util;

import java.text.DecimalFormat;

public class Debug {

    public static int debugLevel = 0;
    private static final long START_TIME = System.currentTimeMillis();

    public static void Log(String message, int type) {
        float time = (float) (System.currentTimeMillis() - Debug.START_TIME) / 1000;
        String header = '[' + new DecimalFormat("0000000.000").format(time) + "]: ";
        switch(type) {
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
            case 6:
                header += "(VLCJ) - ";
                break;
            default:
                break;
        }

        if (debugLevel == 0 && (type == 2 || type == 5)) System.out.println(header + message);
        else if (debugLevel == 1 && (type == 2 ||type== 4 || type == 5)) System.out.println(header + message);
        else if (debugLevel == 2) System.out.println(header + message);
    }
}
