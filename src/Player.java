
public class Player extends GameObject {

    private static final double SPEED = 10;

    public Player(String imagePath, double x, double y) {
        super(imagePath, x, y, 60, 60);
    }

    public void moveLeft() {
        if (getX() - SPEED >= 0) {
            setX(getX() - SPEED);
        }
    }

    public void moveRight(double sceneWidth) {
        if (getX() + SPEED + getFitWidth() <= sceneWidth) {
            setX(getX() + SPEED);
        }
    }

    public Bullet shoot() {
        double bulletX = getX() + getFitWidth() / 2 - 2; // căn giữa viên đạn
        double bulletY = getY() - 10;
        return new Bullet(bulletX, bulletY, -5); // đạn bay lên trên
    }
}
