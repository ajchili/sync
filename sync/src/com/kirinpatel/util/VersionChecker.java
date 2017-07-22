package com.kirinpatel.util;

import jdk.nashorn.api.scripting.URLReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * VersionChecker will determine if the current version of sync is outdated or not.
 */
public class VersionChecker {

    private final static int VERSION = 1;
    private final static int BUILD = 5;
    private final static int REVISION = 0;

    /**
     * Provides if sync is updated.
     *
     * @return Returns if sync is updated
     */
    public static boolean isUpdated() {
        try {
            Scanner s = new Scanner(new URLReader(new URL("https://github.com/ajchili/sync/releases")));
            String version = "";
            while (s.hasNext()) {
                String line = s.nextLine();
                if (line.contains("tag-reference")) {
                    s.nextLine();
                    version = s.nextLine();
                    version = version.substring(version.indexOf("tree/") + 5, version.indexOf("tree/") + 10);
                    break;
                }
            }

            int v = 0;
            int b = 0;
            int r = 0;

            for (int i = 0; i < 5; i += 2) {
                int parsedInt = Integer.parseInt(version.substring(i, i + 1));
                switch(i) {
                    case 0:
                        v = parsedInt;
                        break;
                    case 2:
                        b = parsedInt;
                        break;
                    case 4:
                        r = parsedInt;
                        break;
                    default:
                        break;
                }
            }

            if (VERSION != v) return false;
            else {
                if (BUILD < b) return false;
                else {
                    if (REVISION < r && BUILD == b) return false;
                }
            }
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }

        return true;
    }
}
