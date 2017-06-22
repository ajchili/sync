package com.kirinpatel.gui;

import com.kirinpatel.util.Debug;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

/**
 * This class will create a media view. This view will allow for playback of .mp4 files from a URL.
 *
 * @author Kirin Patel
 * @version 0.0.6
 * @date 6/16/17
 */
public class MediaPanel extends JFXPanel {

    private String mediaURL = "";
    private MediaPlayer mediaPlayer;
    private MediaControl mediaControl;
    private boolean isPaused = false;

    /**
     * Main constructor that will initialize the MediaPanel.
     */
    public MediaPanel() {
        Debug.Log("Creating MediaPanel...", 3);
        Platform.runLater(this::initFX);
    }

    /**
     * Secondary constructor that will initialize the MediaPanel with a
     * specified URL.
     *
     * @param url Media URL
     */
    public MediaPanel(String url) {
        Debug.Log("Creating MediaPanel...", 3);
        this.mediaURL = url;
        Platform.runLater(this::initFX);
    }

    /**
     * Initializes the FX panel.
     */
    private void initFX() {
        Debug.Log("Initializing scene...", 3);
        Scene scene = createScene();
        setScene(scene);
        Debug.Log("Scene initialized.", 3);
        Debug.Log("MediaPanel created.", 3);
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
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(false);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaControl = new MediaControl(mediaPlayer);
            mediaControl.setStyle("-fx-background-color: #000000;");
            scene.setRoot(mediaControl);

            mediaControl.mediaView.fitWidthProperty().bind(scene.widthProperty());
            mediaControl.mediaView.fitHeightProperty().bind(scene.heightProperty());
        } else {
            if (mediaPlayer != null && mediaPlayer.getMedia() != null) {
                mediaPlayer.pause();
            }
        }

