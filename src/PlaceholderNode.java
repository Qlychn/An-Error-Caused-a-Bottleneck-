import com.badlogic.gdx.graphics.Texture;

public class PlaceholderNode extends Node {
    public static final String TEXTURE_PATH = "src/assets/placeholderNode.png";
    private static Texture sharedTexture;

    public PlaceholderNode(float x, float y) {
        super(x, y, "PLACEHOLDER", 1f, 1f, 1f, 0f, 300f, 4, 1);
        setTexture(getSharedTexture());
    }

    private static Texture getSharedTexture() {
        if (sharedTexture == null) {
            sharedTexture = new Texture(TEXTURE_PATH);
        }
        return sharedTexture;
    }

//Dispose da shared texture. Call once when game closed 
    public static void disposeTexture() {
        if (sharedTexture != null) {
            sharedTexture.dispose();
            sharedTexture = null;
        }
    }
}