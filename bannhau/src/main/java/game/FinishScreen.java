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

import java.util.ArrayList;
import java.util.List;

public class FinishScreen {
    private final int WIDTH = 600;
    private final int HEIGHT = 700;

    // Add root as a field
    private VBox root;

    // List to store enemy bullets
    private List<EnemyBullet> enemyBullets = new ArrayList<>();

    // Add bossPhase variable
    private int bossPhase = 0;

    // Add bossBulletPhase variable
    private double bossBulletPhase = 0.0;

    public void showFinishScreen(Stage primaryStage, int score, boolean isWin, Runnable tryAgain) {
        root = new VBox(40);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black;");
        VBox root = new VBox(40);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: black;");

        // RESULT label
        Label resultLabel = new Label(isWin ? "WIN" : "GAME OVER");
        resultLabel.setFont(new Font("Arial", 48));
        resultLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

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

        root.getChildren().addAll(resultLabel, scoreLabel, tryAgainBtn, exitBtn);

        Scene finishScene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(finishScene);
    }

    // Boss bắn đạn mỗi 1.5 giây, chỉ bắn 4 viên/lần
    public void bossShoot() {
        // Example: Set boss center coordinates (adjust as needed)
        double bossCenterX = WIDTH / 2.0;
        double bossCenterY = 100; // Example Y position for the boss

        if (bossPhase == 0) {
            int numBullets = 4; // Giảm số lượng đạn
            double speed = 4.5;
            for (int i = 0; i < numBullets; i++) {
                double angle = bossBulletPhase + 2 * Math.PI * i / numBullets;
                double dx = speed * Math.cos(angle);
                double dy = speed * Math.sin(angle);
                BossBullet bossBullet = new BossBullet(bossCenterX, bossCenterY, dx, dy);
                root.getChildren().add(bossBullet);
                enemyBullets.add(bossBullet);
            }
            bossBulletPhase += Math.PI / 32;
        }
    }

    public void updateEnemyBullets() {
        List<EnemyBullet> toRemove = new ArrayList<>();
        for (EnemyBullet eb : enemyBullets) {
            eb.update();
            if (eb.isOutOfScreen(HEIGHT)) {
                toRemove.add(eb);
            }
        }
        for (EnemyBullet eb : toRemove) {
            root.getChildren().remove(eb);
            enemyBullets.remove(eb);
        }
    }
}
