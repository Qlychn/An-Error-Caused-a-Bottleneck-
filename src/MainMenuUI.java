import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class MainMenuUI {

    // ============================================================================ byte IDs
    public static final byte BTN_START = 0;
    public static final byte BTN_OPTIONS = 1;
    public static final byte BTN_LIBRARY = 2;
    public static final byte BTN_EXIT = 3;

    public static final byte OPT_MONITOR = 0;
    public static final byte OPT_DISPLAY_MODE = 1;
    public static final byte OPT_MASTER_VOLUME = 2;
    public static final byte OPT_MUSIC_VOLUME = 3;
    public static final byte OPT_SFX_VOLUME = 4;

    public static final byte LIB_NODES = 0;
    public static final byte LIB_USERS = 1;
    public static final byte LIB_ERRORS = 2;

    // ================================================ EDIT THESE ARRAYS TO CHANGE THE MENU
    private static final byte[] MAIN_BUTTONS = { BTN_START, BTN_OPTIONS, BTN_LIBRARY, BTN_EXIT };

    private static final byte[] OPTIONS = {
            OPT_MONITOR, OPT_DISPLAY_MODE, OPT_MASTER_VOLUME, OPT_MUSIC_VOLUME, OPT_SFX_VOLUME
    };

    private static final byte[] LIBRARY_TABS = { LIB_NODES, LIB_USERS, LIB_ERRORS };

    // ============================================================ codex / log placeholder text
    private static final String[][] NODE_ENTRIES = {
            { "Triangle Node", "Placeholder: describe what this node does." },
            { "Hexagon Node", "Placeholder: describe what this node does." },
            { "Square Node", "Placeholder: describe what this node does." },
            { "Circle Node", "Placeholder: describe what this node does." },
    };
    private static final String[][] USER_ENTRIES = {
            { "Placeholder User", "A user moving through the pipeline. Has a rating, patience, error cause rate and speed." },
    };
    // Texts taken from the project mockups.
    private static final String[][] ERROR_ENTRIES = {
            { "Array index out of bounds", "Can't accept new users." },
            { "Disconnected from network", "The node lost its connection." },
            { "Too much redundancy in software", "User accept rate -30%." },
    };

    private static final String TITLE_TEXT = "An Error caused a bottleneck °^°";
    private static final String QUIT_TEXT = "Are you sure to quit this game?";

    // ================================================================================ layout
    private static final float UI_W = 1920f, UI_H = 1080f;   // minimum UI viewport (same as the game's)
    private static final float MAIN_X = 120f, MAIN_W = 440f;
    private static final float SIDE_X = 620f, OPTIONS_W = 820f, CODEX_W = 780f;
    private static final float LIB_W = 420f, LIB_MARGIN = 80f, PANEL_GAP = 30f;
    private static final float CONFIRM_W = 820f, CONFIRM_H = 340f;
    private static final float BTN_H = 104f, ROW_H = 88f, GAP = 14f, PAD = 36f, TITLE_H = 90f;
    private static final float BORDER_PX = 4f;
    private static final float TEXT_SCALE = 2.4f, SMALL_SCALE = 1.9f, HEAD_SCALE = 3.2f, TITLE_SCALE = 4.2f;
    private static final float LIBRARY_SLIDE_SECONDS = 0.35f;

    // ================================================================================ colors
    private static final Color DIM = new Color(0f, 0f, 0.03f, 0.62f);
    private static final Color DIM_MODAL = new Color(0f, 0f, 0f, 0.55f);
    private static final Color PANEL_FILL = new Color(0.05f, 0.05f, 0.11f, 0.92f);
    private static final Color BTN_FILL = new Color(0.10f, 0.10f, 0.19f, 1f);
    private static final Color HOVER_FILL = new Color(0.20f, 0.20f, 0.36f, 1f);
    private static final Color PRESSED_FILL = new Color(0.07f, 0.07f, 0.13f, 1f);
    private static final Color SELECTED_FILL = new Color(0.24f, 0.16f, 0.42f, 1f);
    private static final Color BORDER = new Color(0.62f, 0.62f, 0.74f, 1f);
    private static final Color ACCENT = new Color(0.72f, 0.58f, 1f, 1f);
    private static final Color TRACK = new Color(0.20f, 0.20f, 0.28f, 1f);
    private static final Color TEXT = new Color(0.95f, 0.95f, 1f, 1f);
    private static final Color MUTED = new Color(0.68f, 0.68f, 0.78f, 1f);

    // ================================================================================ state
    private final SpriteBatch batch;
    private final OrthographicCamera uiCamera = new OrthographicCamera();
    private final ExtendViewport uiViewport = new ExtendViewport(UI_W, UI_H, uiCamera);
    private final BitmapFont font;
    private final Texture pixel;                 // 1x1 white texture, tinted to draw every rectangle
    private final GlyphLayout layout = new GlyphLayout();
    private final Vector2 tmp = new Vector2();
    private final List<Panel> order = new ArrayList<>();

    private boolean loaded = false;
    private InputProcessor previousProcessor;
    private Runnable onStart;

    private Panel mainPanel, optionsPanel, libraryPanel, codexPanel, confirmPanel;
    private byte activeButton = -1;              // which main button's panel is open (-1 = none)
    private byte activeTab = -1;                 // which Library tab is showing (-1 = none)
    private float libraryProgress = 0f;          // 0 = off screen on the right, 1 = fully slid in
    private Widget pressed;                      // widget the mouse is currently held down on

    public MainMenuUI(SpriteBatch batch) {
        this.batch = batch;

        font = new BitmapFont();
        font.setUseIntegerPositions(false);
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();

        uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    // ============================================================================ public API

    /** Shows the main menu. No return value. */
    public void NodeMainUI() {
        if (loaded) return;
        loaded = true;
        previousProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(menuInput);
        loadMainButtons();
        layoutPanels();
    }

    /** Unloads the main menu (and anything opened from it) and gives input back to the game. */
    public void UnloadMainUI() {
        if (!loaded) return;
        unloadExit();
        unloadLibrary();
        unloadOptions();
        unloadStart();
        unloadMainButtons();
        activeButton = -1;
        pressed = null;
        loaded = false;
        if (Gdx.input.getInputProcessor() == menuInput) {
            Gdx.input.setInputProcessor(previousProcessor);
        }
    }

    public boolean isLoaded() { return loaded; }

    /** What Start does once the real game flow exists (e.g. open stage select). */
    public void setOnStart(Runnable onStart) { this.onStart = onStart; }

    /** Call from Game.resize(). */
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }

    /** Call from Game.render() after the world is drawn. Draws nothing while unloaded. */
    public void render(float delta) {
        if (!loaded) return;

        if (libraryPanel != null && libraryProgress < 1f) {
            libraryProgress = Math.min(1f, libraryProgress + delta / LIBRARY_SLIDE_SECONDS);
        }
        layoutPanels();
        updateHover();

        uiViewport.apply();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        float w = uiViewport.getWorldWidth();
        float h = uiViewport.getWorldHeight();
        drawRect(0, 0, w, h, DIM);
        drawLeft(TITLE_TEXT, MAIN_X, h - 140f, 140f, TITLE_SCALE, TEXT);

        for (Panel panel : panelsInOrder()) {
            if (panel == confirmPanel) drawRect(0, 0, w, h, DIM_MODAL);
            drawPanel(panel);
        }

        batch.setColor(Color.WHITE);
        batch.end();
    }

    /** Frees GPU resources. Call once from Game.dispose(). */
    public void dispose() {
        pixel.dispose();
        font.dispose();
    }

    // ===================================================== per-button load / unload (switch cases)

    private void loadButton(byte id) {
        switch (id) {
            case BTN_START:   loadStart();   break;
            case BTN_OPTIONS: loadOptions(); break;
            case BTN_LIBRARY: loadLibrary(); break;
            case BTN_EXIT:    loadExit();    break;
            default: System.err.println("Unknown main button id: " + id);
        }
    }

    private void unloadButton(byte id) {
        switch (id) {
            case BTN_START:   unloadStart();   break;
            case BTN_OPTIONS: unloadOptions(); break;
            case BTN_LIBRARY: unloadLibrary(); break;
            case BTN_EXIT:    unloadExit();    break;
            default: System.err.println("Unknown main button id: " + id);
        }
    }

    // ---- Start (functions ignored for now: it just closes the menu and calls the onStart hook)
    private void loadStart() {
        UnloadMainUI();
        if (onStart != null) onStart.run();
    }

    private void unloadStart() {
        // Nothing to unload yet. Later: remove the stage-select screen here.
    }

    // ---- Options
    private void loadOptions() {
        unloadOptions();
        optionsPanel = new Panel("Options", OPTIONS_W);
        for (byte id : OPTIONS) {
            addOption(optionsPanel, id);
        }
        optionsPanel.add(new UiButton("Back", ROW_H, this::closeActive));
        optionsPanel.pack(TITLE_H, PAD, GAP);
    }

    private void unloadOptions() {
        optionsPanel = null;
    }

    // ---- Library (slides in from the right)
    private void loadLibrary() {
        unloadLibrary();
        libraryProgress = 0f;
        libraryPanel = new Panel("Library", LIB_W);
        for (byte id : LIBRARY_TABS) {
            final byte tab = id;
            UiButton button = new UiButton(tabName(tab), ROW_H, () -> loadCodex(tab));
            button.selected = () -> activeTab == tab;
            libraryPanel.add(button);
        }
        libraryPanel.add(new UiButton("Back", ROW_H, this::closeActive));
        libraryPanel.pack(TITLE_H, PAD, GAP);
    }

    private void unloadLibrary() {
        unloadCodex();
        libraryPanel = null;
    }

    // ---- Library pages (Nodes / Users / Errors codex screens)
    private void loadCodex(byte tab) {
        unloadCodex();
        activeTab = tab;
        codexPanel = new Panel(codexTitle(tab), CODEX_W);
        for (String[] entry : codexEntries(tab)) {
            codexPanel.add(new UiLabel(entry[0], 46f, HEAD_SCALE * 0.8f, TEXT, false));
            codexPanel.add(new UiLabel(entry[1], 72f, SMALL_SCALE, MUTED, true));
        }
        codexPanel.pack(TITLE_H, PAD, 6f);
        layoutPanels();
    }

    private void unloadCodex() {
        codexPanel = null;
        activeTab = -1;
    }

    // ---- Exit (confirmation)
    private void loadExit() {
        unloadExit();
        confirmPanel = new Panel(QUIT_TEXT, CONFIRM_W);
        confirmPanel.h = CONFIRM_H;
        float buttonW = (CONFIRM_W - PAD * 2 - GAP * 2) / 2f;
        UiButton yes = new UiButton("Yes", BTN_H, () -> Gdx.app.exit());
        UiButton no = new UiButton("No", BTN_H, this::closeActive);
        confirmPanel.add(yes).place(PAD, PAD, buttonW);
        confirmPanel.add(no).place(PAD + buttonW + GAP * 2, PAD, buttonW);
    }

    private void unloadExit() {
        confirmPanel = null;
    }

    // ================================================================== main button stack

    private void loadMainButtons() {
        mainPanel = new Panel("", MAIN_W);
        for (byte id : MAIN_BUTTONS) {
            final byte button = id;
            mainPanel.add(new UiButton(mainButtonName(button), BTN_H, () -> openButton(button)));
        }
        mainPanel.pack(PAD, PAD, GAP);
    }

    private void unloadMainButtons() {
        mainPanel = null;
    }

    /** Opens a main button's panel (closing whichever one was open). Options/Library toggle. */
    private void openButton(byte id) {
        boolean toggles = id == BTN_OPTIONS || id == BTN_LIBRARY;
        boolean wasOpen = activeButton == id;
        closeActive();
        if (toggles && wasOpen) return;
        activeButton = id;
        loadButton(id);
        if (loaded) layoutPanels();  
    }

    private void closeActive() {
        if (activeButton >= 0) {
            unloadButton(activeButton);
            activeButton = -1;
        }
    }

    /** Escape: step back one level (confirm -> codex page -> open panel). */
    private void back() {
        if (confirmPanel != null || codexPanel == null) {
            closeActive();
        } else {
            unloadCodex();
        }
    }

    // ============================================================ option / label lookup (switch)

    private void addOption(Panel panel, byte id) {
        switch (id) {
            case OPT_MONITOR: {
                Graphics.Monitor[] monitors = Gdx.graphics.getMonitors();
                String[] names = new String[monitors.length];
                for (int i = 0; i < monitors.length; i++) {
                    String name = monitors[i].name == null ? "Monitor " + (i + 1) : monitors[i].name;
                    names[i] = name.length() > 24 ? name.substring(0, 21) + "..." : name;
                }
                panel.add(new UiCycle("Monitor", ROW_H, names,
                        () -> GameSettings.monitorIndex,
                        v -> { GameSettings.monitorIndex = (byte) v; applyDisplay(); }));
                break;
            }
            case OPT_DISPLAY_MODE:
                panel.add(new UiCycle("Display", ROW_H, new String[] { "Fullscreen", "Borderless Window" },
                        () -> GameSettings.displayMode,
                        v -> { GameSettings.displayMode = (byte) v; applyDisplay(); }));
                break;
            case OPT_MASTER_VOLUME:
                panel.add(new UiSlider("Master Volume", ROW_H,
                        () -> GameSettings.masterVolume, v -> GameSettings.masterVolume = (byte) v));
                break;
            case OPT_MUSIC_VOLUME:
                panel.add(new UiSlider("Music", ROW_H,
                        () -> GameSettings.musicVolume, v -> GameSettings.musicVolume = (byte) v));
                break;
            case OPT_SFX_VOLUME:
                panel.add(new UiSlider("SFX", ROW_H,
                        () -> GameSettings.sfxVolume, v -> GameSettings.sfxVolume = (byte) v));
                break;
            default:
                System.err.println("Unknown option id: " + id);
        }
    }

    private static String mainButtonName(byte id) {
        switch (id) {
            case BTN_START:   return "Start";
            case BTN_OPTIONS: return "Options";
            case BTN_LIBRARY: return "Library";
            case BTN_EXIT:    return "Exit";
            default:          return "?";
        }
    }

    private static String tabName(byte id) {
        switch (id) {
            case LIB_NODES:  return "Nodes";
            case LIB_USERS:  return "Users";
            case LIB_ERRORS: return "Errors";
            default:         return "?";
        }
    }

    private static String codexTitle(byte id) {
        switch (id) {
            case LIB_NODES:  return "Nodes Codex";
            case LIB_USERS:  return "Users Codex";
            case LIB_ERRORS: return "Errors Log";
            default:         return "";
        }
    }

    private static String[][] codexEntries(byte id) {
        switch (id) {
            case LIB_NODES:  return NODE_ENTRIES;
            case LIB_USERS:  return USER_ENTRIES;
            case LIB_ERRORS: return ERROR_ENTRIES;
            default:         return new String[0][];
        }
    }

    /** Applies GameSettings.monitorIndex + GameSettings.displayMode to the real window. */
    private void applyDisplay() {
        try {
            Graphics.Monitor[] monitors = Gdx.graphics.getMonitors();
            int index = Math.max(0, Math.min(GameSettings.monitorIndex, monitors.length - 1));
            Graphics.Monitor monitor = monitors[index];
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode(monitor);

            if (GameSettings.displayMode == GameSettings.DISPLAY_FULLSCREEN) {
                Gdx.graphics.setFullscreenMode(mode);
            } else {
                Gdx.graphics.setWindowedMode(mode.width, mode.height);
                Gdx.graphics.setUndecorated(true);
                if (Gdx.graphics instanceof Lwjgl3Graphics) {
                    ((Lwjgl3Graphics) Gdx.graphics).getWindow().setPosition(monitor.virtualX, monitor.virtualY);
                }
            }
        } catch (RuntimeException e) {
            System.err.println("Could not change display settings: " + e.getMessage());
        }
    }

    // ============================================================================ input

    private final InputProcessor menuInput = new InputAdapter() {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button != Input.Buttons.LEFT) return true;
            Vector2 p = uiViewport.unproject(tmp.set(screenX, screenY));
            pressed = widgetAt(p.x, p.y);
            if (pressed != null) pressed.onPress(p.x, p.y);
            return true;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (pressed != null) {
                Vector2 p = uiViewport.unproject(tmp.set(screenX, screenY));
                pressed.onDrag(p.x, p.y);
            }
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (button != Input.Buttons.LEFT) return true;
            Widget released = pressed;
            pressed = null;
            if (released != null) {
                Vector2 p = uiViewport.unproject(tmp.set(screenX, screenY));
                released.onRelease(p.x, p.y, released.contains(p.x, p.y));
            }
            return true;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) back();
            return true;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            return true;   // Meyks da world camera doesn't zoom behind the menu
        }
    };

    private Widget widgetAt(float px, float py) {
        List<Panel> panels = panelsInOrder();
        for (int i = panels.size() - 1; i >= 0; i--) {
            Panel panel = panels.get(i);
            if (confirmPanel != null && panel != confirmPanel) continue;   // modal: only the dialog is clickable
            for (int j = panel.widgets.size() - 1; j >= 0; j--) {
                Widget widget = panel.widgets.get(j);
                if (widget.interactive && widget.contains(px, py)) return widget;
            }
            if (panel == confirmPanel) return null;
        }
        return null;
    }

    private void updateHover() {
        Vector2 p = uiViewport.unproject(tmp.set(Gdx.input.getX(), Gdx.input.getY()));
        Widget top = widgetAt(p.x, p.y);
        for (Panel panel : panelsInOrder()) {
            for (Widget widget : panel.widgets) {
                widget.hovered = widget == top;
            }
        }
    }

    // ============================================================================ layout / draw

    private List<Panel> panelsInOrder() {
        order.clear();
        if (mainPanel != null) order.add(mainPanel);
        if (optionsPanel != null) order.add(optionsPanel);
        if (codexPanel != null) order.add(codexPanel);
        if (libraryPanel != null) order.add(libraryPanel);
        if (confirmPanel != null) order.add(confirmPanel);
        return order;
    }

    private void layoutPanels() {
        float w = uiViewport.getWorldWidth();
        float h = uiViewport.getWorldHeight();

        if (mainPanel != null) {
            mainPanel.x = MAIN_X;
            mainPanel.y = centeredY(h, mainPanel);
        }
        if (optionsPanel != null) {
            optionsPanel.x = SIDE_X;
            optionsPanel.y = centeredY(h, optionsPanel);
        }
        float libraryRestX = w - LIB_W - LIB_MARGIN;
        if (libraryPanel != null) {
            float t = Interpolation.pow3Out.apply(libraryProgress);
            libraryPanel.x = w + (libraryRestX - w) * t;           // slides in from the right edge
            libraryPanel.y = centeredY(h, libraryPanel);
        }
        if (codexPanel != null) {
            codexPanel.x = libraryRestX - PANEL_GAP - codexPanel.w;
            codexPanel.y = centeredY(h, codexPanel);
        }
        if (confirmPanel != null) {
            confirmPanel.x = (w - confirmPanel.w) / 2f;
            confirmPanel.y = (h - confirmPanel.h) / 2f;
        }
    }

    private static float centeredY(float worldH, Panel panel) {
        return (worldH - panel.h) / 2f - 30f;
    }

    private void drawPanel(Panel panel) {
        drawBox(panel.x, panel.y, panel.w, panel.h, PANEL_FILL, BORDER);
        if (!panel.title.isEmpty()) {
            if (panel == confirmPanel) {
                drawCentered(panel.title, panel.x, panel.y + panel.h - 190f, panel.w, 150f, HEAD_SCALE, TEXT);
            } else {
                drawLeft(panel.title, panel.x + PAD, panel.y + panel.h - TITLE_H, TITLE_H, HEAD_SCALE, TEXT);
            }
        }
        for (Widget widget : panel.widgets) {
            widget.draw(this);
        }
    }

    private void drawRect(float x, float y, float w, float h, Color color) {
        batch.setColor(color);
        batch.draw(pixel, x, y, w, h);
    }

    private void drawBox(float x, float y, float w, float h, Color fill, Color border) {
        drawRect(x, y, w, h, border);
        drawRect(x + BORDER_PX, y + BORDER_PX, w - BORDER_PX * 2, h - BORDER_PX * 2, fill);
    }

    /** Text centred inside the box (x, y, w, h). */
    private void drawCentered(String text, float x, float y, float w, float h, float scale, Color color) {
        font.getData().setScale(scale);
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, text, x + (w - layout.width) / 2f, y + (h + layout.height) / 2f);
    }

    private void drawLeft(String text, float x, float y, float h, float scale, Color color) {
        font.getData().setScale(scale);
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, text, x, y + (h + layout.height) / 2f);
    }

    private void drawWrapped(String text, float x, float yTop, float wrapWidth, float scale, Color color) {
        font.getData().setScale(scale);
        font.setColor(color);
        font.draw(batch, text, x, yTop, 0, text.length(), wrapWidth, Align.left, true);
    }

    // ============================================================================ widgets

    private static class Panel {
        final String title;
        final float w;
        float h, x, y;
        final List<Widget> widgets = new ArrayList<>();

        Panel(String title, float w) {
            this.title = title;
            this.w = w;
        }

        <T extends Widget> T add(T widget) {
            widget.owner = this;
            widgets.add(widget);
            return widget;
        }

        /** Stacks the widgets top to bottom under the title and sizes the panel to fit them. */
        void pack(float titleH, float pad, float gap) {
            float total = titleH + pad;
            for (Widget widget : widgets) total += widget.h;
            if (widgets.size() > 1) total += gap * (widgets.size() - 1);
            h = total;

            float top = h - titleH;
            for (Widget widget : widgets) {
                top -= widget.h;
                widget.rx = pad;
                widget.ry = top;
                widget.w = w - pad * 2;
                top -= gap;
            }
        }
    }

    private abstract static class Widget {
        Panel owner;
        float rx, ry, w, h;      // position relative to the panel's bottom-left corner
        boolean hovered;
        boolean interactive = true;

        Widget(float h) { this.h = h; }

        float x() { return owner.x + rx; }
        float y() { return owner.y + ry; }

        /** Manual placement (used instead of Panel.pack for side-by-side buttons). */
        Widget place(float rx, float ry, float w) {
            this.rx = rx;
            this.ry = ry;
            this.w = w;
            return this;
        }

        boolean contains(float px, float py) {
            return px >= x() && px <= x() + w && py >= y() && py <= y() + h;
        }

        void onPress(float px, float py) {}
        void onDrag(float px, float py) {}
        void onRelease(float px, float py, boolean inside) {}
        abstract void draw(MainMenuUI ui);
    }

    private static class UiButton extends Widget {
        final String label;
        final Runnable action;
        BooleanSupplier selected;

        UiButton(String label, float h, Runnable action) {
            super(h);
            this.label = label;
            this.action = action;
        }

        @Override
        void onRelease(float px, float py, boolean inside) {
            if (inside) action.run();
        }

        @Override
        void draw(MainMenuUI ui) {
            Color fill = ui.pressed == this ? PRESSED_FILL : hovered ? HOVER_FILL : BTN_FILL;
            if (selected != null && selected.getAsBoolean()) fill = SELECTED_FILL;
            ui.drawBox(x(), y(), w, h, fill, hovered ? ACCENT : BORDER);
            ui.drawCentered(label, x(), y(), w, h, TEXT_SCALE, TEXT);
        }
    }

    private static class UiSlider extends Widget {
        final String label;
        final IntSupplier get;
        final IntConsumer set;

        UiSlider(String label, float h, IntSupplier get, IntConsumer set) {
            super(h);
            this.label = label;
            this.get = get;
            this.set = set;
        }

        float trackX() { return x() + w * 0.46f; }
        float trackW() { return w * 0.38f; }

        void setFromX(float px) {
            float t = (px - trackX()) / trackW();
            t = Math.max(0f, Math.min(1f, t));
            set.accept(Math.round(t * 100f));
        }

        @Override void onPress(float px, float py) { setFromX(px); }
        @Override void onDrag(float px, float py) { setFromX(px); }

        @Override
        void draw(MainMenuUI ui) {
            ui.drawBox(x(), y(), w, h, BTN_FILL, hovered ? ACCENT : BORDER);
            ui.drawLeft(label, x() + 28f, y(), h, TEXT_SCALE, TEXT);

            float value = get.getAsInt();
            float t = value / 100f;
            float trackY = y() + h / 2f - 7f;
            ui.drawRect(trackX(), trackY, trackW(), 14f, TRACK);
            ui.drawRect(trackX(), trackY, trackW() * t, 14f, ACCENT);
            ui.drawRect(trackX() + trackW() * t - 8f, y() + h / 2f - 22f, 16f, 44f, TEXT);
            ui.drawLeft(String.valueOf((int) value), trackX() + trackW() + 28f, y(), h, TEXT_SCALE, TEXT);
        }
    }

    /** Row that cycles through a list of choices: click the left half for previous, right half for next. */
    private static class UiCycle extends Widget {
        final String label;
        final String[] choices;
        final IntSupplier get;
        final IntConsumer set;

        UiCycle(String label, float h, String[] choices, IntSupplier get, IntConsumer set) {
            super(h);
            this.label = label;
            this.choices = choices;
            this.get = get;
            this.set = set;
        }

        float valueX() { return x() + w * 0.46f; }
        float valueW() { return w * 0.54f - 20f; }

        @Override
        void onRelease(float px, float py, boolean inside) {
            if (!inside || choices.length == 0) return;
            int direction = px < valueX() + valueW() / 2f ? -1 : 1;
            int next = (get.getAsInt() + direction + choices.length) % choices.length;
            set.accept(next);
        }

        @Override
        void draw(MainMenuUI ui) {
            ui.drawBox(x(), y(), w, h, BTN_FILL, hovered ? ACCENT : BORDER);
            ui.drawLeft(label, x() + 28f, y(), h, TEXT_SCALE, TEXT);

            int index = Math.max(0, Math.min(get.getAsInt(), choices.length - 1));
            String value = choices.length == 0 ? "-" : choices[index];
            ui.drawCentered(value, valueX(), y(), valueW(), h, 2.0f, TEXT);
            ui.drawLeft("<", valueX(), y(), h, TEXT_SCALE, ACCENT);
            ui.drawCentered(">", valueX() + valueW() - 40f, y(), 40f, h, TEXT_SCALE, ACCENT);
        }
    }

    private static class UiLabel extends Widget {
        final String text;
        final float scale;
        final Color color;
        final boolean wrap;

        UiLabel(String text, float h, float scale, Color color, boolean wrap) {
            super(h);
            this.text = text;
            this.scale = scale;
            this.color = color;
            this.wrap = wrap;
            this.interactive = false;
        }

        @Override
        void draw(MainMenuUI ui) {
            if (wrap) {
                ui.drawWrapped(text, x(), y() + h, w, scale, color);
            } else {
                ui.drawLeft(text, x(), y(), h, scale, color);
            }
        }
    }
}