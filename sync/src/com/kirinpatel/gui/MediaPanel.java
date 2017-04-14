/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kirinpatel.gui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

/**
 * This class will create a media view. This view will allow for playback of
 * .mp4 files from a URL.
 *
 * @author Kirin Patel
 * @version 0.0.2
 */
public class MediaPanel extends JFXPanel {
    
    private String mediaURL = "";
    
    /**
     * Main constructor that will initialize the MediaPanel.
     */
    public MediaPanel() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX();
            }
        });
    }
    
    /**
     * Secondary constructor that will initialize the MediaPanel with a
     * specified URL.
     * 
     * @param url Media URL
     */
    public MediaPanel(String url) {
        this.mediaURL = url;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX();
            }
        });
    }

    /**
     * Initializes the FX panel.
     */
    private void initFX() {
        Scene scene = createScene();
        setScene(scene);
    }
    
    /**
     * Creates the FX scene.
     * 
     * @return Returns the scene.
     */
    private Scene createScene() {
        Group root = new Group();
        Scene scene = new Scene(root);
        
        if (!mediaURL.equals("")) {
            Media media = new Media(mediaURL);
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            MediaView mediaView = new MediaView(mediaPlayer);
            StackPane p = new StackPane();
            p.getChildren().add(mediaView);
            p.setStyle("-fx-background-color: #000000;");

            StackPane.setAlignment(mediaView, Pos.CENTER);
            scene = new Scene(p);
            mediaView.fitWidthProperty().bind(scene.widthProperty());
            mediaView.fitHeightProperty().bind(scene.heightProperty());
        }
        
        return (scene);
    }
    
    /**
     * Sets the media URL of the MediaPanel.
     * 
     * @param url Media URL
     */
    public void setMedia(String url) {
        this.mediaURL = url;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX();
            }
        });
    }
    
    /**
     * Plays media.
     */
    public void play() {
        
    }
    
    /**
     * Stops media.
     */
    public void stop() {
        
    }
    
    /**
     * Pauses media.
     */
    public void pause() {
        
    }
    
    /**
     * Resumes media.
     */
    public void resume() {
        
    }
    
    /**
     * Seeks to specified time in media for playback.
     * 
     * @param millisecond Time in milliseconds
     */
    public void seek(int millisecond) {
        
    }
}
