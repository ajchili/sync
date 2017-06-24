package com.kirinpatel.tomcat;

import com.kirinpatel.util.Debug;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

/**
 * @author Kirin Patel
 * @date 6/23/17
 */
public class TomcatServer {

    private Tomcat tomcat;

    public TomcatServer() {
        File mediaPath = new File("./tomcat/webapps/media");
        if (!mediaPath.getAbsoluteFile().exists()) {
            Debug.Log("Creating Tomcat file structure...", 1);
            mediaPath.mkdirs();
            Debug.Log("Tomcat file structure created.", 1);
        }

        Debug.Log("Starting Tomcat server...", 4);
        tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.setBaseDir("./tomcat");

        Context ctx = tomcat.addContext("", "/media");

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
            Debug.Log("Tomcat server started.", 4);
        } catch (LifecycleException e) {
            Debug.Log("Unable to start tomcat started.", 5);
        }
        tomcat.getServer().await();
    }
}