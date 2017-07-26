package com.kirinpatel.tomcat;

import com.kirinpatel.util.UIMessage;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.kirinpatel.net.Server.TOMCAT_PORT;

public class TomcatServer {
    private Tomcat tomcat;

    public TomcatServer() throws IOException {
        Path mediaPath = Paths.get("tomcat/webapps/media");
        if (Files.exists(Files.createDirectories(mediaPath))) {
            UIMessage.showMessageDialog(
                    "A new folder has been added for offline media.\nPlease open \""
                            + mediaPath.toAbsolutePath()
                            + "\"\nand add any media files that you would like to use for sync.",
                    "Tomcat directory created!");
        } else {
            throw new IOException("Couldn't make tomcat directory at: " + mediaPath.toAbsolutePath());
        }
        tomcat = new Tomcat();
        tomcat.setPort(TOMCAT_PORT);
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
            // TODO(ajchili): catch this better
        } finally {
            System.exit(0);
        }
    }
}
