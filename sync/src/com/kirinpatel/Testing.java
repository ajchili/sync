package com.kirinpatel;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

/**
 * Created by Kirin on 6/23/2017.
 */
public class Testing {

    public Testing() {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.setBaseDir("./tomcat");

        Context ctx = tomcat.addContext("", "/");

        Wrapper defaultServlet = ctx.createWrapper();
        defaultServlet.setName("media");
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);

        ctx.addChild(defaultServlet);
        ctx.addServletMappingDecoded("/*", "media");
        ctx.addWelcomeFile("index.html");

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
        tomcat.getServer().await();
    }
}
