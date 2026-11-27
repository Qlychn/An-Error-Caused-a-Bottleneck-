package scripts.control;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
    private final Stage inGameUIStage;
    private Table inGameUITable;    
    private Table inGameUITableBGShowing;
    private Table inGameUITableBGHidden;

    private Image inGameUIBG;
    private ImageButton inGameUIBGControlButtonDown;
    private ImageButton inGameUIBGControlButtonUp;


    private Skin InGameUIButtinBarBG;
    private Skin inGameUIBGControlDown;
    private Skin inGameUIBGControlUp;
    boolean showing = false;

    private TextButton nodesButton, usersButton, errorsButton, menuButton;


    public InGameUI(Stage stage) {
        this.inGameUIStage = stage;

        InGameUIButtinBarBG = new Skin(Gdx.files.internal("src/assets/skin/InGameUIButtinBarBG.json"));
        inGameUIBGControlDown = new Skin(Gdx.files.internal("src/assets/skin/InGameUIBottomBarBGControlDown.json"));
        inGameUIBGControlUp = new Skin(Gdx.files.internal("src/assets/skin/InGameUIBottomBarBGControlUp.json"));

        initBottomBar();
    }

    public void initBottomBar(){

        inGameUIBGControlButtonDown = new ImageButton(inGameUIBGControlDown);
        inGameUIBGControlButtonUp = new ImageButton(inGameUIBGControlUp);
        inGameUIBG = new Image(InGameUIButtinBarBG.getDrawable("InGameUIBottomBarBG"));

        // nodesButton = new ImageButton("Nodes", style);
        // usersButton = new ImageButton("Users", style);
        // errorsButton = new ImageButton("Errors", style);
        // menuButton = new ImageButton("Menu", style);

        inGameUITableBGHidden = new Table();
        inGameUITableBGHidden.bottom();
        inGameUITableBGHidden.add(inGameUIBGControlButtonUp).height(30).width(50);
        inGameUITableBGHidden.setFillParent(true);
        inGameUITableBGHidden.setDebug(true);

        inGameUITableBGShowing = new Table();
        inGameUITableBGShowing.bottom();
        inGameUITableBGShowing.add(inGameUIBGControlButtonDown).height(30).width(50);
        inGameUITableBGShowing.row();
        inGameUITableBGShowing.add(inGameUIBG).growX().height(100);
        inGameUITableBGShowing.setFillParent(true);
        inGameUITableBGShowing.setDebug(true);

        inGameUIBGControlButtonDown.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hideInGameUI();
            }
        });

        inGameUIBGControlButtonUp.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showInGameUI();
            }
        });

        inGameUITable = new Table();
        inGameUITable.align(Align.bottom);
        inGameUITable.add(nodesButton).width(90).height(90).pad(0, 15, 3,15);
        inGameUITable.add(usersButton).width(90).height(90).pad(0, 15, 3,15);
        inGameUITable.add(errorsButton).width(90).height(90).pad(0, 15, 3,15);
        inGameUITable.add(menuButton).width(90).height(90).pad(0, 15, 3,15);
        inGameUITable.setFillParent(true);
        inGameUITable.setDebug(true);

    }

    public void showInGameUI() {
        Game.multiplexer.removeProcessor(inGameUIStage);
        Game.multiplexer.addProcessor(0, inGameUIStage);
        inGameUITableBGHidden.remove();
        inGameUIStage.addActor(inGameUITable);
        inGameUIStage.addActor(inGameUITableBGShowing);
        showing = true;
    }

    public void hideInGameUI() {
        Game.multiplexer.removeProcessor(inGameUIStage);
        Game.multiplexer.addProcessor(0, inGameUIStage);
        inGameUITable.remove();
        inGameUITableBGShowing.remove();
        inGameUIStage.addActor(inGameUITableBGHidden);
        showing = false;
    }

    public void hideAllInGameUI() {
        inGameUITable.remove();
        inGameUITableBGShowing.remove();
        inGameUITableBGHidden.remove();
        Game.multiplexer.removeProcessor(inGameUIStage);
        showing = false;
    }


    public boolean isLoaded(){
        return showing;
    }

    public void dispose(){
        InGameUIButtinBarBG.dispose();
    }
}