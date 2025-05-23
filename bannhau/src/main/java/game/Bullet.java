package game;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Bullet extends ImageView {
    private double speedY;

    public Bullet(double x, double y, double speedY) {
        super(new Image(Bullet.class.getResource("/game/images/Bullet.png").toExternalForm()));
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
        return getY() + getFitHeight() < 0;
    }
}