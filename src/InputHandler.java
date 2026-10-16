import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

// Input processor taken form official libGDX wiki (event handling)
// https://libgdx.com/wiki/input/event-handling

public class InputHandler extends InputAdapter {
   public boolean keyDown (int keycode) {
      return false;
   }

   public boolean keyUp (int keycode) {
      return false;
   }

   public boolean keyTyped (char character) {
      return false;
   }

   public boolean touchDown(int x, int y, int pointer, int button) {
      CamControls.camPanStart(Game.viewport, x, y);
      return true;
   }

   public boolean touchDragged(int x, int y, int pointer) {
      CamControls.mousePosX = x;
      CamControls.mousePosY = y;
      return true;
   }

   public boolean touchUp(int x, int y, int pointer, int button) {
      CamControls.mouseDown = false;
      return true;
   }

   public boolean mouseMoved (int x, int y) {
      return false;
   }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        /*if (amountY > 0) {
            System.out.println("Scrolled Down: " + amountY);
        } else if (amountY < 0) {

        } */

        CamControls.camZoomChange(amountY);
        
        return true; // Return true to signal that the event has been handled
    }
}