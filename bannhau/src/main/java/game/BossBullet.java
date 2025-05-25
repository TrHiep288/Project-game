package game;

import javafx.scene.image.Image;

public class BossBullet extends EnemyBullet {
    private double dx, dy;

    public BossBullet(double x, double y, double dx, double dy) {
        super(x, y, 0); // speedY không dùng nữa
        setImage(new Image(BossBullet.class.getResource("/game/images/Bullet.png").toExternalForm()));
        setFitWidth(16);
        setFitHeight(32);
        this.dx = dx;
        this.dy = dy;

        // Tính góc xoay theo hướng bay
        double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90; // +90 để đầu đạn hướng theo chiều bay
        setRotate(angle);
    }

    @Override
    public void update() {
        setX(getX() + dx);
        setY(getY() + dy);
    }

 }
