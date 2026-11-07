import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import java.util.Random;

/**
 * Animated background that covers the WHOLE world (Game.WORLD_WIDTH x Game.WORLD_HEIGHT),
 * drawn with the game's OrthographicCamera so it pans and zooms with the world.
 */
public class WorldBackground {

    private static final int TILE = 256;           // grid texture size (power of two so it can repeat)
    private static final int DOT_COUNT = 420;

    private static final Color BASE_A = new Color(0.00f, 0.00f, 0.05f, 1f);
    private static final Color BASE_B = new Color(0.03f, 0.01f, 0.09f, 1f);
    private static final Color GRID_TINT = new Color(0.45f, 0.55f, 1f, 1f);
    private static final Color[] DOT_TINTS = {
            new Color(0.55f, 0.70f, 1.00f, 1f),
            new Color(0.72f, 0.58f, 1.00f, 1f),
            new Color(0.45f, 0.95f, 0.85f, 1f),
    };

    private final float worldW;
    private final float worldH;

    // Generated cache. Not final so disposeGeneratedCache() can free it and null it out (null = disposed).
    private Texture pixel;                         // 1x1 white, stretched over the world for the base colour
    private Texture grid;                          // repeating tile with faint lines
    private Texture dot;                           // soft round glow

    // PLACEHOLDER for the animated background asset (sprite sheet). Stays null until loadAnimatedAsset() is called.
    private Texture animatedSheet;
    private Animation<TextureRegion> animatedBg;

    private final float[] dotX = new float[DOT_COUNT];
    private final float[] dotY = new float[DOT_COUNT];
    private final float[] dotVX = new float[DOT_COUNT];
    private final float[] dotVY = new float[DOT_COUNT];
    private final float[] dotSize = new float[DOT_COUNT];
    private final float[] dotPhase = new float[DOT_COUNT];
    private final float[] dotPulse = new float[DOT_COUNT];
    private final byte[] dotTint = new byte[DOT_COUNT];

    private final Color base = new Color();
    private float time = 0f;

    public WorldBackground(float worldW, float worldH) {
        this.worldW = worldW;
        this.worldH = worldH;

        pixel = makePixel();
        grid = makeGridTile();
        dot = makeDot();

        Random random = new Random(7);              // fixed seed -> same layout every launch
        for (int i = 0; i < DOT_COUNT; i++) {
            dotX[i] = random.nextFloat() * worldW;
            dotY[i] = random.nextFloat() * worldH;
            float angle = random.nextFloat() * MathUtils.PI2;
            float speed = 8f + random.nextFloat() * 28f;     // world units per second
            dotVX[i] = MathUtils.cos(angle) * speed;
            dotVY[i] = MathUtils.sin(angle) * speed;
            dotSize[i] = 40f + random.nextFloat() * 120f;
            dotPhase[i] = random.nextFloat() * MathUtils.PI2;
            dotPulse[i] = 0.4f + random.nextFloat() * 0.9f;  // pulses per ~6 seconds
            dotTint[i] = (byte) random.nextInt(DOT_TINTS.length);
        }
    }

    public void update(float delta) {
        time += delta;
        if (dot == null) return;                    // generated cache was disposed, nothing procedural to move
        for (int i = 0; i < DOT_COUNT; i++) {
            dotX[i] += dotVX[i] * delta;
            dotY[i] += dotVY[i] * delta;
            if (dotX[i] < -dotSize[i]) dotX[i] = worldW + dotSize[i];
            else if (dotX[i] > worldW + dotSize[i]) dotX[i] = -dotSize[i];
            if (dotY[i] < -dotSize[i]) dotY[i] = worldH + dotSize[i];
            else if (dotY[i] > worldH + dotSize[i]) dotY[i] = -dotSize[i];
        }
    }

