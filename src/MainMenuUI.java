import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;

public class MainMenuUI {
    private final Stage uiStage;
    private final TextButtonStyle style;
    private Table mainMenuTable;
    private Table optionsTable;
    private Table libraryTable;
    boolean mainMenuShowing = true;
    boolean optionsShowing = false;

    private int masterVol = 100;    
    private int sfxVol = 100;
    private int musicVol = 100;

    private SelectBox<String> monitorSelect;
    private TextButton startButton, libraryButton, optionsButton, exitButton;
    private TextButton masterVolButton, sfxVolButton, musicVolButton, windowedButton, fullScreenButton, backButton;
    private TextButton errorsButton, usersButton, nodesButton, libraryBackButton;

    Skin skin;

    public MainMenuUI(Stage stage, BitmapFont font) {
        this.uiStage = stage;

        skin = new Skin();
        skin.add("Placeholder", font, BitmapFont.class);
        skin.addRegions(new TextureAtlas(Gdx.files.internal("src/assets/skin/skin.atlas")));
        skin.load(Gdx.files.internal("src/assets/skin/skin.json"));

        style = new TextButtonStyle();
        style.font = font;

        initOptionsMenu();
        initLibrary();

        startButton = new TextButton("Start", style);
        libraryButton = new TextButton("Library", style);
        optionsButton = new TextButton("Options", style);
        exitButton = new TextButton("Exit", style);

        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
               unloadMainMenu();
               unloadOptionsMenu();
            }
        });

        libraryButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                loadLibrary();
            }
        });

        optionsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent                                             event, Actor actor) {
               if (optionsShowing == true){
                unloadOptionsMenu();
               }else{
                loadOptionsMenu();
               }

            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });

        mainMenuTable = new Table();
        mainMenuTable.align(Align.left);
        mainMenuTable.setFillParent(true);
        mainMenuTable.row();
        mainMenuTable.add(startButton).width(500).height(140).pad(10);
        mainMenuTable.row();
        mainMenuTable.add(libraryButton).width(500).height(140).pad(10);
        mainMenuTable.row();
        mainMenuTable.add(optionsButton).width(500).height(140).pad(10);
        mainMenuTable.row();
        mainMenuTable.add(exitButton).width(500).height(140).pad(10);
        //mainMenuTable.setDebug(true);
    }

    public void initOptionsMenu(){

        Monitor[] monitors = Gdx.graphics.getMonitors();

        String[] monitorNames = new String[monitors.length];
        for (int i = 0; i < monitors.length; i++) {
            monitorNames[i] = (i + 1) + ": " + monitors[i].name;
        }

        monitorSelect = new SelectBox<String>(skin);
        monitorSelect.setItems(monitorNames);

        Monitor current = Gdx.graphics.getMonitor();
        for (int i = 0; i < monitors.length; i++) {
            if (monitors[i].virtualX == current.virtualX && monitors[i].virtualY == current.virtualY) {
                monitorSelect.setSelectedIndex(i);
                break;
            }
        }

        String masterVolText = String.valueOf(masterVol);
        String sfxVolText = String.valueOf(sfxVol);
        String musicVolTest = String.valueOf(musicVol);

        masterVolButton = new TextButton("Master (" + masterVolText + "%)", style);
        sfxVolButton = new TextButton("SFX (" + sfxVolText + "%)", style);
        musicVolButton = new TextButton("Music (" + musicVolTest + "%)", style);

        Slider masterVolSlider = new Slider(0, 100, 1, false, skin);
        Slider sfxVolSlider = new Slider(0, 100, 1, false, skin);
        Slider musicVolSlider = new Slider(0, 100, 1, false, skin);

        masterVolSlider.setValue(masterVol);
        sfxVolSlider.setValue(sfxVol);
        musicVolSlider.setValue(musicVol);

        windowedButton = new TextButton("Windowed", style);
        fullScreenButton = new TextButton("Fullscreen", style);
        backButton = new TextButton("Back", style);

        monitorSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Monitor target = monitors[monitorSelect.getSelectedIndex()];
                DisplayMode mode = Gdx.graphics.getDisplayMode(target);

                if (Gdx.graphics.isFullscreen()) {
                    Gdx.graphics.setFullscreenMode(mode);
                } else {
                    int x = target.virtualX + (mode.width - Gdx.graphics.getWidth()) / 2;
                    int y = target.virtualY + (mode.height - Gdx.graphics.getHeight()) / 2;
                    ((Lwjgl3Graphics) Gdx.graphics).getWindow().setPosition(x, y);
                }
            }
        });

        masterVolSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                masterVol = (int) masterVolSlider.getValue();
                masterVolButton.setText("Master (" + masterVol + "%)");
            }
        });
        sfxVolSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sfxVol = (int) sfxVolSlider.getValue();
                sfxVolButton.setText("SFX (" + sfxVol + "%)");
            }
        });
        musicVolSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                musicVol = (int) musicVolSlider.getValue();
                musicVolButton.setText("Music (" + musicVol + "%)");
            }
        });
        if(Gdx.graphics.supportsDisplayModeChange()) {
        windowedButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
               Gdx.graphics.setWindowedMode(1280, 720);
            }
        });
        fullScreenButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Monitor currMonitor = Gdx.graphics.getMonitor();
                DisplayMode displayMode = Gdx.graphics.getDisplayMode(currMonitor);
                if(!Gdx.graphics.setFullscreenMode(displayMode)) {
                // switching to full-screen mode failed
                }
            }
        });
        }
        
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
               unloadOptionsMenu();
            }
        });

        optionsTable = new Table();
        optionsTable.align(Align.right);
        optionsTable.add(monitorSelect).width(600).height(60).pad(10).colspan(2);
        optionsTable.row();
        optionsTable.add(masterVolButton).width(200).height(100).pad(10);
        optionsTable.add(masterVolSlider).width(400).height(40).pad(10);
        optionsTable.row();
        optionsTable.add(sfxVolButton).width(200).height(100).pad(10);
        optionsTable.add(sfxVolSlider).width(400).height(40).pad(10);
        optionsTable.row();
        optionsTable.add(musicVolButton).width(200).height(100).pad(10);
        optionsTable.add(musicVolSlider).width(400).height(40).pad(10);
        optionsTable.row();
        optionsTable.add(windowedButton).width(300).height(100).pad(10);
        optionsTable.add(fullScreenButton).width(300).height(100).pad(10);
        optionsTable.row();
        optionsTable.add(backButton).width(600).height(100).pad(10).colspan(2);
        optionsTable.setFillParent(true);
        //optionsTable.setDebug(true);

    }

    public void loadOptionsMenu(){
        uiStage.addActor(optionsTable);
        optionsShowing = true;
    }

    public void unloadOptionsMenu(){
        optionsTable.remove();
        optionsShowing = false;
    }

    public void initLibrary(){

        errorsButton = new TextButton("Errors", style);
        usersButton = new TextButton("Users", style);
        nodesButton = new TextButton("Nodes", style);
        libraryBackButton = new TextButton("Back", style);

        libraryBackButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                unloadLibrary();
            }
        });

        libraryTable = new Table();
        libraryTable.align(Align.center);
        libraryTable.add(errorsButton).width(200).height(100).pad(10);
        libraryTable.add(usersButton).width(200).height(100).pad(10);
        libraryTable.add(nodesButton).width(200).height(100).pad(10);
        libraryTable.row();
        libraryTable.add(libraryBackButton).width(200).height(100).pad(10).colspan(3);
        libraryTable.setFillParent(true);
        libraryTable.setDebug(true);

    }

    public void loadLibrary(){
        uiStage.addActor(libraryTable);
        mainMenuTable.remove();
        optionsTable.remove();
    }

    public void unloadLibrary(){
        libraryTable.remove();
        uiStage.addActor(mainMenuTable);
    }


    public void loadMainMenu(){
        Game.multiplexer.removeProcessor(Game.inputHandler);
        Game.multiplexer.addProcessor(Game.uiStage);
        uiStage.addActor(mainMenuTable);
        mainMenuShowing = true;
    }

    public void unloadMainMenu(){
        Game.multiplexer.removeProcessor(Game.uiStage);
        Game.multiplexer.addProcessor(Game.inputHandler);
        mainMenuTable.remove();
        mainMenuShowing = false;
    }

    public boolean isLoaded(){
        return mainMenuShowing;
    }
}