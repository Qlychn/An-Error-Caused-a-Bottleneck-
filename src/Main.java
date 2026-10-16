import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;



public class Main {
    public static void main(String[] args) {

        Monitor[] monitors = Lwjgl3ApplicationConfiguration.getMonitors();
        DisplayMode primaryMode = Lwjgl3ApplicationConfiguration.getDisplayMode();

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("An Error caused a bottleneck °^°");
        config.useVsync(true);
        //config.setWindowedMode(1280 , 720); 
        config.setForegroundFPS(120);
        config.setWindowIcon("src/assets/appIcon.png");
        config.setFullscreenMode(primaryMode);


        new Lwjgl3Application(new Game(), config);
    }
}