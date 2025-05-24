package game;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class Instructions {
    private final int WIDTH = 600;
    private final int HEIGHT = 500;

    public void show(Stage primaryStage, Runnable backToMenu) {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white;");

        Label title = new Label("Space Shooter Instructions");
        title.setFont(new Font("Arial Black", 32));
        title.setTextFill(Color.web("#222"));
        title.setTextAlignment(TextAlignment.CENTER);

        Label content = new Label(
            "Use the A, W, S, and D keys or the arrow keys to move your spaceship.\n" +
            "Press SPACE to shoot bullets and destroy the enemies.\n" +
            "If an enemy reaches the bottom of the screen, you lose a life.\n" +
            "The game resets if you lose all lives.\n" +
            "Collect power-ups to increase your score.\n" +
            "Defeat the boss enemy to level up and increase the difficulty.\n" +
            "Good luck and have fun!"
        );
        content.setFont(new Font("Arial", 22));
        content.setTextFill(Color.web("#222"));
        content.setWrapText(true);
        content.setTextAlignment(TextAlignment.LEFT);

        Button okBtn = new Button("OK");
        okBtn.setFont(new Font("Arial Black", 24));
        okBtn.setPrefWidth(120);
        okBtn.setPrefHeight(50);
        okBtn.setStyle(
            "-fx-background-radius: 20;" +
            "-fx-background-color: #00bfff;" +
            "-fx-text-fill: white;" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 2;" +
            "-fx-border-color: #0099cc;"
        );
        okBtn.setEffect(new DropShadow(10, Color.LIGHTBLUE));
        okBtn.setOnAction(e -> backToMenu.run());

        root.getChildren().addAll(title, content, okBtn);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Instructions");
    }
}
