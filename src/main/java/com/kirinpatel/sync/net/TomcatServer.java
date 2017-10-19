package com.kirinpatel.sync.net;

import com.kirinpatel.sync.Launcher;
import com.kirinpatel.sync.util.UIMessage;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class TomcatServer {
    private Tomcat tomcat;

    TomcatServer() throws IOException {
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
        tomcat.setPort(Server.TOMCAT_PORT);
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

    void start() {
        try {
            tomcat.start();
        } catch(LifecycleException e) {
            // If this fails, close the server
            Launcher.connectedUser.stop();
            return;
        }
        tomcat.getServer().await();
    }

    void stop() {
        try {
            tomcat.getServer().stop();
            tomcat.getServer().destroy();
        } catch(LifecycleException e) {
            // If this fails, close sync
            System.exit(0);
        }
    }
}
