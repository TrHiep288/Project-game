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
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

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

    private MediaPlayer bgmPlayer; // Thêm dòng này

    private BossEnemy boss = null;
    private boolean bossActive = false;
    private boolean gameEnded = false;
    private boolean bossSpawned = false; // Biến đánh dấu xem boss đã xuất hiện chưa

    @Override
    public void start(Stage primaryStage) {
    Menu menu = new Menu();
    menu.showMenu(primaryStage, () -> startGame(primaryStage));
}

    private void startGame(Stage primaryStage) {
        bossActive = false;
        bossSpawned = false;
        gameEnded = false;
        boss = null;
        enemies.clear();
        bullets.clear();
        enemyBullets.clear();
        powerUps.clear();
        enemyRows.clear();
        currentRow = 0;
        playerHp = 5;
        score = 0;
        powerLevel = 1;
        powerUpEndTime = 0;

        // RESET trạng thái phím
        left = false;
        right = false;
        up = false;
        down = false;

        // STOP nhạc nền cũ nếu có
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }

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

        // Media background
        Media bgm = new Media(getClass().getResource("/game/sounds/ThienLyOi.mp3").toExternalForm());
        bgmPlayer = new MediaPlayer(bgm);
        bgmPlayer.setVolume(0.8);
        bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        bgmPlayer.setOnError(() -> {
            System.out.println("Lỗi phát nhạc: " + bgmPlayer.getError());
        });
        bgmPlayer.play();

        // Game loop
        new AnimationTimer() {
            long lastShot = 0;
            long lastBossShot = 0;
            AnimationTimer self = this;

            @Override
            public void handle(long now) {
                if (gameEnded) return; // Nếu game đã kết thúc thì không làm gì cả

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
                        Bullet bullet = new Bullet(player.getX() + playerWidth / 2 - 4, player.getY() - 10, -5, 1);
                        root.getChildren().add(bullet);
                        bullets.add(bullet);
                    } else if (powerLevel == 2) {
                        Bullet bullet1 = new Bullet(player.getX() + playerWidth / 2 - 12, player.getY() - 10, -5, 1);
                        Bullet bullet2 = new Bullet(player.getX() + playerWidth / 2 + 8, player.getY() - 10, -5, 1);
                        root.getChildren().addAll(bullet1, bullet2);
                        bullets.add(bullet1);
                        bullets.add(bullet2);
                    } else if (powerLevel >= 3) {
                        Bullet bullet1 = new Bullet(player.getX() + playerWidth / 2 - 18, player.getY() - 10, -5, 2); // mạnh hơn
                        Bullet bullet2 = new Bullet(player.getX() + playerWidth / 2 - 2, player.getY() - 10, -5, 2);
                        Bullet bullet3 = new Bullet(player.getX() + playerWidth / 2 + 14, player.getY() - 10, -5, 2);
                        root.getChildren().addAll(bullet1, bullet2, bullet3);
                        bullets.add(bullet1);
                        bullets.add(bullet2);
                        bullets.add(bullet3);
                    }
                    // Khi bắn đạn
                    AudioClip shootSound = new AudioClip(getClass().getResource("/game/sounds/bandan.wav").toExternalForm());
                    shootSound.setVolume(0.2); // Âm lượng nhỏ (20%)
                    shootSound.play();

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
                            enemy.takeDamage(bullet.getDamage());
                            bulletToRemove.add(bullet);
                            if (!enemy.isAlive()) enemyToRemove.add(enemy);
                        }
                    }
                }
                for (Enemy enemy : enemyToRemove) {
                    root.getChildren().remove(enemy);
                    enemies.remove(enemy);
                    score += 100;
                    // Phát âm thanh nổ khi địch bị tiêu diệt
                    AudioClip explosionSound = new AudioClip(getClass().getResource("/game/sounds/no.wav").toExternalForm());
                    explosionSound.setVolume(0.5); // Âm lượng vừa (50%)
                    explosionSound.play();
                }
                for (Bullet bullet : bulletToRemove) {
                    root.getChildren().remove(bullet);
                    bullets.remove(bullet);
                }

                // Next enemy row & boss xuất hiện
                if (enemies.isEmpty() && !bossActive && !bossSpawned) {
                    boss = new BossEnemy(WIDTH, 40); // WIDTH là chiều rộng màn hình
                    root.getChildren().add(boss);
                    bossActive = true;
                    bossSpawned = true; // Đánh dấu đã sinh boss
                } else if (enemies.isEmpty() && !bossActive && !bossSpawned) {
                    // Chỉ spawn enemy mới nếu chưa có boss và boss chưa từng xuất hiện
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
                        // Phát âm thanh ăn power-up
                        AudioClip powerupSound = new AudioClip(getClass().getResource("/game/sounds/ansao.wav").toExternalForm());
                        powerupSound.setVolume(1.0); // Âm lượng lớn nhất (100%)
                        powerupSound.play();
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
                    gameEnded = true; // Đánh dấu là game đã kết thúc
                    self.stop();
                    javafx.application.Platform.runLater(() -> {
                        try { Thread.sleep(1000); } catch (InterruptedException e) {}
                        FinishScreen finish = new FinishScreen();
                        finish.showFinishScreen(primaryStage, score, () -> startGame(primaryStage));
                    });
                }

                if (bossActive && boss != null) {
                    boss.update(WIDTH);

                    // Boss bắn đạn mỗi 1 giây
                  
                    // Va chạm đạn với boss
                    List<Bullet> bossHit = new ArrayList<>();
                    for (Bullet bullet : bullets) {
                        if (boss.getBoundsInParent().intersects(bullet.getBoundsInParent())) {
                            boss.takeDamage(bullet.getDamage());
                            bossHit.add(bullet);
                        }
                    }
                    for (Bullet bullet : bossHit) {
                        root.getChildren().remove(bullet);
                        bullets.remove(bullet);
                    }

                    // Nếu boss chết
                    if (!boss.isAlive() && !gameEnded) {
                        root.getChildren().remove(boss);
                        bossActive = false;
                        gameEnded = true;
                        score += 1000;

                        // Âm thanh nổ boss
                        AudioClip bossExplosion = new AudioClip(getClass().getResource("/game/sounds/boss.wav").toExternalForm());
                        bossExplosion.setVolume(1.0);
                        bossExplosion.play();

                        self.stop(); // Dừng game loop

                        javafx.application.Platform.runLater(() -> {
                            try { Thread.sleep(1000); } catch (InterruptedException e) {}
                            FinishScreen finish = new FinishScreen();
                            finish.showFinishScreen(primaryStage, score, () -> startGame(primaryStage));
                        });
                    }

                    // Vẽ thanh máu boss
                    root.getChildren().removeIf(node -> "boss_hp_bar".equals(node.getUserData()));
                    double barWidth = WIDTH * (boss.getHp() / 30.0);
                    javafx.scene.shape.Rectangle bossHpBar = new javafx.scene.shape.Rectangle(0, 0, barWidth, 10);
                    bossHpBar.setFill(javafx.scene.paint.Color.PURPLE);
                    bossHpBar.setUserData("boss_hp_bar");
                    root.getChildren().add(bossHpBar);
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