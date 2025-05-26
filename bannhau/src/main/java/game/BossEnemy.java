package game;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BossEnemy extends ImageView {
    private double hp = 60;
    private double centerX, centerY;
    private double orbitAngle = 0;
    private double orbitRadius = 80;
    private double orbitSpeed = 0.02;
    private double speedY = 2; // tốc độ rơi xuống

    private double startCenterY;
    private double targetCenterY;
    private double centerYSpeed = 2; // tốc độ tâm di chuyển xuống

    public BossEnemy(double sceneWidth, double y) {
        super(new Image(BossEnemy.class.getResource("/game/images/Boss.png").toExternalForm()));
        setFitWidth(120);
        setFitHeight(120);

        centerX = sceneWidth / 2;
        startCenterY = -getFitHeight() / 2; // bắt đầu từ trên ngoài màn hình
        targetCenterY = 140; // vị trí tâm cuối cùng ở nửa trên màn hình
        centerY = startCenterY;
    }

    public void update(double width) {
        // Tăng dần orbitAngle để boss vừa rơi vừa xoay tròn
        orbitAngle += orbitSpeed;

        // Di chuyển tâm quỹ đạo xuống dần đến targetCenterY
        if (centerY < targetCenterY) {
            centerY = Math.min(centerY + centerYSpeed, targetCenterY);
        }

        // Boss luôn xoay quanh tâm đang di chuyển
        double x = centerX + orbitRadius * Math.cos(orbitAngle) - getFitWidth() / 2;
        double y = centerY + orbitRadius * Math.sin(orbitAngle) - getFitHeight() / 2;
        setX(x);
        setY(y);
    }

    public void takeDamage(double dmg) {
        hp -= dmg;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public double getHp() {
        return hp;
    }

    public void updateBullets(List<EnemyBullet> enemyBullets, double HEIGHT, javafx.scene.Group root) {
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