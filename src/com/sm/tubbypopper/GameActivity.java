package com.sm.tubbypopper;

import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Menu;
import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends SimpleBaseGameActivity implements IOnMenuItemClickListener {

    static final int CAMERA_WIDTH = 800;
    static final int CAMERA_HEIGHT = 480;
    public static final int BOMB = 0;
    public static final int APPLE = 1;
    public static final int MANGO = 2;
    private  VertexBufferObjectManager vertexBufferObjectManager;
    private Camera mCamera;
    private Scene mMainScene;
    private BitmapTextureAtlas mangoTextureAtlas;
    private BitmapTextureAtlas bombTextureAtlas;
    private BitmapTextureAtlas appleTextureAtlas;
    private BitmapTextureAtlas skyTextureAtlas;
    private BitmapTextureAtlas thornsTextureAtlas;
    private BitmapTextureAtlas plusTenTextureAtlas;
    private BitmapTextureAtlas playTextureAtlas;
    private BitmapTextureAtlas pauseTextureAtlas;
    private BitmapTextureAtlas explosionTextureAtlas;

    private TextureRegion mangoTextureRegion;
    private TextureRegion bombTextureRegion;
    private TextureRegion appleTextureRegion;
    private TextureRegion skyTextureRegion;
    private TextureRegion thornsTextureRegion;
    private TextureRegion plusTenTextureRegion;
    private TextureRegion playTextureRegion;
    private TextureRegion pauseTextureRegion;
    private TextureRegion explosionTextureRegion;
    private TimerHandler generateBallTimer;
    private TimerHandler levelUpTimer;
    private TimerHandler removePlusTenTimer;
    private TimerHandler endGameTimer;
    private TimerHandler timeCheckTimer;

    private MenuScene menuScene;
    double generationDelay = 2;
    int bombCount = 0;
    int appleCount = 0;
    int mangoCount = 0;
    int startX = 2;
    int startY = 2;
    int speed = 40;
    int Score = 0;
    int life = 3;
    Sprite plus10 = null;
    Text scoreTextDisplay;
    Text livesTextDisplay;
    Text elapsedTimeDisplay;
    String scoreText;
    Sound popSound;
    Sound dyingBeep;
    Music bgMusic;
    Sound boom;
    int time = 180;
    boolean isPaused = false;
    ArrayList<Sprite> thePoppables;
    private Sprite thornsOnBackgroundSprite;
    private Sprite backgroundSprite;
    private Font font;


    @Override
    protected synchronized void onResume() {
        toggleMusic(1);
        super.onResume();
    }
    @Override
    public synchronized void onResumeGame() {
        toggleMusic(1);
        super.onResumeGame();
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        EngineOptions gameEngineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
                new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
                this.mCamera);
        gameEngineOptions.getAudioOptions().setNeedsSound(true);
        gameEngineOptions.getAudioOptions().getMusicOptions().setNeedsMusic(true);
        return gameEngineOptions;

    }

    @Override
    protected void onCreateResources() {
        Log.e("Creating:", "Resources");
        vertexBufferObjectManager=getVertexBufferObjectManager();
        loadGameSounds();
        loadGameMusic();
        createTextureRegions();
        loadTextureRegions();

    }

    @Override
    protected Scene onCreateScene() {
        initSceneSprites();
        mMainScene = new Scene();
        initScene();
        initTimers();
        addMenuToScene();
        registerTimers(getEngine());
        return mMainScene;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    private MenuScene createMenu() {
        menuScene = new MenuScene(mCamera);
        menuScene.setX(-375);
        menuScene.setY(-120);
        addPauseOptionToMenu();
        menuScene.setBackgroundEnabled(false);
        menuScene.setOnMenuItemClickListener(this);
        return menuScene;
    }

    private void addPauseOptionToMenu() {
        final IMenuItem pauseMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(0, pauseTextureRegion, vertexBufferObjectManager), 2, 1);
        menuScene.addMenuItem(pauseMenuItem);
        menuScene.buildAnimations();
    }
    private void addMenuToScene() {
        menuScene = createMenu();
        mMainScene.setChildScene(menuScene);
    }

    @Override
    public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) {
        switch (pMenuItem.getID()) {
            case 0:
                if (isPaused == false) {
                    pauseGame(pMenuScene);
                } else {
                    resumeGame(pMenuScene);
                }
                return true;

            default:
                return false;
        }
    }


    private void resumeGame(MenuScene pMenuScene) {
        isPaused = false;
        toggleMusic(1);
        playPauseButtonSet(pMenuScene, pauseTextureRegion);
        ignoreSceneUpdates(false);
    }

    @Override
    protected void onPause() {
        toggleMusic(0);
        super.onPause();
    }

    @Override
    public void onPauseGame() {
        toggleMusic(0);
        super.onPauseGame();
    }
    private void pauseGame(MenuScene pMenuScene) {
        isPaused = true;
        toggleMusic(0);
        playPauseButtonSet(pMenuScene, playTextureRegion);
        ignoreSceneUpdates(true);
    }



    private void ignoreSceneUpdates(boolean isIgnored) {
        mMainScene.setIgnoreUpdate(isIgnored);
    }

    private void playPauseButtonSet(MenuScene pMenuScene, TextureRegion textureRegion) {
        IMenuItem pMenuItem;
        pMenuScene.reset();
        pMenuScene.clearMenuItems();
        pMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(0, textureRegion, vertexBufferObjectManager), 2, 1);
        pMenuScene.addMenuItem(pMenuItem);
        pMenuScene.buildAnimations();
    }
    private void toggleMusic(int state) {
        if (bgMusic != null) {
            switch (state) {
                case 1:
                    bgMusic.resume();
                    break;
                case 2:
                    bgMusic.pause();
                    break;
            }
        }
    }

    private void loadGameSounds() {
        try {
            popSound = SoundFactory.createSoundFromAsset(getSoundManager(), this, "pop.mp3");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dyingBeep = SoundFactory.createSoundFromAsset(getSoundManager(), this, "dyingbeep.mp3");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            boom = SoundFactory.createSoundFromAsset(getSoundManager(), this, "boom.mp3");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadGameMusic() {
        try {
            bgMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "music.mp3");
        } catch (IOException e) {
            Log.e("loadGameMusic:", "Could not load game music");
        }
        bgMusic.setLooping(true);
        bgMusic.setVolume(0.1f);
    }

    private void createTextureRegions() {
        createTextureAtlas();

        bombTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (bombTextureAtlas, this, "bomb.png", startX, startY);
        mangoTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (mangoTextureAtlas, this, "mmango.png", startX, startY);
        appleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (appleTextureAtlas, this, "apple.png", startX, startY);
        skyTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (skyTextureAtlas, this, "sky.png", startX, startY);
        thornsTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (thornsTextureAtlas, this, "thorns.png", startX, startY);
        plusTenTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (plusTenTextureAtlas, this, "plus10.png", startX, startY);
        playTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (playTextureAtlas, this, "play.png", startX, startY);
        pauseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (pauseTextureAtlas, this, "pause.png", startX, startY);
        explosionTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (explosionTextureAtlas, this, "explosion.png", startX, startY);


    }

    private void createTextureAtlas() {
        TextureManager textureManager = this.getTextureManager();
        bombTextureAtlas = new BitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mangoTextureAtlas = new BitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        appleTextureAtlas = new BitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        skyTextureAtlas = new BitmapTextureAtlas(textureManager, 4096, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        thornsTextureAtlas = new BitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        plusTenTextureAtlas = new BitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        playTextureAtlas = new BitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        pauseTextureAtlas = new BitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        explosionTextureAtlas = new BitmapTextureAtlas(textureManager, 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        font = FontFactory.create(this.getFontManager(), textureManager, 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);

    }

    private void loadTextureRegions() {
        this.font.load();

        mangoTextureAtlas.load();
        bombTextureAtlas.load();
        appleTextureAtlas.load();
        skyTextureAtlas.load();
        thornsTextureAtlas.load();
        plusTenTextureAtlas.load();
        playTextureAtlas.load();
        pauseTextureAtlas.load();
        explosionTextureAtlas.load();
    }

    private void registerTimers(Engine engine) {
        engine.registerUpdateHandler(generateBallTimer);
        engine.registerUpdateHandler(levelUpTimer);
        engine.registerUpdateHandler(endGameTimer);
        engine.registerUpdateHandler(timeCheckTimer);
        mMainScene.registerUpdateHandler(collisionDetecter);
    }

    private void initTimers() {
        initEndGameTimer();
        initPoppableGenerator();
        initElapsedTimeTimer();
        initRemovePlus10NotificationTimer();
        initLevelUpTimer();
    }

    private void initLevelUpTimer() {
        levelUpTimer = new TimerHandler(30, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {

                Log.e("Level Increased", "Increasing Level");
                increaseSpeed();
            }

        }) {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                if (!isPaused) {
                    super.onUpdate(pSecondsElapsed);
                }
            }

        };
    }

    private void initRemovePlus10NotificationTimer() {
        removePlusTenTimer = new TimerHandler((float) 0.5, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {

                mMainScene.detachChild(plus10);
                mMainScene.unregisterUpdateHandler(pTimerHandler);
            }
        }) {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                if (!isPaused) {
                    super.onUpdate(pSecondsElapsed);
                }
            }

        };
    }

    private void initElapsedTimeTimer() {
        timeCheckTimer = new TimerHandler((float) 1, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                time--;
                if (time == 0) {
                    endTheGame();
                }
                elapsedTimeDisplay.setText("Time:" + Integer.toString(time));
            }
        }) {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                if (!isPaused) {
                    super.onUpdate(pSecondsElapsed);
                }
            }

        };
    }

    private void initPoppableGenerator() {
        generateBallTimer = new TimerHandler((float) generationDelay, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {

                generatePoppables();
            }


        }
        ) {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                if (!isPaused) {
                    super.onUpdate(pSecondsElapsed);
                }
            }

        };
    }

    private void initEndGameTimer() {
        endGameTimer = new TimerHandler((float) 180, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {

                endTheGame();
            }
        }) {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                if (!isPaused) {
                    super.onUpdate(pSecondsElapsed);
                }
            }

        }

        ;
    }

    private void initScene() {
        setBackground(backgroundSprite, thornsOnBackgroundSprite);
        toggleMusic(1);
        loadGameInfoText();
    }

    private void initSceneSprites() {
        thePoppables = new ArrayList<Sprite>();
        backgroundSprite = new Sprite(0, 0, skyTextureRegion, vertexBufferObjectManager);
        thornsOnBackgroundSprite = new Sprite(0, CAMERA_HEIGHT - 118, thornsTextureRegion, vertexBufferObjectManager);
    }

    private void loadGameInfoText() {
        String s = "this is where the score will be";
        loadScoreText(s);
        loadLivesText(s);
        elapsedTimeDisplay = new Text(CAMERA_WIDTH - 150, 0, this.font, s, new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);
        attachTextToScene();

    }

    private void attachTextToScene() {
        this.mMainScene.attachChild(scoreTextDisplay);
        this.mMainScene.attachChild(livesTextDisplay);
        this.mMainScene.attachChild(elapsedTimeDisplay);
    }

    private void loadLivesText(String s) {
        livesTextDisplay = new Text(0, 30, this.font, s, new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);
        livesTextDisplay.setText("Lives:" + life);
        livesTextDisplay.setZIndex(1000);
    }

    private void loadScoreText(String s) {
        scoreTextDisplay = new Text(0, 0, this.font, s, new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);
        scoreTextDisplay.setColor(Color.RED);
        scoreTextDisplay.setZIndex(1000);
        scoreText = "Score:";
        scoreTextDisplay.setText(scoreText + Score);
    }

    private void setBackground(Sprite bg, Sprite thorns) {
        bg.setCullingEnabled(true);
        this.mMainScene.setBackground(new SpriteBackground(bg));
        this.mMainScene.attachChild(thorns);
    }

    private void endTheGame() {

        Intent i = new Intent(this, GameOver.class);
        i.putExtra("score", Integer.toString(Score));
        i.putExtra("life", Integer.toString(life));
        finish();
        startActivity(i);
    }

    private void increaseSpeed() {
        if (speed != 0) {
            speed -= 10;
        }
    }

    private void generatePoppables() {
        Sprite sprite=null;
        Random chooseBall = new Random();
        Random chooseLocation = new Random();
        Random indexChanger = new Random();
        int poppableChoice = chooseBall.nextInt(3);
        int posX = startX;
        int posY = startY;
        int screenChunk = CAMERA_WIDTH / 4;
        int salt = indexChanger.nextInt((screenChunk - 20) - 5) + 5;
        switch (chooseLocation.nextInt(4)) {
            case 0:
                posX = posX + salt;
                break;
            case 1:
                posX = posX + screenChunk + salt;
                break;
            case 2:
                posX = posX + screenChunk * 2 + salt;
                break;
            case 3:
                posX = posX + screenChunk * 3 + salt;
                break;
        }

        switch (poppableChoice) {
            case BOMB:

                sprite = new Sprite(posX, posY, bombTextureRegion, vertexBufferObjectManager) {

                    @Override
                    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                        if (!isPaused) {
                            Sprite ex = new Sprite(this.getX(), this.getY(), explosionTextureRegion, getVertexBufferObjectManager());
                            this.detachSelf();
                            mMainScene.attachChild(ex);
                            boom.play();
                            life = 0;
                            endTheGame();
                            thePoppables.remove(this);
                        }
                        return true;

                    }
                };
                bombCount++;
                break;
            case APPLE:
               sprite= initFruitSprite(posX, posY, appleTextureRegion);
                appleCount++;
                break;
            case MANGO:
               sprite= initFruitSprite(posX,posY,mangoTextureRegion);
                mangoCount++;
                break;
        }
        thePoppables.add(sprite);
        registerSpriteAndAddMoveModifier(sprite, poppableChoice, sprite.getX(), sprite.getY());
      }

    private Sprite initFruitSprite(final int posX, final int posY, final TextureRegion appleTextureRegion1) {
        return new Sprite(posX, posY, appleTextureRegion1, vertexBufferObjectManager) {


            @Override
            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                Sprite sprite = this;
                if (pSceneTouchEvent.isActionDown() && !isPaused) {
                    if (sprite.detachSelf() || mMainScene.detachChild(sprite)) {
                        Log.e("Child", "Detatched");
                        removeFruitAndUpdateScore(sprite);
                    }
                    mMainScene.detachChild(sprite);
                }
                return true;
            }
        };


    }

    private void registerSpriteAndAddMoveModifier(Sprite sprite, int ballChoice, final float startX, final float startY) {
        sprite.setTag(ballChoice);
        this.mMainScene.attachChild(sprite);
        this.mMainScene.registerTouchArea(sprite);
        this.mMainScene.setTouchAreaBindingOnActionDownEnabled(true);

        MoveModifier drop = getMoveModifier(startX, startY);
        sprite.registerEntityModifier(drop);
    }

    private MoveModifier getMoveModifier(final float startX, final float startY) {
        return new MoveModifier(speed, startX, startX,
                startY, CAMERA_HEIGHT) {
            @Override
            protected void onModifierFinished(IEntity pItem) {

                super.onModifierFinished(pItem);
            }
        };
    }

    IUpdateHandler collisionDetecter = new IUpdateHandler() {
        @Override
        public void onUpdate(float pSecondsElapsed) {

            for (int i = 0; i < thePoppables.size(); i++) {
                if (thornsOnBackgroundSprite.collidesWith(thePoppables.get(i))) {


                    if (thePoppables.get(i).getTag() != BOMB) {
                        dyingBeep.play();

                        life--;
                        livesTextDisplay.setText("Lives:" + life);
                        if (life == 0) {
                            endTheGame();
                        }


                        Log.e("Update", "A good ball died");
                    }
                    mMainScene.detachChild(thePoppables.get(i));
                    thePoppables.remove(i);
                }
            }
        }

        @Override
        public void reset() {
        }
    };

    private void removeFruitAndUpdateScore(Sprite sprite) {
        Score = Score + 10;
        String me = "";
        me = Integer.toString(Score);
        scoreTextDisplay.setText(scoreText + me);
        plus10 = new Sprite(sprite.getX(), sprite.getY(), plusTenTextureRegion, vertexBufferObjectManager);
        mMainScene.attachChild(plus10);
        mMainScene.registerUpdateHandler(removePlusTenTimer);
        popSound.play();
        thePoppables.remove(sprite);
    }
}
