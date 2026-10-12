import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class Main {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("2D Game");
        config.setWindowedMode(1920, 1080); 
        config.setForegroundFPS(120);

        new Lwjgl3Application(new Game(), config);
    }
}