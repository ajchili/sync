package com.kirinpatel.tomcat;

import com.kirinpatel.util.UIMessage;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class TomcatServer {

    private Tomcat tomcat;

    public TomcatServer() {
        File mediaPath = new File("tomcat/webapps/media");
        if (!mediaPath.getAbsoluteFile().exists()) {
            mediaPath.mkdirs();
            new UIMessage("Tomcat directory created!", "A new folder has been added for offline media.\nPlease open \"" + mediaPath.getAbsolutePath() + "\"\nand add any media files that you would like to use for sync.", 0);
        }

        tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.setBaseDir("./tomcat");

        Context ctx = tomcat.addContext("","./media");

        Wrapper defaultServlet = ctx.createWrapper();
        defaultServlet.setName("media");
        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.setLoadOnStartup(1);

        ctx.addChild(defaultServlet);
        ctx.addServletMappingDecoded("/*", "media");
        ctx.addWelcomeFile("index.html");
    }

    public void start() {
        try {
            tomcat.start();
        } catch(LifecycleException e) {
            System.exit(0);
        }
        tomcat.getServer().await();
    }

    public void stop() {
        try {
            tomcat.getServer().stop();
            tomcat.getServer().destroy();
        } catch(LifecycleException e) {
            System.out.println("Catch this exception better, smh. What is wrong with you????");
        } finally {
            System.exit(0);
        }
    }
}
