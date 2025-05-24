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
        insRoot.setAlignment(Pos.CENTER);
        insRoot.setStyle("-fx-background-color: linear-gradient(to bottom, #0a2a43, #1e3c72);");

        Label insTitle = new Label("Instructions");
        insTitle.setFont(new Font("Arial Black", 48));
        insTitle.setTextFill(Color.CYAN);
        insTitle.setEffect(new DropShadow(20, Color.CYAN));

        Label insText = new Label(
            "Move: Arrow Keys\n" +
            "Shoot: Auto\n" +
            "Collect power-ups to upgrade your weapon.\n" +
            "Survive as long as possible and defeat enemies!"
        );
        insText.setFont(new Font("Arial", 24));
        insText.setTextFill(Color.WHITE);
        insText.setTextAlignment(TextAlignment.CENTER);

        Button backBtn = new Button("BACK");
        styleMenuButton(backBtn);
        backBtn.setOnAction(e -> backToMenu.run());

        insRoot.getChildren().addAll(insTitle, insText, backBtn);

        Scene insScene = new Scene(insRoot, WIDTH, HEIGHT);
        primaryStage.setScene(insScene);
    }
}
