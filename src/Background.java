import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Background {
    private final float r, g, b, a;
    private Texture overlayTexture;

    public Background(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        overlayTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void render() {
        Gdx.gl.glClearColor(r, g, b, a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public void drawOverlay(SpriteBatch batch, float width, float height) {
        batch.setColor(1f, 1f, 1f, 0.3f);
        batch.draw(overlayTexture, 0, 0, width, height);
        batch.setColor(1f, 1f, 1f, 1f);
    }
    
    public void dispose() {
        if (overlayTexture != null) {
            overlayTexture.dispose();
        }
    }
}

