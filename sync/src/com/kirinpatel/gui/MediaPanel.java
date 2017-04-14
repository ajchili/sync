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
 *
 * @author Kirin Patel
 * @version 0.0.1
 */
public class MediaPanel extends JFXPanel {
    
    private String mediaURL = "";
    
    public MediaPanel() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX();
            }
        });
    }

    private void initFX() {
        Scene scene = createScene();
        setScene(scene);
    }
    
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
    
    public void play() {
        
    }
    
    public void stop() {
        
    }
    
    public void pause() {
        
    }
    
    public void resume() {
        
    }
    
    public void seek(int millisecond) {
        
    }
}
