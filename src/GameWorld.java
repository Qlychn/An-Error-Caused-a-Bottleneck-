import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
 
public class GameWorld {
 
    private static final String SKIN_ATLAS_PATH = "src/assets/skin/skin.atlas";
    private static final float NODE_RING_RADIUS = 900f;
    private static final int NODE_COUNT = 3;
    private static final float USER_DRAW_SIZE = 110f;
    private static final float USER_SPEED_SCALE = 60f;   // User.speed is "units per tick"; this makes it units per second
    private static final float LINK_THICKNESS = 10f;
    private static final Color LINK_COLOR = new Color(0.72f, 0.58f, 1f, 0.55f);
 
    private final float worldW;
    private final float worldH;
 
    private final TextureAtlas skinAtlas;           // the Skin Composer atlas, loaded only to reach its "white-pixel" region
    private final TextureRegion pixel;              // 1x1 white from the skin, used for the links and the background colour
    private final WorldBackground background;       // animated, covers the whole world
    private final PlaceholderNode[] nodes;
    private final User playerUser;                

    private final Vector2 userPos = new Vector2();
    private final Vector2 userTargetPos = new Vector2();
    private int userTarget = 0;
 
    public GameWorld(float worldW, float worldH, User playerUser) {
        this.worldW = worldW;
        this.worldH = worldH;
        this.playerUser = playerUser;
 
        skinAtlas = new TextureAtlas(Gdx.files.internal(SKIN_ATLAS_PATH));
        pixel = new TextureRegion(skinAtlas.findRegion("white-pixel"));
        float u = (pixel.getU() + pixel.getU2()) / 2f;     // sample the centre of the texel only, so the
        float v = (pixel.getV() + pixel.getV2()) / 2f;     // linear filter can't blend in the atlas padding
        pixel.setRegion(u, v, u, v);
 
        background = new WorldBackground(worldW, worldH, pixel);
 
        nodes = new PlaceholderNode[NODE_COUNT];
        for (int i = 0; i < NODE_COUNT; i++) {
            float angle = 90f + i * (360f / NODE_COUNT);
            nodes[i] = new PlaceholderNode(
                    worldW / 2f + MathUtils.cosDeg(angle) * NODE_RING_RADIUS,
                    worldH / 2f + MathUtils.sinDeg(angle) * NODE_RING_RADIUS);
        }
        for (int i = 0; i < NODE_COUNT; i++) {
            nodes[i].setConnection(0, nodes[(i + 1) % NODE_COUNT]);
            nodes[i].setConnection(1, nodes[(i + NODE_COUNT - 1) % NODE_COUNT]);
        }
 
        userPos.set(nodes[0].getX(), nodes[0].getY());
        pickNextTarget();
    }
 
    public void update(float delta, boolean gameRunning) {
        background.update(delta);
        if (gameRunning) {
            playerUser.update(delta);
            playerUser.moveNode(userPos, userTargetPos, playerUser.getSpeed() * USER_SPEED_SCALE * delta);
            if (userPos.epsilonEquals(userTargetPos)) pickNextTarget();
        }
    }
 
    public void draw(SpriteBatch batch) {
        background.draw(batch);
 
        for (int i = 0; i < NODE_COUNT; i++) {
            drawLink(batch, nodes[i], nodes[(i + 1) % NODE_COUNT]);
        }
        for (PlaceholderNode placeholderNode : nodes) {
            placeholderNode.draw(batch);
        }
 
        Texture userTexture = playerUser.getUserPlaceholderTexture();
        if (userTexture != null) {
            batch.draw(userTexture, userPos.x - USER_DRAW_SIZE / 2f, userPos.y - USER_DRAW_SIZE / 2f,
                    USER_DRAW_SIZE, USER_DRAW_SIZE);
        }
    }
 
    private void pickNextTarget() {
        userTarget = (userTarget + 1) % NODE_COUNT;
        userTargetPos.set(nodes[userTarget].getX(), nodes[userTarget].getY());
    }
 
    private void drawLink(SpriteBatch batch, Node a, Node b) {
        float dx = b.getX() - a.getX();
        float dy = b.getY() - a.getY();
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
        batch.setColor(LINK_COLOR);
        batch.draw(pixel, a.getX(), a.getY() - LINK_THICKNESS / 2f, 0f, LINK_THICKNESS / 2f,
                length, LINK_THICKNESS, 1f, 1f, angle);
        batch.setColor(Color.WHITE);
    }
 
    public void dispose() {
        background.dispose();
        skinAtlas.dispose();                       
        PlaceholderNode.disposeTexture();          
    }
}