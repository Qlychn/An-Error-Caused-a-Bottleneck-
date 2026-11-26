package scripts.control;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import scripts.game.Game;

import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class InGameUI {
    private final Stage uiStage;
    private final TextButtonStyle style;
    private Table inGameUITable;
    private ImageButton inGameUIBG;
    boolean showing = false;

    private static final float barHeight = 250;

    private TextButton nodesButton, usersButton, errorsButton, menuButton;

    Skin skin;

    public InGameUI(Stage stage, BitmapFont font) {
        this.uiStage = stage;

        skin = new Skin();
        skin.add("Placeholder", font, BitmapFont.class);
        skin.addRegions(new TextureAtlas(Gdx.files.internal("src/assets/skin/skin.atlas")));
        skin.load(Gdx.files.internal("src/assets/skin/skin.json"));

        inGameUIBG = new ImageButton(skin);

        style = new TextButtonStyle();
        style.font = font;

        initBottomBar();
    }

    public void initBottomBar(){

        // nodesButton = new ImageButton("Nodes", style);
        // usersButton = new ImageButton("Users", style);
        // errorsButton = new ImageButton("Errors", style);
        // menuButton = new ImageButton("Menu", style);

        // menuButton.addListener(new ChangeListener() {
        //     @Override
        //     public void changed(ChangeEvent event, Actor actor) {
        //         unloadInGameUI();
        //         Game.mainMenu.loadMainMenu();
        //     }
        // });

        inGameUITable = new Table();
        inGameUITable.align(Align.bottomRight);
        // inGameUITable.add(nodesButton).width(200).height(100).pad(10);
        // inGameUITable.add(usersButton).width(200).height(100).pad(10);
        // inGameUITable.add(errorsButton).width(200).height(100).pad(10);
        // inGameUITable.add(menuButton).width(200).height(100).pad(10);
        // inGameUITable.setFillParent(true);
        inGameUITable.setDebug(true);

    }


    public void loadInGameUI(){
        Game.multiplexer.addProcessor(0, uiStage);
        uiStage.addActor(inGameUITable);
        showing = true;
    }

    public void unloadInGameUI(){
        Game.multiplexer.removeProcessor(uiStage);
        inGameUITable.remove();
        showing = false;
    }

    public boolean isLoaded(){
        return showing;
    }

    public void dispose(){
        skin.remove("Transparent", BitmapFont.class);
        skin.dispose();
    }
}