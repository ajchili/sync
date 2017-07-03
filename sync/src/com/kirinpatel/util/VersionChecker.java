package com.kirinpatel.util;

import jdk.nashorn.api.scripting.URLReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class VersionChecker {

    private final static int VERSION = 1;
    private final static int BUILD = 3;
    private final static int REVISION = 1;

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

            for (int i = 0; i < 5; i += 2) {
                int parsedInt = Integer.parseInt(version.substring(i, i + 1));
                switch(i) {
                    case 0:
                        if (parsedInt != VERSION) return false;
                        break;
                    case 2:
                        if (parsedInt != BUILD) return false;
                        break;
                    case 4:
                        if (parsedInt != REVISION) return false;
                        break;
                    default:
                        break;
                }
            }
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }

        return true;
    }
}