    /** Draws into an already-begun batch that uses the world camera. Leaves the batch colour white. */
    public void draw(SpriteBatch batch) {
        if (animatedBg != null) {                   // real asset loaded -> stretch the current frame over the world
            batch.setColor(Color.WHITE);
            batch.draw(animatedBg.getKeyFrame(time, true), 0f, 0f, worldW, worldH);
            return;
        }
        if (pixel == null || grid == null || dot == null) return;   // cache disposed and no asset yet -> nothing to draw

        float breathe = 0.5f + 0.5f * MathUtils.sin(time * 0.15f);
        base.set(BASE_A).lerp(BASE_B, breathe);
        batch.setColor(base);
        batch.draw(pixel, 0f, 0f, worldW, worldH);

        drawGrid(batch, 1f, 0.07f, time * 18f, time * 6f);       // fine grid, drifts right/down
        drawGrid(batch, 4f, 0.10f, -time * 10f, time * 4f);      // big grid, drifts the other way (parallax)

        for (int i = 0; i < DOT_COUNT; i++) {
            float pulse = 0.5f + 0.5f * MathUtils.sin(time * dotPulse[i] + dotPhase[i]);
            Color tint = DOT_TINTS[dotTint[i]];
            float size = dotSize[i] * (0.85f + 0.3f * pulse);
            batch.setColor(tint.r, tint.g, tint.b, 0.10f + 0.28f * pulse);
            batch.draw(dot, dotX[i] - size / 2f, dotY[i] - size / 2f, size, size);
        }

        batch.setColor(Color.WHITE);
    }

    /** Draws the repeating grid across the whole world, scrolled by (scrollX, scrollY) world units. */
    private void drawGrid(SpriteBatch batch, float cellScale, float alpha, float scrollX, float scrollY) {
        float cell = TILE * cellScale;                // world units covered by one tile
        float u = scrollX / cell;
        float v = scrollY / cell;
        u -= MathUtils.floor(u);                      // keep the numbers small so floats stay precise
        v -= MathUtils.floor(v);

        batch.setColor(GRID_TINT.r, GRID_TINT.g, GRID_TINT.b, alpha);
        batch.draw(grid, 0f, 0f, worldW, worldH, u, v, u + worldW / cell, v + worldH / cell);
    }

    /**
     * PLACEHOLDER: load the animated background from a sprite sheet (frames laid out in a grid, read left->right,
     * top->bottom). Not called anywhere yet. Once the asset exists, call this, then disposeGeneratedCache().
     * Example: background.loadAnimatedAsset("world_bg.png", 8, 4, 1f / 12f);
     */
    public void loadAnimatedAsset(String path, int frameCols, int frameRows, float frameDuration) {
        unloadAnimatedAsset();                     
        animatedSheet = new Texture(Gdx.files.internal(path));
        animatedSheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureRegion[][] grid2d = TextureRegion.split(animatedSheet,
                animatedSheet.getWidth() / frameCols, animatedSheet.getHeight() / frameRows);
        TextureRegion[] frames = new TextureRegion[frameCols * frameRows];
        int n = 0;
        for (int row = 0; row < frameRows; row++) {
            for (int col = 0; col < frameCols; col++) frames[n++] = grid2d[row][col];
        }
        animatedBg = new Animation<TextureRegion>(frameDuration, frames);
        animatedBg.setPlayMode(Animation.PlayMode.LOOP);
    }

    /** Frees the animated asset (if any) and goes back to the generated background. Safe to call repeatedly. */
    public void unloadAnimatedAsset() {
        if (animatedSheet != null) animatedSheet.dispose();
        animatedSheet = null;
        animatedBg = null;
    }

    /**
     * Disposes the cache used to generate the background (mi pc almost crashed ). Safe to call more than once.
     */
    public void disposeGeneratedCache() {
        if (pixel != null) pixel.dispose();
        if (grid != null) grid.dispose();
        if (dot != null) dot.dispose();
        pixel = null;
        grid = null;
        dot = null;
    }

    public void dispose() {
        disposeGeneratedCache();
        unloadAnimatedAsset();
    }

    // ------------------------------------------------------------------ generated textures

    private static Texture makePixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private static Texture makeGridTile() {
        Pixmap pixmap = new Pixmap(TILE, TILE, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fillRectangle(0, 0, TILE, 2);          // top line
        pixmap.fillRectangle(0, 0, 2, TILE);          // left line
        pixmap.fillRectangle(0, 0, 8, 8);             // brighter node at every crossing
        Texture texture = new Texture(pixmap, true);  // true = generate mipmaps (no shimmer when zoomed out)
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        return texture;
    }

    private static Texture makeDot() {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        float r = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = (float) Math.sqrt((x + 0.5f - r) * (x + 0.5f - r) + (y + 0.5f - r) * (y + 0.5f - r)) / r;
                float a = Math.max(0f, 1f - d);
                a = a * a;                            // soft falloff
                pixmap.drawPixel(x, y, Color.rgba8888(1f, 1f, 1f, a));
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }
}