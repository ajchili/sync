package com.kirinpatel.sync;

import com.kirinpatel.sync.net.Client;
import com.kirinpatel.sync.net.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import java.time.Duration;

@RunWith(JUnit4.class)
public class IntegrationTest {
    private Client client;
    private Server server;
    @Before
    public void setup() {
        new NativeDiscovery().discover();
        server = new Server("https://www.youtube.com/watch?v=Uqpd9Ff9SpQ");
        sleep(Duration.ofSeconds(10));
        client = new Client("localhost");
    }

    @Test
    public void playVideo() {
        server.getGUI().playbackPanel.getMediaPlayer().play();
        sleep(Duration.ofSeconds(20));
    }

    private void sleep(Duration dur) {
        try {
            Thread.sleep(dur.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @After
    public void tearDown() {

    }
}
