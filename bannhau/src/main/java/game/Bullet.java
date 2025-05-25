package game;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Bullet extends ImageView {
    private double speedY;
    private int damage;

    public Bullet(double x, double y, double speedY, int damage) {
        super(new Image(Bullet.class.getResource("/game/images/Bullet.png").toExternalForm()));
        this.speedY = speedY;
        this.damage = damage;
        setFitWidth(8);
        setFitHeight(20);
        setX(x);
        setY(y);
    }

    public void update() {
        setY(getY() + speedY);
    }

    public int getDamage() {
        return damage;
    }

    public boolean isOutOfScreen(double sceneHeight) {
        return getY() < 0 || getY() > sceneHeight;
    }
}