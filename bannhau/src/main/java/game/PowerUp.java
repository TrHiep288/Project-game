package game;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PowerUp extends ImageView {
    private double speedY = 2;
    private PowerUpType type;

    public PowerUp(double x, double y, PowerUpType type) {
        super(getImageForType(type));
        this.type = type;
        setFitWidth(30);
        setFitHeight(30);
        setX(x);
        setY(y);
    }

    private static Image getImageForType(PowerUpType type) {
        String path;
        switch (type) {
            case FIRE:
                path = "/game/images/powerup.png";
                break;
            case SHIELD:
                path = "/game/images/powerup_shield.png";
                break;
            case HEALTH:
                path = "/game/images/powerup_health.png";
                break;
            default:
                throw new IllegalArgumentException("Unknown PowerUpType: " + type);
        }
        return new Image(PowerUp.class.getResource(path).toExternalForm());
    }

    public void update() {
        setY(getY() + speedY);
    }

    public boolean isOutOfScreen(double sceneHeight) {
        return getY() > sceneHeight;
    }

    public PowerUpType getType() {
        return type;
    }
}
