package com.kirinpatel.util;

public class URLEncoding {

    /**
     * Credit: https://www.urlencoder.org/
     *
     * @param url Un-encoded URL
     * @return Encoded URL
     */
    public static String encode(String url) {
        url = url.replace("%", "%25");
        url = url.replace(" ", "%20");
        url = url.replace("\"", "%22");
        url = url.replace("-", "%2D");
        url = url.replace(".", "%2E");
        url = url.replace("<", "%3C");
        url = url.replace(">", "%3E");
        url = url.replace("\\", "%5C");
        url = url.replace("^", "%5E");
        url = url.replace("_", "%5F");
        url = url.replace("`", "%60");
        url = url.replace("{", "%7B");
        url = url.replace("|", "%7C");
        url = url.replace("}", "%7D");
        url = url.replace("~", "%7E");
        return url;
    }
}
