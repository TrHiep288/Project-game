package game;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GameObject extends ImageView {
    public GameObject(String imagePath, double x, double y, double width, double height) {
        super(new Image(imagePath));
        setX(x);
        setY(y);
        setFitWidth(width);
        setFitHeight(height);
    }
}