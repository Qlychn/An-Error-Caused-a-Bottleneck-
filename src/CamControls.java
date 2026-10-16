import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;


public class CamControls {
    public static float camZoomChangeVal = 0;
    private static float targetZoom = Game.baseGameZoom;
    public static float camZoomDifference;

    public static float mousePosX;
    public static float mousePosY;
    public static Vector2 initialWorldCords = new Vector2();
    public static Vector2 worldCords = new Vector2();
    public static Vector2 camCordsDifference = new Vector2(0, 0);
    public static float camVelocityX = 0;
    public static float camVelocityY = 0;
    public static float camPanLerp = 0.25f;   
    public static float camFriction = 0.93f; 
    public static boolean mouseDown = false;

    public static void camZoomChange(float val) {
        camZoomChangeVal += (val / 5f);
    }

    public static void mainCamZoomHandler(OrthographicCamera cam, float baseZoomValue) {
        targetZoom += camZoomChangeVal;
        camZoomChangeVal = 0;
        targetZoom = MathUtils.clamp(targetZoom, 1f, 10f);

        camZoomDifference = targetZoom - cam.zoom;
        cam.zoom += 0.1f * camZoomDifference;

        camClamp(cam);
        cam.update();
    }

    // Function made with assistance from Claude Sonnet 5.5
    public static void camPanStart(ExtendViewport viewport, float x, float y) {
        mousePosX = x;
        mousePosY = y;

        initialWorldCords.set(x, y);
        viewport.unproject(initialWorldCords); // screen -> world, once, at click time

        camVelocityX = 0;
        camVelocityY = 0;
        mouseDown = true;
    }

    // Function made with assistance from Claude Sonnet 5.5
    public static void mainCamPanHandler(OrthographicCamera camera, ExtendViewport viewport) {
        if (mouseDown) {
            worldCords.set(mousePosX, mousePosY);
            viewport.unproject(worldCords);

            camCordsDifference.x = initialWorldCords.x - worldCords.x;
            camCordsDifference.y = initialWorldCords.y - worldCords.y;

            camVelocityX = camCordsDifference.x * camPanLerp;
            camVelocityY = camCordsDifference.y * camPanLerp;
        } else {
            camVelocityX *= camFriction;
            camVelocityY *= camFriction;

            if (Math.abs(camVelocityX) < 0.01f) camVelocityX = 0;
            if (Math.abs(camVelocityY) < 0.01f) camVelocityY = 0;

            if (camVelocityX == 0 && camVelocityY == 0) return;
        }

        float expectedX = camera.position.x + camVelocityX;
        float expectedY = camera.position.y + camVelocityY;

        camera.translate(camVelocityX, camVelocityY);
        camClamp(camera);

        if (Math.abs(camera.position.x - expectedX) > 0.001f) camVelocityX = 0;
        if (Math.abs(camera.position.y - expectedY) > 0.001f) camVelocityY = 0;

        camera.update();
    }

    // Function made with assistance from Claude Sonnet 5.5
    public static void camClamp(OrthographicCamera camera) {
        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        if (effectiveViewportWidth >= Game.WORLD_WIDTH) {
            camera.position.x = Game.WORLD_WIDTH / 2f;
        } else {
            camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f, Game.WORLD_WIDTH - effectiveViewportWidth / 2f);
        }

        if (effectiveViewportHeight >= Game.WORLD_HEIGHT) {
            camera.position.y = Game.WORLD_HEIGHT / 2f;
        } else {
            camera.position.y = MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f, Game.WORLD_HEIGHT - effectiveViewportHeight / 2f);
        }
    }
}