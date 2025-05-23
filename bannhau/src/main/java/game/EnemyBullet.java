package game;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class EnemyBullet extends ImageView {
    private double speedY;

    public EnemyBullet(double x, double y, double speedY) {
        super(new Image("file:resources/Bullet.png")); // Có thể dùng ảnh khác nếu muốn
        this.speedY = speedY;
        setFitWidth(8);
        setFitHeight(20);
        setX(x);
        setY(y);
    }

    public void update() {
        setY(getY() + speedY);
    }

    public boolean isOutOfScreen(double sceneHeight) {
        return getY() > sceneHeight;
    }
}