package game;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class SpaceShooter extends Application {

    // Khai báo các biến toàn cục
    private final int WIDTH = 600;   // Chiều rộng cửa sổ game
    private final int HEIGHT = 800;  // Chiều cao cửa sổ game

    private Player player;           // Đối tượng người chơi (tàu)
    private List<Bullet> bullets = new ArrayList<>(); // Danh sách đạn của player
    private List<Enemy> enemies = new ArrayList<>();  // Danh sách kẻ thù
    private List<EnemyBullet> enemyBullets = new ArrayList<>(); // Danh sách đạn của kẻ thù
    private List<PowerUp> powerUps = new ArrayList<>(); // Danh sách power-up

    private boolean left, right, up, down; // Cờ kiểm tra phím di chuyển
    private boolean poweredUp = false; // Cờ kiểm tra trạng thái được tăng cường
    private int powerLevel = 1; // Mặc định bắn 1 tia
    private long powerUpEndTime = 0; // Thời gian kết thúc hiệu lực power-up
    private int playerHp = 5; // Máu của player
    private Label hpLabel;    // Label hiển thị máu

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Thêm background vào root (ảnh nền)
        ImageView background = new ImageView(new Image("file:resources/Background.jpg"));
        background.setFitWidth(WIDTH);
        background.setFitHeight(HEIGHT);
        root.getChildren().add(background);

        // Tạo player (tàu không gian) và thêm vào root
        player = new Player("file:resources/Player.png", WIDTH / 2 - 30, HEIGHT - 100);
        root.getChildren().add(player);

        // Tạo nhiều enemy xuất phát từ 2 bên
        for (int i = 0; i < 5; i++) {
            // Enemy bên trái vào giữa
            Enemy enemyLeft = new Enemy("file:resources/Enemy.png", -60, 100 + i * 60, 100 + i * 80);
            // Enemy bên phải vào giữa
            Enemy enemyRight = new Enemy("file:resources/Enemy.png", WIDTH + 10, 130 + i * 60, WIDTH - 150 - i * 80);

            root.getChildren().addAll(enemyLeft, enemyRight);
            enemies.add(enemyLeft);
            enemies.add(enemyRight);
        }

        // Tạo label hiển thị máu cho player
        hpLabel = new Label("HP: " + playerHp);
        hpLabel.setFont(new Font("Arial", 20));
        hpLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        hpLabel.setLayoutX(WIDTH - 100); // Góc phải
        hpLabel.setLayoutY(10);
        root.getChildren().add(hpLabel);

        // Xử lý phím bấm: khi nhấn phím, đặt cờ true để di chuyển mượt
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                left = true;
            } else if (event.getCode() == KeyCode.RIGHT) {
                right = true;
            } else if (event.getCode() == KeyCode.UP) {
                up = true;
            } else if (event.getCode() == KeyCode.DOWN) {
                down = true;
            }
        });

        // Khi nhả phím, đặt cờ false để dừng di chuyển
        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                left = false;
            } else if (event.getCode() == KeyCode.RIGHT) {
                right = false;
            } else if (event.getCode() == KeyCode.UP) {
                up = false;
            } else if (event.getCode() == KeyCode.DOWN) {
                down = false;
            }
        });

        // Vòng lặp game, cập nhật trạng thái mỗi frame
        new AnimationTimer() {
            long lastShot = 0; // Lưu thời gian bắn viên đạn trước
            AnimationTimer self = this; // Tham chiếu đến chính AnimationTimer này

            @Override
            public void handle(long now) {
                double playerWidth = player.getFitWidth();
                double playerHeight = player.getFitHeight();

                // Tự động bắn đạn mỗi 300ms
                if (now - lastShot > 300_000_000) { // 300 triệu nano giây = 300ms
                    if (powerLevel == 1) {
                        Bullet bullet = player.shoot();
                        root.getChildren().add(bullet);
                        bullets.add(bullet);
                    } else if (powerLevel == 2) {
                        Bullet bullet1 = new Bullet(player.getX() + player.getFitWidth() / 2 - 12, player.getY() - 10, -5);
                        Bullet bullet2 = new Bullet(player.getX() + player.getFitWidth() / 2 + 8, player.getY() - 10, -5);
                        root.getChildren().addAll(bullet1, bullet2);
                        bullets.add(bullet1);
                        bullets.add(bullet2);
                    } else if (powerLevel >= 3) {
                        Bullet bullet1 = new Bullet(player.getX() + player.getFitWidth() / 2 - 18, player.getY() - 10, -5);
                        Bullet bullet2 = new Bullet(player.getX() + player.getFitWidth() / 2 - 2, player.getY() - 10, -5);
                        Bullet bullet3 = new Bullet(player.getX() + player.getFitWidth() / 2 + 14, player.getY() - 10, -5);
                        root.getChildren().addAll(bullet1, bullet2, bullet3);
                        bullets.add(bullet1);
                        bullets.add(bullet2);
                        bullets.add(bullet3);
                    }
                    lastShot = now;
                }

                // Di chuyển player và giới hạn trong màn hình
                if (left && player.getX() > 0) 
                    player.setX(Math.max(0, player.getX() - 3));
                if (right && player.getX() + playerWidth < WIDTH) 
                    player.setX(Math.min(WIDTH - playerWidth, player.getX() + 3));
                if (up && player.getY() > 0) 
                    player.setY(Math.max(0, player.getY() - 3));
                if (down && player.getY() + playerHeight < HEIGHT) 
                    player.setY(Math.min(HEIGHT - playerHeight, player.getY() + 3));

                // Cập nhật vị trí đạn
                for (Bullet bullet : bullets) {
                    bullet.update();
                }
                // Cập nhật vị trí enemy
                for (Enemy enemy : enemies) {
                    enemy.update();

                    // Cho mỗi enemy bắn đạn mỗi 1 giây (khi đã vào giữa)
                    if (!enemy.isMovingIn()) {
                        Long lastEnemyShot = (Long) enemy.getProperties().getOrDefault("lastShot", 0L);
                        if (now - lastEnemyShot > 2_000_000_000L) { // 2 giây
                            EnemyBullet eb = new EnemyBullet(
                                enemy.getX() + enemy.getFitWidth()/2 - 4,
                                enemy.getY() + enemy.getFitHeight(),
                                1 // tốc độ đạn địch
                            );
                            root.getChildren().add(eb);
                            enemyBullets.add(eb);
                            enemy.getProperties().put("lastShot", now);
                        }
                    }
                }

                // Cập nhật vị trí đạn địch
                List<EnemyBullet> toRemove = new ArrayList<>();
                for (EnemyBullet eb : enemyBullets) {
                    eb.update();
                    if (eb.isOutOfScreen(HEIGHT)) {
                        toRemove.add(eb);
                    }
                    // Va chạm với player
                    if (eb.getBoundsInParent().intersects(player.getBoundsInParent())) {
                        playerHp--;
                        hpLabel.setText("HP: " + playerHp); // Cập nhật label
                        toRemove.add(eb);
                    }
                }
                for (EnemyBullet eb : toRemove) {
                    root.getChildren().remove(eb);
                    enemyBullets.remove(eb);
                }

                List<Enemy> enemyToRemove = new ArrayList<>();
                List<Bullet> bulletToRemove = new ArrayList<>();

                for (Enemy enemy : enemies) {
                    for (Bullet bullet : bullets) {
                        if (enemy.getBoundsInParent().intersects(bullet.getBoundsInParent())) {
                            enemy.takeDamage(1); // Mỗi viên đạn trừ 1 máu
                            bulletToRemove.add(bullet);
                            if (!enemy.isAlive()) {
                                enemyToRemove.add(enemy);
                            }
                        }
                    }
                }

                // Xóa enemy hết máu và đạn đã va chạm
                for (Enemy enemy : enemyToRemove) {
                    root.getChildren().remove(enemy);
                    enemies.remove(enemy);
                }
                for (Bullet bullet : bulletToRemove) {
                    root.getChildren().remove(bullet);
                    bullets.remove(bullet);
                }

                // Xóa các thanh máu cũ trước khi vẽ lại
                root.getChildren().removeIf(node -> node.getUserData() != null && node.getUserData().equals("enemy_hp_bar"));

                // Vẽ thanh máu cho từng enemy
                for (Enemy enemy : enemies) {
                    double x = enemy.getX() + enemy.getFitWidth() * 0.25; // Lệch vào 15% hai bên
                    double y = enemy.getY() - 1; // Sát trên đầu enemy
                    double width = enemy.getFitWidth() * 0.5; // Thanh máu chỉ chiếm 70% chiều rộng enemy
                    double maxHp = 3.0; // Số máu tối đa
                    double hpPercent = Math.max(0, enemy.getHp() / maxHp);
                    double barWidth = width * hpPercent;

                    javafx.scene.shape.Rectangle hpBar = new javafx.scene.shape.Rectangle(x, y, barWidth, 4);
                    hpBar.setFill(javafx.scene.paint.Color.RED);
                    hpBar.setUserData("enemy_hp_bar");

                    root.getChildren().add(hpBar);
                }

                // Tạo power-up ngẫu nhiên (ví dụ mỗi 5 giây có 1% xuất hiện)
                if (Math.random() < 0.01 && powerUps.size() < 1) {
                    PowerUp pu = new PowerUp(Math.random() * (WIDTH - 30), 0);
                    root.getChildren().add(pu);
                    powerUps.add(pu);
                }

                // Cập nhật power-up
                List<PowerUp> puToRemove = new ArrayList<>();
                for (PowerUp pu : powerUps) {
                    pu.update();
                    if (pu.isOutOfScreen(HEIGHT)) {
                        puToRemove.add(pu);
                    }
                    // Va chạm với player
                    if (pu.getBoundsInParent().intersects(player.getBoundsInParent())) {
                        powerLevel = Math.min(3, powerLevel + 1); // Tăng tối đa 3 tia
                        powerUpEndTime = now + 5_000_000_000L; // Hiệu lực 5 giây
                        puToRemove.add(pu);
                    }
                }
                for (PowerUp pu : puToRemove) {
                    root.getChildren().remove(pu);
                    powerUps.remove(pu);
                }

                // Hết hiệu lực power-up
                if (poweredUp && now > powerUpEndTime) {
                    poweredUp = false;
                }
                if (powerLevel > 1 && now > powerUpEndTime) {
                    powerLevel = 1;
                }

                // Cập nhật lại label máu của player
                hpLabel.setText("HP: " + playerHp);

                // Kiểm tra game over
                if (playerHp <= 0) {
                    hpLabel.setText("HP: 0 (Game Over)");
                    self.stop(); // Dừng AnimationTimer (dừng game)
                    // Đóng cửa sổ game sau 1 giây (nếu muốn)
                    javafx.application.Platform.runLater(() -> {
                        try { Thread.sleep(1000); } catch (InterruptedException e) {}
                        ((Stage) hpLabel.getScene().getWindow()).close();
                    });
                }
            }
        }.start();

        primaryStage.setTitle("Space Shooter - JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
