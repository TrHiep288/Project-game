import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PowerUp extends ImageView {
    private double speedY = 2;

    public PowerUp(double x, double y) {
        super(new Image("file:resources/PowerUp.png")); // Đặt ảnh powerup vào resources
        setFitWidth(30);
        setFitHeight(30);
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