        return (scene);
    }

    /**
     * Sets the media URL of the MediaPanel.
     *
     * @param mediaURL Media URL
     */
    public void setMediaURL(String mediaURL) {
        if (!this.mediaURL.equals(mediaURL)) {
            if (mediaURL.isEmpty()) {
                Debug.Log("Clearing MediaURL...", 1);
            } else {
                Debug.Log("Setting MediaURL (" + mediaURL + ")...", 1);
            }

            this.mediaURL = mediaURL;
            Platform.runLater(this::initFX);
        } else {
            Debug.Log("MediaURL does not refresh, please seek through the media.", 1);
        }
    }

    public void playMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    public void pauseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void seek(Duration time) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(time);
        }
    }

    public String getMediaURL() {
        return mediaURL;
    }

    public boolean isMediaPaused() {
        return isPaused;
    }

    public Duration getMediaTime() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentTime();
        }

        return new Duration(0);
    }

    /**
     * @author Kirin Patel
     * @version 0.0.1
     * @date 6/17/17
     *
     * Source: http://docs.oracle.com/javafx/2/media/playercontrol.htm
     */
    public class MediaControl extends BorderPane {
        private MediaPlayer mp;
        private MediaView mediaView;
        private final boolean repeat = false;
        private boolean stopRequested = false;
        private boolean atEndOfMedia = false;
        private Duration duration;
        private Slider timeSlider;
        private Label playTime;
        private Slider volumeSlider;
        private HBox mediaBar;

        public MediaControl(final MediaPlayer mp) {
            this.mp = mp;
            mediaView = new MediaView(mp);

            StackPane p = new StackPane();
            p.getChildren().add(mediaView);
            p.setStyle("-fx-background-color: black;");
            StackPane.setAlignment(mediaView, Pos.CENTER);

            setCenter(p);

            mediaBar = new HBox();
            mediaBar.setAlignment(Pos.BOTTOM_CENTER);
            mediaBar.setPadding(new Insets(5, 10, 5, 10));
            BorderPane.setAlignment(mediaBar, Pos.CENTER);

            final Button playButton  = new Button(">");
            mediaBar.getChildren().add(playButton);
            p.getChildren().add(mediaBar);

            Label spacer = new Label("   ");
            mediaBar.getChildren().add(spacer);

            Label timeLabel = new Label("Time: ");
            mediaBar.getChildren().add(timeLabel);

            timeSlider = new Slider();
            HBox.setHgrow(timeSlider, Priority.ALWAYS);
            timeSlider.setMinWidth(50);
            timeSlider.setMaxWidth(Double.MAX_VALUE);
            mediaBar.getChildren().add(timeSlider);

            playTime = new Label();
            playTime.setPrefWidth(130);
            playTime.setMinWidth(50);
            mediaBar.getChildren().add(playTime);

            Label volumeLabel = new Label("Vol: ");
            mediaBar.getChildren().add(volumeLabel);

            volumeSlider = new Slider();
            volumeSlider.setPrefWidth(70);
            volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
            volumeSlider.setMinWidth(30);

            mediaBar.getChildren().add(volumeSlider);

            playButton.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    MediaPlayer.Status status = mp.getStatus();

                    if (status == MediaPlayer.Status.UNKNOWN  || status == MediaPlayer.Status.HALTED)
                    {
                        return;
                    }

                    if ( status == MediaPlayer.Status.PAUSED
                            || status == MediaPlayer.Status.READY
                            || status == MediaPlayer.Status.STOPPED)
                    {
                        if (atEndOfMedia) {
                            mp.seek(mp.getStartTime());
                            atEndOfMedia = false;
                        }
                        mp.play();
                    } else {
                        mp.pause();
                    }
                }
            });

            mp.currentTimeProperty().addListener(new InvalidationListener()
            {
                public void invalidated(Observable ov) {
                    updateValues();
                }
            });

            mp.setOnPlaying(new Runnable() {
                public void run() {
                    if (stopRequested) {
                        mp.pause();
                        stopRequested = false;
                    } else {
                        Debug.Log("Playing media...", 1);
                        isPaused = false;
                        playButton.setText("||");
                    }
                }
            });

            mp.setOnPaused(new Runnable() {
                public void run() {
                    Debug.Log("Pausing media...", 1);
                    isPaused = true;
                    playButton.setText(">");
                }
            });

            mp.setOnReady(new Runnable() {
                public void run() {
                    duration = mp.getMedia().getDuration();
                    updateValues();
                }
            });

            mp.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
            mp.setOnEndOfMedia(new Runnable() {
                public void run() {
                    if (!repeat) {
                        playButton.setText(">");
                        stopRequested = true;
                        atEndOfMedia = true;
                    }
                }
            });

            timeSlider.valueProperty().addListener(new InvalidationListener() {
                public void invalidated(Observable ov) {
                    if (timeSlider.isValueChanging()) {
                        Debug.Log("Seeking through media...", 1);
                        mp.seek(duration.multiply(timeSlider.getValue() / 100.0));
                    }
                }
            });

            volumeSlider.valueProperty().addListener(new InvalidationListener() {
                public void invalidated(Observable ov) {
                    Debug.Log("Setting volume...", 1);
                    mp.setVolume(volumeSlider.getValue() / 100.0);
                }
            });
        }

        protected void updateValues() {
            if (playTime != null && timeSlider != null && volumeSlider != null) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        Duration currentTime = mp.getCurrentTime();
                        playTime.setText(formatTime(currentTime, duration));
                        timeSlider.setDisable(duration.isUnknown());
                        if (!timeSlider.isDisabled()
                                && duration.greaterThan(Duration.ZERO)
                                && !timeSlider.isValueChanging()) {
                            timeSlider.setValue(currentTime.divide(duration).toMillis()
                                    * 100.0);
                        }
                        if (!volumeSlider.isValueChanging()) {
                            volumeSlider.setValue((int)Math.round(mp.getVolume()
                                    * 100));
                        }
                    }
                });
            }
        }

        private String formatTime(Duration elapsed, Duration duration) {
            int intElapsed = (int)Math.floor(elapsed.toSeconds());
            int elapsedHours = intElapsed / (60 * 60);
            if (elapsedHours > 0) {
                intElapsed -= elapsedHours * 60 * 60;
            }
            int elapsedMinutes = intElapsed / 60;
            int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                    - elapsedMinutes * 60;

            if (duration.greaterThan(Duration.ZERO)) {
                int intDuration = (int)Math.floor(duration.toSeconds());
                int durationHours = intDuration / (60 * 60);
                if (durationHours > 0) {
                    intDuration -= durationHours * 60 * 60;
                }
                int durationMinutes = intDuration / 60;
                int durationSeconds = intDuration - durationHours * 60 * 60 -
                        durationMinutes * 60;
                if (durationHours > 0) {
                    return String.format("%d:%02d:%02d/%d:%02d:%02d",
                            elapsedHours, elapsedMinutes, elapsedSeconds,
                            durationHours, durationMinutes, durationSeconds);
                } else {
                    return String.format("%02d:%02d/%02d:%02d",
                            elapsedMinutes, elapsedSeconds,durationMinutes,
                            durationSeconds);
                }
            } else {
                if (elapsedHours > 0) {
                    return String.format("%d:%02d:%02d", elapsedHours,
                            elapsedMinutes, elapsedSeconds);
                } else {
                    return String.format("%02d:%02d",elapsedMinutes,
                            elapsedSeconds);
                }
            }
        }
    }

}