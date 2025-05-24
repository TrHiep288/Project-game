package game;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class FinishScreen {
    private final int WIDTH = 600;
    private final int HEIGHT = 700;

    public void showFinishScreen(Stage primaryStage, int score, Runnable tryAgain) {
        VBox root = new VBox(40);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black;");

        // GAME OVER label
        Label gameOver = new Label("GAME OVER");
        gameOver.setFont(new Font("Arial Black", 72));
        gameOver.setTextFill(Color.RED);
        gameOver.setEffect(new DropShadow(30, Color.RED));

        // Score label
        Label scoreLabel = new Label("Your Score: " + score);
        scoreLabel.setFont(new Font("Arial Black", 48));
        scoreLabel.setTextFill(Color.WHITE);

        // Try Again button
        Button tryAgainBtn = new Button("Try Again");
        tryAgainBtn.setFont(new Font("Arial Black", 36));
        tryAgainBtn.setTextFill(Color.WHITE);
        tryAgainBtn.setPrefWidth(320);
        tryAgainBtn.setPrefHeight(80);
        tryAgainBtn.setStyle(
            "-fx-background-radius: 20;" +
            "-fx-background-color: #333333;" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 3;" +
            "-fx-border-color: white;"
        );
        tryAgainBtn.setEffect(new DropShadow(10, Color.BLACK));
        tryAgainBtn.setOnAction(e -> tryAgain.run());

        // Exit Game button
        Button exitBtn = new Button("Exit Game");
        exitBtn.setFont(new Font("Arial Black", 36));
        exitBtn.setTextFill(Color.WHITE);
        exitBtn.setPrefWidth(320);
        exitBtn.setPrefHeight(80);
        exitBtn.setStyle(
            "-fx-background-radius: 20;" +
            "-fx-background-color: #f44336;" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 3;" +
            "-fx-border-color: white;"
        );
        exitBtn.setEffect(new DropShadow(10, Color.RED));
        exitBtn.setOnAction(e -> {
            primaryStage.close();
        });

        root.getChildren().addAll(gameOver, scoreLabel, tryAgainBtn, exitBtn);

        Scene finishScene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(finishScene);
    }
}
