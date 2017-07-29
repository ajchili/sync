package com.kirinpatel.tomcat;

import com.kirinpatel.net.Server;
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
        if (!Files.exists(mediaPath)) {
            if(!Files.exists(Files.createDirectories(mediaPath))) {
                throw new IOException("Couldn't make tomcat directory at: " + mediaPath.toAbsolutePath());
            } else {
                UIMessage.showMessageDialog(
                        "A new folder has been added for offline media.\nPlease open \""
                                + mediaPath.toAbsolutePath()
                                + "\"\nand add any media files that you would like to use for sync.",
                        "Tomcat directory created!");
            }
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
            // If this fails, close the server
            Server.stop();
            return;
        }
        tomcat.getServer().await();
    }

    public void stop() {
        try {
            tomcat.getServer().stop();
            tomcat.getServer().destroy();
        } catch(LifecycleException e) {
            // If this fails, close the server
            System.exit(0);
        }
    }
}
