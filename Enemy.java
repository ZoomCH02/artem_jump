import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Enemy extends ImageView {
    private double speed;

    public Enemy(Image image, double speed) {
        super(image);
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void move() {
        setTranslateX(getTranslateX() - speed);
    }
}
