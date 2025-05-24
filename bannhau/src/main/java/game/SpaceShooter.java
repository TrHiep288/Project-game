package game;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class SpaceShooter extends Application {

    private final int WIDTH = 600;
    private final int HEIGHT = 700;

    private Player player;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<EnemyBullet> enemyBullets = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();
    private List<List<Enemy>> enemyRows = new ArrayList<>();
    private int currentRow = 0;

    private boolean left, right, up, down;
    private int powerLevel = 1;
    private long powerUpEndTime = 0;
    private int playerHp = 5;
    private Label hpLabel;
    private Label infoLabel;

    private int score = 0;
    private long startTime = 0;

    private ImageView background1;
    private ImageView background2;
    private double backgroundSpeed = 1;

    @Override
    public void start(Stage primaryStage) {
    Menu menu = new Menu();
    menu.showMenu(primaryStage, () -> startGame(primaryStage));
}

    private void startGame(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Background động
        background1 = new ImageView(new Image(getClass().getResource("/game/images/Background.jpg").toExternalForm()));
        background2 = new ImageView(new Image(getClass().getResource("/game/images/Background.jpg").toExternalForm()));
        background1.setFitWidth(WIDTH);
        background1.setFitHeight(HEIGHT);
        background2.setFitWidth(WIDTH);
        background2.setFitHeight(HEIGHT);
        background1.setY(0);
        background2.setY(-HEIGHT);
        root.getChildren().addAll(background1, background2);

        // Player
        player = new Player(getClass().getResource("/game/images/Player.png").toExternalForm(), WIDTH / 2 - 30, HEIGHT - 100);
        root.getChildren().add(player);

        // Enemy rows
        createEnemyRows();
        spawnCurrentRow(root);

        // HP label
        hpLabel = new Label("HP: " + playerHp);
        hpLabel.setFont(new Font("Arial", 20));
        hpLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        hpLabel.setLayoutX(WIDTH - 100);
        hpLabel.setLayoutY(10);
        root.getChildren().add(hpLabel);

        // Info label (score + time)
        infoLabel = new Label("Score: 0   Time: 0s");
        infoLabel.setFont(new Font("Arial", 16));
        infoLabel.setStyle("-fx-text-fill: yellow; -fx-font-weight: bold;");
        infoLabel.setLayoutX(WIDTH - 180);
        infoLabel.setLayoutY(40);
        root.getChildren().add(infoLabel);

        startTime = System.currentTimeMillis();

        // Key events
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) left = true;
            else if (event.getCode() == KeyCode.RIGHT) right = true;
            else if (event.getCode() == KeyCode.UP) up = true;
            else if (event.getCode() == KeyCode.DOWN) down = true;
        });
        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) left = false;
            else if (event.getCode() == KeyCode.RIGHT) right = false;
            else if (event.getCode() == KeyCode.UP) up = false;
            else if (event.getCode() == KeyCode.DOWN) down = false;
        });

        // Game loop
        new AnimationTimer() {
            long lastShot = 0;
            AnimationTimer self = this;

            @Override
            public void handle(long now) {
                // Background scroll
                background1.setY(background1.getY() + backgroundSpeed);
                background2.setY(background2.getY() + backgroundSpeed);
                if (background1.getY() >= HEIGHT) background1.setY(background2.getY() - HEIGHT);
                if (background2.getY() >= HEIGHT) background2.setY(background1.getY() - HEIGHT);

                double playerWidth = player.getFitWidth();
                double playerHeight = player.getFitHeight();

                // Player shoot
                if (now - lastShot > 300_000_000) {
                    if (powerLevel == 1) {
                        Bullet bullet = player.shoot();
                        root.getChildren().add(bullet);
                        bullets.add(bullet);
                    } else if (powerLevel == 2) {
                        Bullet bullet1 = new Bullet(player.getX() + playerWidth / 2 - 12, player.getY() - 10, -5);
                        Bullet bullet2 = new Bullet(player.getX() + playerWidth / 2 + 8, player.getY() - 10, -5);
                        root.getChildren().addAll(bullet1, bullet2);
                        bullets.add(bullet1);
                        bullets.add(bullet2);
                    } else if (powerLevel >= 3) {
                        Bullet bullet1 = new Bullet(player.getX() + playerWidth / 2 - 18, player.getY() - 10, -5);
                        Bullet bullet2 = new Bullet(player.getX() + playerWidth / 2 - 2, player.getY() - 10, -5);
                        Bullet bullet3 = new Bullet(player.getX() + playerWidth / 2 + 14, player.getY() - 10, -5);
                        root.getChildren().addAll(bullet1, bullet2, bullet3);
                        bullets.add(bullet1);
                        bullets.add(bullet2);
                        bullets.add(bullet3);
                    }
                    lastShot = now;
                }

                // Player move
                if (left && player.getX() > 0)
                    player.setX(Math.max(0, player.getX() - 3));
                if (right && player.getX() + playerWidth < WIDTH)
                    player.setX(Math.min(WIDTH - playerWidth, player.getX() + 3));
                if (up && player.getY() > 0)
                    player.setY(Math.max(0, player.getY() - 3));
                if (down && player.getY() + playerHeight < HEIGHT)
                    player.setY(Math.min(HEIGHT - playerHeight, player.getY() + 3));

                // Update bullets
                for (Bullet bullet : bullets) bullet.update();

                // Update enemies
                for (Enemy enemy : enemies) {
                    enemy.update();
                    if (!enemy.isMovingIn()) {
                        Long lastEnemyShot = (Long) enemy.getProperties().getOrDefault("lastShot", 0L);
                        if (now - lastEnemyShot > 2_000_000_000L) {
                            EnemyBullet eb = new EnemyBullet(
                                enemy.getX() + enemy.getFitWidth() / 2 - 4,
                                enemy.getY() + enemy.getFitHeight(),
                                4
                            );
                            root.getChildren().add(eb);
                            enemyBullets.add(eb);
                            enemy.getProperties().put("lastShot", now);
                        }
                    }
                }

                // Update enemy bullets
                List<EnemyBullet> toRemove = new ArrayList<>();
                for (EnemyBullet eb : enemyBullets) {
                    eb.update();
                    if (eb.isOutOfScreen(HEIGHT)) toRemove.add(eb);
                    if (eb.getBoundsInParent().intersects(player.getBoundsInParent())) {
                        playerHp--;
                        hpLabel.setText("HP: " + playerHp);
                        toRemove.add(eb);
                    }
                }
                for (EnemyBullet eb : toRemove) {
                    root.getChildren().remove(eb);
                    enemyBullets.remove(eb);
                }

                // Bullet-enemy collision
                List<Enemy> enemyToRemove = new ArrayList<>();
                List<Bullet> bulletToRemove = new ArrayList<>();
                for (Enemy enemy : enemies) {
                    for (Bullet bullet : bullets) {
                        if (enemy.getBoundsInParent().intersects(bullet.getBoundsInParent())) {
                            enemy.takeDamage(1);
                            bulletToRemove.add(bullet);
                            if (!enemy.isAlive()) enemyToRemove.add(enemy);
                        }
                    }
                }
                for (Enemy enemy : enemyToRemove) {
                    root.getChildren().remove(enemy);
                    enemies.remove(enemy);
                    score += 100;
                }
                for (Bullet bullet : bulletToRemove) {
                    root.getChildren().remove(bullet);
                    bullets.remove(bullet);
                }

                // Next enemy row
                if (enemies.isEmpty()) {
                    currentRow++;
                    if (currentRow < enemyRows.size()) {
                        spawnCurrentRow(root);
                    } else {
                        createEnemyRows();
                        spawnCurrentRow(root);
                    }
                }

                // Draw enemy HP bars
                root.getChildren().removeIf(node -> node.getUserData() != null && node.getUserData().equals("enemy_hp_bar"));
                for (Enemy enemy : enemies) {
                    double x = enemy.getX() + enemy.getFitWidth() * 0.25;
                    double y = enemy.getY() - 1;
                    double width = enemy.getFitWidth() * 0.5;
                    double maxHp = 3.0;
                    double hpPercent = Math.max(0, enemy.getHp() / maxHp);
                    double barWidth = width * hpPercent;
                    javafx.scene.shape.Rectangle hpBar = new javafx.scene.shape.Rectangle(x, y, barWidth, 4);
                    hpBar.setFill(javafx.scene.paint.Color.RED);
                    hpBar.setUserData("enemy_hp_bar");
                    root.getChildren().add(hpBar);
                }

                // Power-up random
                if (Math.random() < 0.01 && powerUps.size() < 1) {
                    PowerUp pu = new PowerUp(Math.random() * (WIDTH - 30), 0);
                    root.getChildren().add(pu);
                    powerUps.add(pu);
                }

                // Update power-ups
                List<PowerUp> puToRemove = new ArrayList<>();
                for (PowerUp pu : powerUps) {
                    pu.update();
                    if (pu.isOutOfScreen(HEIGHT)) puToRemove.add(pu);
                    if (pu.getBoundsInParent().intersects(player.getBoundsInParent())) {
                        powerLevel = Math.min(3, powerLevel + 1);
                        powerUpEndTime = now + 5_000_000_000L;
                        puToRemove.add(pu);
                    }
                }
                for (PowerUp pu : puToRemove) {
                    root.getChildren().remove(pu);
                    powerUps.remove(pu);
                }

                // Power-up timeout
                if (powerLevel > 1 && now > powerUpEndTime) powerLevel = 1;

                // Update HP label
                hpLabel.setText("HP: " + playerHp);

                // Update score & time label
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                infoLabel.setText("Score: " + score + "   Time: " + elapsed + "s");

                // Game over
                if (playerHp <= 0) {
                    hpLabel.setText("HP: 0 (Game Over)");
                    self.stop();
                    javafx.application.Platform.runLater(() -> {
                        try { Thread.sleep(1000); } catch (InterruptedException e) {}
                        FinishScreen finish = new FinishScreen();
                        finish.showFinishScreen(primaryStage, score, () -> startGame(primaryStage));
                    });
                }
            }
        }.start();

        primaryStage.setTitle("Space Shooter - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Enemy formations
    private void createEnemyRows() {
        enemyRows.clear();
        int numEnemies = 8;
        int formationType = (int)(Math.random() * 3); // 0: line, 1: circle, 2: square

        double enemyWidth = player.getFitWidth();
        double enemyHeight = player.getFitHeight();

        if (formationType == 0) {
            // Line
            double padding = (WIDTH - enemyWidth * numEnemies) / 2;
            int y = 80;
            List<Enemy> row = new ArrayList<>();
            for (int i = 0; i < numEnemies; i++) {
                double targetX = padding + i * enemyWidth;
                int startX = (i < numEnemies / 2) ? -((int)enemyWidth) : WIDTH + (int)enemyWidth;
                Enemy enemy = new Enemy(
                    getClass().getResource("/game/images/Enemy.png").toExternalForm(),
                    startX, y, targetX
                );
                enemy.setFitWidth(enemyWidth);
                enemy.setFitHeight(enemyHeight);
                row.add(enemy);
            }
            enemyRows.add(row);
        } else if (formationType == 1) {
            // Circle
            double centerX = WIDTH / 2.0;
            double centerY = HEIGHT / 4.0;
            double radius = Math.min(WIDTH, HEIGHT) / 5.0;
            List<Enemy> row = new ArrayList<>();
            for (int i = 0; i < numEnemies; i++) {
                double angle = 2 * Math.PI * i / numEnemies;
                double targetX = centerX + radius * Math.cos(angle) - enemyWidth / 2;
                double targetY = centerY + radius * Math.sin(angle) - enemyHeight / 2;
                int startX = (i < numEnemies / 2) ? -((int)enemyWidth) : WIDTH + (int)enemyWidth;
                Enemy enemy = new Enemy(
                    getClass().getResource("/game/images/Enemy.png").toExternalForm(),
                    startX, targetY, targetX
                );
                enemy.setFitWidth(enemyWidth);
                enemy.setFitHeight(enemyHeight);
                row.add(enemy);
            }
            enemyRows.add(row);
        } else {
            // Square (2 rows, 4 cols)
            int rows = 2, cols = 4;
            double paddingX = (WIDTH - enemyWidth * cols) / 2;
            double paddingY = 60;
            for (int r = 0; r < rows; r++) {
                List<Enemy> squareRow = new ArrayList<>();
                double y = paddingY + r * (enemyHeight + 10);
                for (int c = 0; c < cols; c++) {
                    double targetX = paddingX + c * enemyWidth;
                    int startX = (c < cols / 2) ? -((int)enemyWidth) : WIDTH + (int)enemyWidth;
                    Enemy enemy = new Enemy(
                        getClass().getResource("/game/images/Enemy.png").toExternalForm(),
                        startX, y, targetX
                    );
                    enemy.setFitWidth(enemyWidth);
                    enemy.setFitHeight(enemyHeight);
                    squareRow.add(enemy);
                }
                enemyRows.add(squareRow);
            }
        }
        currentRow = 0;
    }

    // Spawn current enemy row
    private void spawnCurrentRow(Pane root) {
        for (Enemy e : enemies) {
            root.getChildren().remove(e);
        }
        enemies.clear();
        if (currentRow < enemyRows.size()) {
            for (Enemy e : enemyRows.get(currentRow)) {
                root.getChildren().add(e);
                enemies.add(e);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}