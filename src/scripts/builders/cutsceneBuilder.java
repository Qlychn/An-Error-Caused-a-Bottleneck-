package scripts.builders;
// most of these were done with the help of Gemini AI, but some were done with the help of the official libGDX wiki/documentation and gitHub
// and from copying how the MainMenuBuilder.java was done, since it is a similar builder class
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import scripts.game.Game;

class CutsceneBuilder {

    public static class DialogueStep {
        public final String speaker;
        public final String text;
        public final String bgPath;
        public final String portraitPath;

        public DialogueStep(String speaker, String text, String bgPath, String portraitPath) {
            this.speaker = speaker;
            this.text = text;
            this.bgPath = bgPath;
            this.portraitPath = portraitPath;
        }
    }

    public static class DialogueCutscene {
        private final Stage uiStage;
        private final Table rootTable;
        private final Image bgImage;
        private final Image portraitImage;
        private final Label speakerLabel;
        private final Label dialogueLabel;
        private final Array<DialogueStep> steps;
        private final Array<Texture> ownedTextures;
        private final Runnable onComplete;

        private int dialogueSequence = 0;
        private boolean active = false;

        private DialogueCutscene(
                Stage uiStage,
                Table rootTable,
                Image bgImage,
                Image portraitImage,
                Label speakerLabel,
                Label dialogueLabel,
                Array<DialogueStep> steps,
                Array<Texture> ownedTextures,
                Runnable onComplete) {
            this.uiStage = uiStage;
            this.rootTable = rootTable;
            this.bgImage = bgImage;
            this.portraitImage = portraitImage;
            this.speakerLabel = speakerLabel;
            this.dialogueLabel = dialogueLabel;
            this.steps = steps;
            this.ownedTextures = ownedTextures;
            this.onComplete = onComplete;
        }

        public void start() {
            if (steps.size == 0) {
                finish();
                return;
            }
            active = true;
            dialogueSequence = 0;

            Game.multiplexer.removeProcessor(Game.inputHandler);
            if (!Game.multiplexer.getProcessors().contains(uiStage, true)) {
                Game.multiplexer.addProcessor(uiStage);
            }

            uiStage.addActor(rootTable);
            uiStage.setKeyboardFocus(rootTable);
            applyStep(steps.get(0));
        }

        public void nextStep() {
            if (!active) return;
            dialogueSequence++;
            if (dialogueSequence >= steps.size) {
                finish();
            } else {
                applyStep(steps.get(dialogueSequence));
            }
        }

        private void applyStep(DialogueStep step) {
            speakerLabel.setText(step.speaker != null ? step.speaker : "");
            dialogueLabel.setText(step.text != null ? step.text : "");

            if (step.bgPath != null) {
                Texture tex = loadTexture(step.bgPath);
                bgImage.setDrawable(new TextureRegionDrawable(new TextureRegion(tex)));
            }
            if (step.portraitPath != null) {
                Texture tex = loadTexture(step.portraitPath);
                portraitImage.setDrawable(new TextureRegionDrawable(new TextureRegion(tex)));
                portraitImage.setVisible(true);
            }
        }

        private Texture loadTexture(String path) {
            Texture tex = new Texture(Gdx.files.internal(path));
            ownedTextures.add(tex);
            return tex;
        }

        public void finish() {
            if (!active) return;
            active = false;
            rootTable.remove();
            dispose();

            if (onComplete != null) {
                onComplete.run();
            }
        }

        public boolean isActive() {
            return active;
        }

        public void dispose() {
            for (Texture tex : ownedTextures) {
                if (tex != null) tex.dispose();
            }
            ownedTextures.clear();
        }
    }

    private final Stage uiStage;
    private final BitmapFont font;
    private final Array<DialogueStep> steps = new Array<>();
    private String initialBgPath;
    private boolean skippable = true;
    private Runnable onComplete;

    public CutsceneBuilder(Stage uiStage, BitmapFont font) {
        this.uiStage = uiStage;
        this.font = font;
    }

    public CutsceneBuilder setInitialBackground(String path) {
        this.initialBgPath = path;
        return this;
    }

    public CutsceneBuilder setSkippable(boolean skippable) {
        this.skippable = skippable;
        return this;
    }

    public CutsceneBuilder addStep(String text) {
        steps.add(new DialogueStep("", text, null, null));
        return this;
    }

    public CutsceneBuilder addStep(String speaker, String text) {
        steps.add(new DialogueStep(speaker, text, null, null));
        return this;
    }

    public CutsceneBuilder addStep(String speaker, String text, String bgPath, String portraitPath) {
        steps.add(new DialogueStep(speaker, text, bgPath, portraitPath));
        return this;
    }

    public CutsceneBuilder onComplete(Runnable onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    public DialogueCutscene build() {
        Array<Texture> ownedTextures = new Array<>();

        LabelStyle speakerStyle = new LabelStyle(font, Color.CYAN);
        LabelStyle textStyle = new LabelStyle(font, Color.WHITE);
        TextButtonStyle skipStyle = new TextButtonStyle();
        skipStyle.font = font;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0.08f, 0.85f);
        pixmap.fill();
        Texture boxTex = new Texture(pixmap);
        pixmap.dispose();
        ownedTextures.add(boxTex);

        Image bgImage = new Image();
        if (initialBgPath != null) {
            Texture bgTex = new Texture(Gdx.files.internal(initialBgPath));
            ownedTextures.add(bgTex);
            bgImage.setDrawable(new TextureRegionDrawable(new TextureRegion(bgTex)));
        }

        Image portraitImage = new Image();
        portraitImage.setVisible(false);

        Label speakerLabel = new Label("", speakerStyle);
        Label dialogueLabel = new Label("", textStyle);
        dialogueLabel.setWrap(true);
        dialogueLabel.setAlignment(Align.topLeft);

        // Layout: Stack background behind foreground UI table
        Table rootTable = new Table();
        rootTable.setFillParent(true);

        Table dialogueBox = new Table();
        dialogueBox.setBackground(new TextureRegionDrawable(new TextureRegion(boxTex)));
        dialogueBox.pad(30);
        dialogueBox.add(speakerLabel).left().row();
        dialogueBox.add(dialogueLabel).grow().padTop(15);

        TextButton skipButton = new TextButton("Skip >>", skipStyle);
        skipButton.setVisible(skippable);

        rootTable.add(skipButton).top().right().pad(20).row();
        rootTable.add(portraitImage).size(300, 300).bottom().left().padLeft(60).row();
        rootTable.add(dialogueBox).growX().height(280).pad(30).bottom();

        DialogueCutscene cutscene = new DialogueCutscene(
                uiStage, rootTable, bgImage, portraitImage,
                speakerLabel, dialogueLabel, steps, ownedTextures, onComplete
        );

        skipButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                cutscene.finish();
            }
        });

        rootTable.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT) {
                    cutscene.nextStep();
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
                    cutscene.nextStep();
                    return true;
                }
                return false;
            }
        });

        return cutscene;
    }
}