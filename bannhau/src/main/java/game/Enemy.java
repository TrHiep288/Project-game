package game;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Enemy extends ImageView {
    private static final double SPEED = 3;
    private boolean movingIn = true;
    private double targetX;
    private int hp; // máu của địch

    public Enemy(String imagePath, double startX, double startY, double targetX) {
        super(new Image(imagePath));
        this.setFitWidth(50);
        this.setFitHeight(50);
        this.setX(startX);
        this.setY(startY);
        this.targetX = targetX;
        this.hp = 3; // Đặt máu ban đầu, ví dụ 3
    }

    public void update() {
        if (movingIn) {
            // Di chuyển vào vị trí targetX
            if (Math.abs(getX() - targetX) > SPEED) {
                if (getX() < targetX) setX(getX() + SPEED);
                else setX(getX() - SPEED);
            } else {
                setX(targetX);
                movingIn = false; // Đã vào giữa, đứng im
            }
        }
        // Nếu muốn enemy di chuyển xuống, thêm setY(getY() + speedY);
    }

    // Giảm máu khi trúng đạn
    public void takeDamage(int damage) {
        hp -= damage;
    }

    // Kiểm tra còn sống không
    public boolean isAlive() {
        return hp > 0;
    }

    public boolean isMovingIn() {
        return movingIn;
    }

    public int getHp() {
        return hp;
    }
}