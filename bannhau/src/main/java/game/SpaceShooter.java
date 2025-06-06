package game;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SpaceShooter extends Application {
    private BossEnemy boss;
    private boolean gameEnded = false;

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
    private boolean playerShield = false;
    private long shieldEndTime = 0;
    private Label hpLabel;
    private Label infoLabel;

    private int score = 0;
    private long startTime = 0;

    private ImageView background1;
    private ImageView background2;
    private double backgroundSpeed = 1;

    private MediaPlayer bgmPlayer;

    private boolean bossActive = false;
    private boolean bossSpawned = false;

    private double bossBulletPhase = 0;
    private int bossPhase = 0;
    private int formationsCleared = 0;

    private long lastShot = 0;

    private ImageView shieldIcon = null;

    private AIController aiController;
    private boolean aiEnabled = false; //Đặt true để bật AI tự động chơi
    
    @Override
    public void start(Stage primaryStage) {
        // Phát nhạc khi vào menu
        Media bgm = new Media(getClass().getResource("/game/sounds/ThienLyOi.mp3").toExternalForm());
        bgmPlayer = new MediaPlayer(bgm);
        bgmPlayer.setVolume(0.8);
        bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        bgmPlayer.setOnError(() -> System.out.println("Lỗi phát nhạc: " + bgmPlayer.getError()));
        bgmPlayer.play();

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
        formationsCleared = 0;
        left = right = up = down = false;

        // STOP nhạc nền menu nếu có
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }

        Pane root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // XÓA hiệu ứng khiên cũ nếu có
        if (shieldIcon != null) {
            root.getChildren().remove(shieldIcon);
            shieldIcon = null;
        }
        playerShield = false;
        shieldEndTime = 0;

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
        root.getChildren().add(player); // chỉ gọi 1 lần duy nhất khi bắt đầu game

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
            else if (event.getCode() == KeyCode.SPACE) {
                if (!spacePressed) { // Nếu vừa mới nhấn, bắn ngay 1 viên
                    shootBullet(root);
                }
                spacePressed = true;
            }
        });
        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) left = false;
            else if (event.getCode() == KeyCode.RIGHT) right = false;
            else if (event.getCode() == KeyCode.UP) up = false;
            else if (event.getCode() == KeyCode.DOWN) down = false;
            else if (event.getCode() == KeyCode.SPACE) spacePressed = false;
        });

        // Game loop
        new AnimationTimer() {
            long lastBossShot = 0;
            AnimationTimer self = this;

            @Override
            public void handle(long now) {
                if (gameEnded) return;

                // Background scroll
                background1.setY(background1.getY() + backgroundSpeed);
                background2.setY(background2.getY() + backgroundSpeed);
                if (background1.getY() >= HEIGHT) background1.setY(background2.getY() - HEIGHT);
                if (background2.getY() >= HEIGHT) background2.setY(background1.getY() - HEIGHT);

                double playerWidth = player.getFitWidth();
                double playerHeight = player.getFitHeight();

                // Player move
                if (left)
                    player.setX(Math.max(0, player.getX() - 3));
                if (right)
                    player.setX(Math.min(WIDTH - player.getFitWidth(), player.getX() + 3));
                if (up)
                    player.setY(Math.max(0, player.getY() - 3));
                if (down)
                    player.setY(Math.min(HEIGHT - player.getFitHeight(), player.getY() + 3));

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

                // Update enemy bullets & check collision with player
                List<EnemyBullet> toRemove = new ArrayList<>();
                boolean playerHitThisFrame = false;

                // Đạn enemy
                for (EnemyBullet eb : enemyBullets) {
                    eb.update();
                    if (eb.isOutOfScreen(HEIGHT)) toRemove.add(eb);
                    if (!playerHitThisFrame && eb.getBoundsInParent().intersects(player.getBoundsInParent()) && playerHp > 0 && !gameEnded) {
                        toRemove.add(eb);
                        if (!playerShield) { // Nếu không có khiên thì mới mất máu
                            playerHp--;
                        }
                        playerHitThisFrame = true;
                        // ...âm thanh...
                        break;
                    }
                }

                // Va chạm enemy thường
                if (!playerHitThisFrame) {
                    for (Enemy enemy : enemies) {
                        if (enemy.getBoundsInParent().intersects(player.getBoundsInParent()) && playerHp > 0 && !gameEnded) {
                            if (!playerShield) {
                                playerHp--;
                            }
                            playerHitThisFrame = true;
                            // ...âm thanh...
                            break;
                        }
                    }
                }

                // Va chạm boss
                if (!playerHitThisFrame && bossActive && boss != null && !gameEnded) {
                    if (player.getBoundsInParent().intersects(boss.getBoundsInParent()) && playerHp > 0) {
                        if (!playerShield) {
                            playerHp--;
                        }
                        playerHitThisFrame = true;
                        // ...âm thanh...
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
                    // Hiệu ứng nổ
                    ImageView boom = new ImageView(getClass().getResource("/game/images/Boom.png").toExternalForm());
                    boom.setFitWidth(enemy.getFitWidth());
                    boom.setFitHeight(enemy.getFitHeight());
                    boom.setX(enemy.getX());
                    boom.setY(enemy.getY());
                    root.getChildren().add(boom);
                    PauseTransition pt = new PauseTransition(Duration.seconds(0.4));
                    pt.setOnFinished(e -> root.getChildren().remove(boom));
                    pt.play();

                    AudioClip explosionSound = new AudioClip(getClass().getResource("/game/sounds/no.wav").toExternalForm());
                    explosionSound.setVolume(0.5);
                    explosionSound.play();
                }
                for (Bullet bullet : bulletToRemove) {
                    root.getChildren().remove(bullet);
                    bullets.remove(bullet);
                }

                // Next enemy row & boss xuất hiện
                if (enemies.isEmpty() && !bossActive && !bossSpawned) {
                    formationsCleared++;
                    if (formationsCleared < 3) {
                        currentRow++;
                        if (currentRow < enemyRows.size()) {
                            spawnCurrentRow(root);
                        } else {
                            createEnemyRows();
                            spawnCurrentRow(root);
                        }
                    } else {
                        boss = new BossEnemy(WIDTH, 40);
                        root.getChildren().add(boss);
                        bossActive = true;
                        bossSpawned = true;
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
                    PowerUpType[] types = PowerUpType.values();
                    PowerUpType type = types[(int)(Math.random() * types.length)];
                    PowerUp pu = new PowerUp(Math.random() * (WIDTH - 30), 0.0, type);
                    root.getChildren().add(pu);
                    powerUps.add(pu);
                }

                // Update power-ups
                List<PowerUp> puToRemove = new ArrayList<>();
                for (PowerUp pu : powerUps) {
                    pu.update();
                    if (pu.isOutOfScreen(HEIGHT)) puToRemove.add(pu);
                    if (pu.getBoundsInParent().intersects(player.getBoundsInParent())) {
                        if (pu.getType() == PowerUpType.FIRE) {
                            powerLevel = Math.min(3, powerLevel + 1);
                            powerUpEndTime = now + 5_000_000_000L;
                        } else if (pu.getType() == PowerUpType.SHIELD) {
                            playerShield = true;
                            shieldEndTime = System.currentTimeMillis() + 5000; // 5 giây
                            if (shieldIcon == null) {
                                shieldIcon = new ImageView(new Image(getClass().getResource("/game/images/shield.png").toExternalForm()));
                                shieldIcon.setFitWidth(player.getFitWidth() + 10);
                                shieldIcon.setFitHeight(player.getFitHeight() + 10);
                                shieldIcon.setMouseTransparent(true);
                                root.getChildren().add(shieldIcon);
                            }
                            shieldIcon.setVisible(true);
                        } else if (pu.getType() == PowerUpType.HEALTH) {
                            playerHp = Math.min(playerHp + 1, 5); // hồi 1 máu, tối đa 5
                        }
                        puToRemove.add(pu);
                        AudioClip powerupSound = new AudioClip(getClass().getResource("/game/sounds/ansao.wav").toExternalForm());
                        powerupSound.setVolume(1.0);
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

                // Game over: chỉ thua khi playerHp <= 0 (tức là trúng 5 viên đạn)
                if (playerHp <= 0) {
                    hpLabel.setText("HP: 0 ");
                    gameEnded = true;
                    self.stop();
                    Platform.runLater(() -> {
                        try { Thread.sleep(1000); } catch (InterruptedException e) {}
                        FinishScreen finish = new FinishScreen();
                        finish.showFinishScreen(primaryStage, score, false, () -> startGame(primaryStage));
                    });
                }

                // Boss logic
                if (bossActive && boss != null) {
                    boss.update(WIDTH);

                    // Boss bắn đạn mỗi 1 giây, giảm số lượng đạn để giảm lag
                    if (now - lastBossShot > 1_000_000_000L) {
                        double bossCenterX = boss.getX() + boss.getFitWidth() / 2 - 8;
                        double bossCenterY = boss.getY() + boss.getFitHeight() / 2 - 16;

                        if (bossPhase == 0) {
                            int numBullets = 4;
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
                        } else if (bossPhase == 1) {
                            int numBullets = 6;
                            double speed = 6;
                            for (int i = 0; i < numBullets; i++) {
                                double angle = bossBulletPhase + i * Math.PI / 3 + bossBulletPhase * 3;
                                double dx = speed * Math.cos(angle);
                                double dy = speed * Math.sin(angle);
                                BossBullet bossBullet = new BossBullet(bossCenterX, bossCenterY, dx, dy);
                                root.getChildren().add(bossBullet);
                                enemyBullets.add(bossBullet);
                            }
                            bossBulletPhase += Math.PI / 10;
                        } else if (bossPhase == 2) {
                            double speed = 7;
                            for (int i = -1; i <= 1; i++) {
                                double angle = bossBulletPhase + i * Math.PI / 6;
                                double dx = speed * Math.cos(angle);
                                double dy = speed * Math.sin(angle);
                                BossBullet bossBullet = new BossBullet(bossCenterX, bossCenterY, dx, dy);
                                root.getChildren().add(bossBullet);
                                enemyBullets.add(bossBullet);
                            }
                            bossBulletPhase += Math.PI / 6;
                        }
                        lastBossShot = now;
                    }

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
                        // Hiệu ứng nổ boss
                        ImageView boom = new ImageView(getClass().getResource("/game/images/Boom.png").toExternalForm());
                        boom.setFitWidth(boss.getFitWidth());
                        boom.setFitHeight(boss.getFitHeight());
                        boom.setX(boss.getX());
                        boom.setY(boss.getY());
                        root.getChildren().add(boom);
                        PauseTransition pt = new PauseTransition(Duration.seconds(0.7));
                        pt.setOnFinished(e -> root.getChildren().remove(boom));
                        pt.play();

                        root.getChildren().remove(boss);
                        bossActive = false;
                        gameEnded = true;
                        score += 1000;
                        AudioClip bossExplosion = new AudioClip(getClass().getResource("/game/sounds/boss.wav").toExternalForm());
                        bossExplosion.setVolume(1.0);
                        bossExplosion.play();
                        self.stop();
                        Platform.runLater(() -> {
                            try { Thread.sleep(1000); } catch (InterruptedException e) {}
                            FinishScreen finish = new FinishScreen();
                            finish.showFinishScreen(primaryStage, score, true, () -> startGame(primaryStage));
                        });
                    }

                    // Vẽ thanh máu boss di chuyển trên đầu boss
                    root.getChildren().removeIf(node -> "boss_hp_bar".equals(node.getUserData()));
                    double bossMaxHp = 60.0;
                    double barWidth = boss.getFitWidth() * (boss.getHp() / bossMaxHp);
                    double barHeight = 10;
                    double barX = boss.getX();
                    double barY = boss.getY() - barHeight - 8;
                    javafx.scene.shape.Rectangle bossHpBar = new javafx.scene.shape.Rectangle(barX, barY, barWidth, barHeight);
                    bossHpBar.setFill(javafx.scene.paint.Color.RED);
                    bossHpBar.setStroke(javafx.scene.paint.Color.WHITE);
                    bossHpBar.setStrokeWidth(2);
                    bossHpBar.setUserData("boss_hp_bar");
                    root.getChildren().add(bossHpBar);
                }

                // Sau khi xử lý đạn enemy, thêm kiểm tra va chạm với boss:
                if (bossActive && boss != null && !gameEnded) {
                    if (player.getBoundsInParent().intersects(boss.getBoundsInParent()) && playerHp > 0) {
                        playerHp--;
                        AudioClip hitSound = new AudioClip(getClass().getResource("/game/sounds/hit.wav").toExternalForm());
                        hitSound.setVolume(0.2);
                        hitSound.play();
                        // Có thể thêm hiệu ứng đẩy player ra khỏi boss nếu muốn
                    }
                }

                // Kiểm tra va chạm giữa player và enemy (chỉ kiểm tra 1 lần cho mỗi enemy)
                boolean playerCollideEnemy = false;
                for (Enemy enemy : enemies) {
                    if (!playerCollideEnemy && enemy.getBoundsInParent().intersects(player.getBoundsInParent()) && playerHp > 0 && !gameEnded) {
                        if (playerShield) {
                            playerShield = false; // Mất khiên, không mất máu
                            // Ẩn icon giáp nếu có
                        } else {
                            playerHp--;
                        }
                        playerCollideEnemy = true;
                        AudioClip hitSound = new AudioClip(getClass().getResource("/game/sounds/hit.wav").toExternalForm());
                        hitSound.setVolume(0.2);
                        hitSound.play();
                        break;
                    }
                }

                if (playerShield && System.currentTimeMillis() > shieldEndTime) {
                    playerShield = false;
                    if (shieldIcon != null) shieldIcon.setVisible(false);
                }

                // Luôn cập nhật vị trí icon khiên theo player
                if (playerShield && shieldIcon != null) {
                    shieldIcon.setX(player.getX() - 5);
                    shieldIcon.setY(player.getY() - 5);
                }

                if (aiEnabled && aiController != null) {
                    aiController.setBoss(boss);
                    aiController.setGameEnded(gameEnded);
                    aiController.updateAI();
                }

                // Bắn liên tục khi giữ SPACE
                if (spacePressed && !gameEnded && playerHp > 0) {
                    shootBullet(root);
                }
            }
        }.start();

        primaryStage.setTitle("Space Shooter - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setUserData(this); // Để AI gọi simulateKeyPress

        aiController = new AIController(
            player,
            enemies,
            enemyBullets,
            powerUps,
            bullets,
            boss,        // <-- add this
            WIDTH,       // double sceneWidth
            gameEnded,   // <-- add this
            root
        );
    }

    // Enemy formations
    private void createEnemyRows() {
        enemyRows.clear();
        int numEnemies = 8;
        double enemyWidth = 60;
        double enemyHeight = 60;

        // 1. Hình tròn
        {
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
        }

        // 2. Hình vuông (2 hàng, 4 cột)
        {
            int rows = 2, cols = 4;
            double paddingX = (WIDTH - enemyWidth * cols) / 2;
            double paddingY = 60;
            List<Enemy> squareRow = new ArrayList<>();
            for (int r = 0; r < rows; r++) {
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
            }
            enemyRows.add(squareRow);
        }

        // 3. Hình thẳng (line)
        {
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

    private boolean spacePressed = false; // Đã có

    // Thêm hàm này vào class
    private void shootBullet(Pane root) {
        long now = System.nanoTime();
        if (!gameEnded && playerHp > 0 && now - lastShot > 300_000_000) {
            double playerWidth = player.getFitWidth();
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
                Bullet bullet1 = new Bullet(player.getX() + playerWidth / 2 - 18, player.getY() - 10, -5, 2);
                Bullet bullet2 = new Bullet(player.getX() + playerWidth / 2 - 2, player.getY() - 10, -5, 2);
                Bullet bullet3 = new Bullet(player.getX() + playerWidth / 2 + 14, player.getY() - 10, -5, 2);
                root.getChildren().addAll(bullet1, bullet2, bullet3);
                bullets.add(bullet1);
                bullets.add(bullet2);
                bullets.add(bullet3);
            }
            AudioClip shootSound = new AudioClip(getClass().getResource("/game/sounds/bandan.wav").toExternalForm());
            shootSound.setVolume(0.2);
            shootSound.play();
            lastShot = now;
        }
    }

    public void simulateKeyPress(KeyCode key, Pane root) {
        if (key == KeyCode.SPACE) {
            shootBullet(root); // hoặc logic bắn đạn của bạn
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
