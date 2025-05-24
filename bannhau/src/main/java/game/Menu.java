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

public class Menu {
    private final int WIDTH = 600;
    private final int HEIGHT = 700;

    public void showMenu(Stage primaryStage, Runnable startGame) {
        VBox menuRoot = new VBox(30);
        menuRoot.setAlignment(Pos.CENTER);
        menuRoot.setStyle("-fx-background-color: linear-gradient(to bottom, #0a2a43, #1e3c72);");

        // Title with glow
        Label title = new Label("Welcome to\nSpace Shooter!");
        title.setFont(new Font("Arial Black", 60));
        title.setTextFill(Color.CYAN);
        title.setTextAlignment(TextAlignment.CENTER);
        DropShadow ds = new DropShadow(30, Color.CYAN);
        title.setEffect(ds);

        // Start button
        Button startBtn = new Button("START");
        styleMenuButton(startBtn);
        startBtn.setOnAction(e -> startGame.run());

        // Instructions button
        Button insBtn = new Button("INSTRUCTIONS");
        styleMenuButton(insBtn);
        insBtn.setOnAction(e -> showInstructions(primaryStage, startGame));

        menuRoot.getChildren().addAll(title, startBtn, insBtn);

        Scene menuScene = new Scene(menuRoot, WIDTH, HEIGHT);
        primaryStage.setScene(menuScene);
        primaryStage.setTitle("Space Shooter - Menu");
        primaryStage.show();
    }

    private void styleMenuButton(Button btn) {
        btn.setFont(new Font("Arial Black", 24)); // Đổi 32 thành 24 cho nhỏ lại
        btn.setTextFill(Color.WHITE);
        btn.setPrefWidth(320);
        btn.setPrefHeight(70); // Có thể giảm chiều cao nếu muốn
        btn.setStyle(
            "-fx-background-radius: 40;" +
            "-fx-background-insets: 0;" +
            "-fx-background-color: linear-gradient(to right, #7f53ff, #409fff);" +
            "-fx-border-radius: 40;" +
            "-fx-border-width: 3;" +
            "-fx-border-color: white;"
        );
        btn.setEffect(new DropShadow(15, Color.CYAN));
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-radius: 40;" +
            "-fx-background-color: linear-gradient(to right, #409fff, #7f53ff);" +
            "-fx-border-radius: 40;" +
            "-fx-border-width: 3;" +
            "-fx-border-color: white;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-radius: 40;" +
            "-fx-background-color: linear-gradient(to right, #7f53ff, #409fff);" +
            "-fx-border-radius: 40;" +
            "-fx-border-width: 3;" +
            "-fx-border-color: white;"
        ));
    }

    private void showInstructions(Stage primaryStage, Runnable backToMenu) {
        VBox insRoot = new VBox(20);
        insRoot.setAlignment(Pos.TOP_CENTER);
        insRoot.setStyle("-fx-background-color: white; -fx-padding: 30 30 30 30;");

        // Title row
        Label insTitle = new Label("Space Shooter Instructions");
        insTitle.setFont(new Font("Arial", 32));
        insTitle.setTextFill(Color.web("#222"));
        insTitle.setTextAlignment(TextAlignment.LEFT);

        // Icon (optional, you can add an ImageView here if you want)

        // Content
        Label insText = new Label(
            "Use the A, W, S, and D keys or the arrow keys to move your spaceship.\n" +
            "Press SPACE to shoot bullets and destroy the enemies.\n" +
            "If an enemy reaches the bottom of the screen, you lose a life.\n" +
            "The game resets if you lose all lives.\n" +
            "Collect power-ups to increase your score.\n" +
            "Defeat the boss enemy to level up and increase the difficulty.\n" +
            "Good luck and have fun!"
        );
        insText.setFont(new Font("Arial", 20));
        insText.setTextFill(Color.web("#222"));
        insText.setWrapText(true);
        insText.setTextAlignment(TextAlignment.LEFT);

        Button backBtn = new Button("BACK");
        backBtn.setFont(new Font("Arial Black", 22));
        backBtn.setPrefWidth(120);
        backBtn.setPrefHeight(50);
        backBtn.setStyle(
            "-fx-background-radius: 20;" +
            "-fx-background-color: #00bfff;" +
            "-fx-text-fill: white;" +
            "-fx-border-radius: 20;" +
            "-fx-border-width: 2;" +
            "-fx-border-color: #0099cc;"
        );
        backBtn.setEffect(new DropShadow(10, Color.LIGHTBLUE));
        backBtn.setOnAction(e -> backToMenu.run());

        insRoot.getChildren().addAll(insTitle, insText, backBtn);

        Scene insScene = new Scene(insRoot, WIDTH, HEIGHT);
        primaryStage.setScene(insScene);
    }
}
