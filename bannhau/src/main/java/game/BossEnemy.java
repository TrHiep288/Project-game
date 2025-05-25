package game;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class BossEnemy extends ImageView {
    private double hp = 30;
    private double speedX = 2;
    private double speedY = 0.5;
    private double direction = 1;

    public BossEnemy(double x, double y) {
        super(new Image(BossEnemy.class.getResource("/game/images/Boss.png").toExternalForm()));
        setFitWidth(120);
        setFitHeight(120);
        setX(x);
        setY(y);
    }

    public void update(double width) {
        // Di chuyển ngang qua lại
        setX(getX() + speedX * direction);
        if (getX() <= 0 || getX() + getFitWidth() >= width) {
            direction *= -1;
        }
        // Có thể thêm di chuyển dọc nếu muốn
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

}