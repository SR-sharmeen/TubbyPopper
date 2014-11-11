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
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends  SimpleBaseGameActivity implements IOnMenuItemClickListener  {

	static final int CAMERA_WIDTH = 800; 
	static final int CAMERA_HEIGHT = 480; 
	private Camera mCamera; 
	private Scene mMainScene;
	private BitmapTextureAtlas mBlueTextureAtlas; 
	private BitmapTextureAtlas mBlackTextureAtlas; 
	private BitmapTextureAtlas mRedTextureAtlas; 
	private BitmapTextureAtlas mBackgroundTextureAtlas; 
	private BitmapTextureAtlas mThornsTextureAtlas; 
	private BitmapTextureAtlas mShowPlusTenTextureAtlas; 
	private BitmapTextureAtlas mPlayTextureAtlas; 
	private BitmapTextureAtlas mPauseTextureAtlas; 
	private BitmapTextureAtlas mExplosionTextureAtlas; 

	private TextureRegion BlueBall;
	private TextureRegion BlackBall;
	private TextureRegion RedBall;
	private TextureRegion BackGround;
	private TextureRegion Thorns;
	private TextureRegion PlusTen;
	private TextureRegion Play;
	private TextureRegion Pause;
	private TextureRegion Explode;
	private TimerHandler generateBallTimer;
	private TimerHandler levelUpTimer;
	private TimerHandler removePlusTenTimer;
	private TimerHandler endGameTimer;
	private TimerHandler timeCheckTimer;
	
	private MenuScene menuScene;
	double generationDelay=2;
	int blackCount=0;
	int redCount=0;
	int blueCount=0;
	int startX=2;
	int startY=2;
	int speed=60;
	int Score=0;
	int life=3;
	Sprite plus10=null;
	Text scoreTextDisplay;
	Text livesTextDisplay;
	Text elapsedTimeDisplay;
	String scoreText;
	Sound popSound ;
	Sound dyingBeep;
	Music bgMusic;
	Sound boom;
	int time=180;
	boolean firstHalf=true;
	boolean secondHalf=false;
	boolean isPaused =false;
	ArrayList<Sprite> theBalls;
    private Sprite thornsOnBackgroundSprite;
    private Sprite backgroundSprite;
	private Font mFont;

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY)
    {
	    switch(pMenuItem.getID())
	    {
	        case 0:
	        	if(isPaused ==false)
	        	{
                    pauseGame(pMenuScene);
                }
	        	else
	        	{
                    resumeGame(pMenuScene);
	        	}
	            return true;
	    
	        default:
	            return false;
	    }
	}

    private void resumeGame(MenuScene pMenuScene) {
        isPaused =false;
        toggleMusic(1);
        playPauseButtonSet(pMenuScene,Pause);
        ignoreSceneUpdates(false);
    }

    private void pauseGame(MenuScene pMenuScene) {
        isPaused =true;
        toggleMusic(0);
        playPauseButtonSet(pMenuScene, Play);
        ignoreSceneUpdates(true);
    }

    private void toggleMusic(int state) {
        if(bgMusic!=null)
        {
            switch (state){
                case 1:
                    bgMusic.resume();
                    break;
                case 2:
                    bgMusic.pause();
                    break;
            }

        }
    }

    private void ignoreSceneUpdates(boolean isIgnored) {
        mMainScene.setIgnoreUpdate(isIgnored);
    }

    private void playPauseButtonSet(MenuScene pMenuScene, TextureRegion textureRegion) {
        IMenuItem pMenuItem;
        pMenuScene.reset();
        pMenuScene.clearMenuItems();
        pMenuItem= new ScaleMenuItemDecorator(new SpriteMenuItem(0, textureRegion, getVertexBufferObjectManager()), 2, 1);
        pMenuScene.addMenuItem(pMenuItem);
        pMenuScene.buildAnimations();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onPause() {
	    toggleMusic(0);
		super.onPause();
	};
	@Override
	public void onPauseGame() {
		toggleMusic(0);
		super.onPauseGame();
	};
	@Override
	protected synchronized void onResume() {
        toggleMusic(1);
        super.onResume();
	};
	@Override
	public
	synchronized void onResumeGame() {
        toggleMusic(1);
        super.onResumeGame();
	};
	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT); 
		EngineOptions gameEngineOptions=new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
			new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), 					
			this.mCamera);
		gameEngineOptions.getAudioOptions().setNeedsSound(true);
		gameEngineOptions.getAudioOptions().getMusicOptions().setNeedsMusic(true);
		return gameEngineOptions;

	}

	@Override
	protected void onCreateResources() {
        Log.e("Creating:", "Resources");

        Log.e("Creating:", "Sounds");
        loadGameSounds();
        loadGameMusic();
        Log.e("Creating", "Texture Regions");
        mBlackTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mBlueTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mRedTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mBackgroundTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 4096, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mThornsTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mShowPlusTenTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mPlayTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mPauseTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mExplosionTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

        BlackBall= BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (this.mBlackTextureAtlas, this, "bomb.png", startX, startY);
        BlueBall= BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (this.mBlueTextureAtlas, this, "mmango.png", startX, startY);
        RedBall= BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (this.mRedTextureAtlas, this, "apple.png", startX, startY);
        BackGround=BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (this.mBackgroundTextureAtlas, this, "sky.png", startX, startY);
        Thorns=BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (this.mThornsTextureAtlas, this, "thorns.png", startX, startY);
        PlusTen=BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (this.mShowPlusTenTextureAtlas, this, "plus10.png", startX, startY);
        Play=BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (this.mPlayTextureAtlas, this, "play.png", startX, startY);
        Pause=BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (this.mPauseTextureAtlas, this, "pause.png", startX, startY);
        Explode=BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (this.mExplosionTextureAtlas, this, "explosion.png", startX, startY);

        this.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);

        this.mFont.load();

        mBlueTextureAtlas.load();
        mBlackTextureAtlas.load();
        mRedTextureAtlas.load();
        mBackgroundTextureAtlas.load();
        mThornsTextureAtlas.load();
        mShowPlusTenTextureAtlas.load();
        mPlayTextureAtlas.load();
        mPauseTextureAtlas.load();
        mExplosionTextureAtlas.load();
		
	}

    private void loadGameSounds() {
        try {
             popSound= SoundFactory.createSoundFromAsset(getSoundManager(), this, "pop.mp3");
            } catch (IOException e) {
            e.printStackTrace();
            }
        try {
             dyingBeep= SoundFactory.createSoundFromAsset(getSoundManager(), this, "dyingbeep.mp3");
            } catch (IOException e) {
            e.printStackTrace();
            }
        try {
             boom= SoundFactory.createSoundFromAsset(getSoundManager(), this, "boom.mp3");
            } catch (IOException e) {
            e.printStackTrace();
            }
    }

    private void createTextureRegions() {
        mBlackTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mBlueTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mRedTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mBackgroundTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 4096, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mThornsTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mShowPlusTenTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mPlayTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mPauseTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        mExplosionTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

        BlackBall= BitmapTextureAtlasTextureRegionFactory.createFromAsset
                (this.mBlackTextureAtlas, this, "bomb.png", startX, startY);
        BlueBall= BitmapTextureAtlasTextureRegionFactory.createFromAsset
                 (this.mBlueTextureAtlas, this, "mmango.png", startX, startY);
        RedBall= BitmapTextureAtlasTextureRegionFactory.createFromAsset
                 (this.mRedTextureAtlas, this, "apple.png", startX, startY);
        BackGround=BitmapTextureAtlasTextureRegionFactory.createFromAsset
                 (this.mBackgroundTextureAtlas, this, "sky.png", startX, startY);
        Thorns=BitmapTextureAtlasTextureRegionFactory.createFromAsset
                 (this.mThornsTextureAtlas, this, "thornsOnBackgroundSprite.png", startX, startY);
        PlusTen=BitmapTextureAtlasTextureRegionFactory.createFromAsset
                 (this.mShowPlusTenTextureAtlas, this, "plus10.png", startX, startY);
        Play=BitmapTextureAtlasTextureRegionFactory.createFromAsset
                 (this.mPlayTextureAtlas, this, "play.png", startX, startY);
        Pause=BitmapTextureAtlasTextureRegionFactory.createFromAsset
                 (this.mPauseTextureAtlas, this, "pause.png", startX, startY);
        Explode=BitmapTextureAtlasTextureRegionFactory.createFromAsset
                 (this.mExplosionTextureAtlas, this, "explosion.png", startX, startY);

        this.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);

        this.mFont.load();

        mBlueTextureAtlas.load();
        mBlackTextureAtlas.load();
        mRedTextureAtlas.load();
        mBackgroundTextureAtlas.load();
        mThornsTextureAtlas.load();
        mShowPlusTenTextureAtlas.load();
        mPlayTextureAtlas.load();
        mPauseTextureAtlas.load();
        mExplosionTextureAtlas.load();
    }

    private void loadGameMusic() {
        try {
            bgMusic= MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "music.mp3");
        } catch (IOException e) {
            Log.e("loadGameMusic:", "Could not load game music");
        }
        bgMusic.setLooping(true);
        bgMusic.setVolume(0.1f);
    }

    private MenuScene createMenu()
	{
	    menuScene = new MenuScene(mCamera);
	    menuScene.setX(-375);
	    menuScene.setY(-120);
        addPauseOptionToMenu();
	    menuScene.setBackgroundEnabled(false);
	    menuScene.setOnMenuItemClickListener(this);
	    return menuScene;
	}

    private void addPauseOptionToMenu() {
        final IMenuItem pauseMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(0, Pause, getVertexBufferObjectManager()), 2, 1);
        menuScene.addMenuItem(pauseMenuItem);
        menuScene.buildAnimations();
    }

    @Override
	protected Scene onCreateScene() {
        Sprite bg= new Sprite(0,0, BackGround, getVertexBufferObjectManager());

        final Sprite thorns= new Sprite(0,CAMERA_HEIGHT-118, Thorns, getVertexBufferObjectManager());

        bg.setCullingEnabled(true);
        theBalls=new ArrayList<Sprite>();
        this.mMainScene=new Scene();
        //	this.mMainScene.setBackground(new Background(255, 255, 255));
        this.mMainScene.setBackground(new SpriteBackground(bg));
        this.mMainScene.attachChild(thorns);

        //bgMusic.setVolume(5);
        bgMusic.play();
        String s="this is where the score will be";
        scoreTextDisplay = new Text(0, 0, this.mFont, s, new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
        livesTextDisplay = new Text(0, 30, this.mFont, s, new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
        elapsedTimeDisplay = new Text(CAMERA_WIDTH-150, 0, this.mFont, s, new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
        scoreTextDisplay.setColor(Color.RED);
        scoreTextDisplay.setZIndex(1000);
        livesTextDisplay.setZIndex(1000);
        this.mMainScene.attachChild(scoreTextDisplay);
        this.mMainScene.attachChild(livesTextDisplay);
        this.mMainScene.attachChild(elapsedTimeDisplay);
        livesTextDisplay.setText("Lives:"+life);
        scoreText="Score:";
        scoreTextDisplay.setText(scoreText+Score);
        IUpdateHandler detect = new IUpdateHandler()
        {
            @Override
            public void onUpdate(float pSecondsElapsed) {

                //	elapsedTimeDisplay.setText("Time:"+Float.toString(pSecondsElapsed));
                for(int i=0; i<theBalls.size();i++)
                {
                    if(thorns.collidesWith(theBalls.get(i)) )
                    {


                        if(theBalls.get(i).getTag()!=0)
                        {
                            dyingBeep.play();

                            life--;
                            livesTextDisplay.setText("Lives:"+life);
                            if(life==0)
                            {
                                endTheGame();
                            }


                            Log.e("Update", "A good ball died");
                        }
                        mMainScene.detachChild(theBalls.get(i));
                        theBalls.remove(i);
                    }
                }

            }

            @Override
            public void reset() {
                // TODO Auto-generated method stub

            }
        };
        this.mMainScene.registerUpdateHandler(detect);
        endGameTimer = new TimerHandler((float) 180, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {

                endTheGame();
            }
        }){
            @Override
            public void onUpdate(float pSecondsElapsed) {
                if(!isPaused)
                {
                    super.onUpdate(pSecondsElapsed);
                }
            }

        }

        ;

        generateBallTimer = new TimerHandler((float) generationDelay, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {

                generateBalls();
            }



        }


        )
        {
            @Override
            public void onUpdate(float pSecondsElapsed) {
                if(!isPaused)
                {
                    super.onUpdate(pSecondsElapsed);
                }
            }

        }

        ;
        timeCheckTimer = new TimerHandler((float) 1, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                time--;
                if(time==0)
                {
                    endTheGame();
                }
                elapsedTimeDisplay.setText("Time:"+Integer.toString(time));
            }
        }){
            @Override
            public void onUpdate(float pSecondsElapsed) {
                if(!isPaused)
                {
                    super.onUpdate(pSecondsElapsed);
                }
            }

        }

        ;
        removePlusTenTimer = new TimerHandler((float) 0.5, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {

                mMainScene.detachChild(plus10);
                mMainScene.unregisterUpdateHandler(pTimerHandler);
            }
        }){
            @Override
            public void onUpdate(float pSecondsElapsed) {
                if(!isPaused)
                {
                    super.onUpdate(pSecondsElapsed);
                }
            }

        }

        ;
        levelUpTimer=new TimerHandler(30, true, new ITimerCallback()
        {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {

                Log.e("Level Increased","Increasing Level");
                IncreaseSpeed();
                UpdateFrequency();
            }

        }){
            @Override
            public void onUpdate(float pSecondsElapsed) {
                if(!isPaused)
                {
                    super.onUpdate(pSecondsElapsed);
                }
            }

        }

        ;
        menuScene=createMenu();
        mMainScene.setChildScene(menuScene);
        getEngine().registerUpdateHandler(generateBallTimer);
        getEngine().registerUpdateHandler(levelUpTimer);
        getEngine().registerUpdateHandler(endGameTimer);
        getEngine().registerUpdateHandler(timeCheckTimer);
        return this.mMainScene;

		
	}

    private void addMenuToScene() {
        menuScene=createMenu();
        mMainScene.setChildScene(menuScene);
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
        initBallGenerator();
        initElapsedTimeTimer();
        initRemovePlus10NotificationTimer();
        initLevelUpTimer();
    }

    private void initLevelUpTimer() {
        levelUpTimer=new TimerHandler(30, true, new ITimerCallback()
		{
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {

				Log.e("Level Increased", "Increasing Level");
				IncreaseSpeed();
				UpdateFrequency();
			}

		}){
			@Override
			public void onUpdate(float pSecondsElapsed) {
				if(!isPaused)
				{
					super.onUpdate(pSecondsElapsed);
				}
			}

		}

		;
    }

    private void initRemovePlus10NotificationTimer() {
        removePlusTenTimer = new TimerHandler((float) 0.5, true, new ITimerCallback() {
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {

				mMainScene.detachChild(plus10);
				mMainScene.unregisterUpdateHandler(pTimerHandler);
			}
			}){
			@Override
			public void onUpdate(float pSecondsElapsed) {
				if(!isPaused)
				{
					super.onUpdate(pSecondsElapsed);
				}
			}

		}

		;
    }

    private void initElapsedTimeTimer() {
        timeCheckTimer = new TimerHandler((float) 1, true, new ITimerCallback() {
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
					time--;
					if(time==0)
					{
						endTheGame();
					}
				elapsedTimeDisplay.setText("Time:"+Integer.toString(time));
			}
			}){
			@Override
			public void onUpdate(float pSecondsElapsed) {
				if(!isPaused)
				{
					super.onUpdate(pSecondsElapsed);
				}
			}

		}

		;
    }

    private void initBallGenerator() {
        generateBallTimer = new TimerHandler((float) generationDelay, true, new ITimerCallback() {
				@Override
				public void onTimePassed(TimerHandler pTimerHandler) {

					generateBalls();
				}



				}


				)
			{
			@Override
			public void onUpdate(float pSecondsElapsed) {
				if(!isPaused)
				{
					super.onUpdate(pSecondsElapsed);
				}
			}

		}

		;
    }

    private void initEndGameTimer() {
        endGameTimer = new TimerHandler((float) 180, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {

                endTheGame();
            }
            }){
            @Override
            public void onUpdate(float pSecondsElapsed) {
                if(!isPaused)
                {
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
        theBalls=new ArrayList<Sprite>();
        backgroundSprite= new Sprite(0,0, BackGround, getVertexBufferObjectManager());
        thornsOnBackgroundSprite = new Sprite(0,CAMERA_HEIGHT-118, Thorns, getVertexBufferObjectManager());
    }

    private void loadGameInfoText() {
        String s="this is where the score will be";
        scoreTextDisplay = new Text(0, 0, this.mFont, s, new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
        livesTextDisplay = new Text(0, 30, this.mFont, s, new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
        elapsedTimeDisplay = new Text(CAMERA_WIDTH-150, 0, this.mFont, s, new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
        scoreTextDisplay.setColor(Color.RED);
        scoreTextDisplay.setZIndex(1000);
        livesTextDisplay.setZIndex(1000);
        this.mMainScene.attachChild(scoreTextDisplay);
        this.mMainScene.attachChild(livesTextDisplay);
        this.mMainScene.attachChild(elapsedTimeDisplay);
        livesTextDisplay.setText("Lives:"+life);
        scoreText="Score:";
        scoreTextDisplay.setText(scoreText+Score);
    }

    private void setBackground(Sprite bg, Sprite thorns) {
        bg.setCullingEnabled(true);
        this.mMainScene.setBackground(new SpriteBackground(bg));
        this.mMainScene.attachChild(thorns);
    }

    private void endTheGame()
	{
	
		Intent i=new Intent(this, GameOver.class);
		i.putExtra("score", Integer.toString(Score));
		i.putExtra("life", Integer.toString(life));
		finish();
		startActivity(i);
	}
	
	private void goodBallTouched(Sprite ball)
	{
		Score+=10;
		this.mMainScene.detachChild(ball);
	}
	private void IncreaseSpeed()
	{
		if(speed!=0)
			{speed-=10;}
	}
	private void UpdateFrequency()
	{
		if(firstHalf==true)
		{
			firstHalf=false;
			secondHalf=true;
			redCount=blueCount=blackCount=0;
		}
		else if(secondHalf=true)
		{
			secondHalf=false;
			redCount=blueCount=blackCount=0;
		}
	}
	private void generateBalls()
	{
		Random chooseBall=new Random();
		Random chooseLocation=new Random();
		Random indexChanger=new Random();
		int ballChoice=chooseBall.nextInt(3);
		if(firstHalf)
		{
			if(ballChoice==0 && blackCount>=(redCount+blueCount/2))
			{
				ballChoice=chooseBall.nextInt(2)+1;
			}
		}
		if(secondHalf)
		{
			if(ballChoice==0 && blackCount>=redCount+blueCount)
			{
				ballChoice=chooseBall.nextInt(2)+1;
			}
		}
		if(!firstHalf && !secondHalf)
		{
			if(ballChoice!=0 && blackCount<=2*(redCount+blueCount))
			{
				ballChoice=0;
			}
		}
		 int posX=startX;
		 int posY=startY;
		 int screenChunk=CAMERA_WIDTH/4;
			int salt=indexChanger.nextInt((screenChunk-20)-5)+5;
		 	switch(chooseLocation.nextInt(4))
		 	{
		 		case 0:
		 			posX=posX+salt;
		 			break;
		 		case 1:
		 			posX=posX+screenChunk+salt;
		 			break;
		 		case 2:
		 			posX=posX+screenChunk*2+salt;
		 			break;
		 		case 3:
		 			posX=posX+screenChunk*3+salt;
		 			break;
		 	}
		 	int index=0;
			switch(ballChoice)
			{
				case 0:
					
					Sprite s=new Sprite(posX,posY, BlackBall, getVertexBufferObjectManager()){
					
				      @Override
					      public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY)
				      		{
				    	  		if(!isPaused)
				    	  	{
				    	  			Sprite ex= new Sprite(this.getX(),this.getY(), Explode, getVertexBufferObjectManager());
				    	  			this.detachSelf();
				    	  			mMainScene.attachChild(ex);
				    	  		boom.play();
				    	  		life=0;
					         	endTheGame();
					         	theBalls.remove(this);
				    	  	}
				    	  		return true;
				    	  		
				      		}
					   };

					theBalls.add(s);
					registrations(s,ballChoice, s.getX(), s.getY());
					blackCount++;
					break;
				case 1:
					Sprite s2= new Sprite(posX,posY, RedBall, getVertexBufferObjectManager()){
						
				
			
						
				      @Override
					      public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY)
				      		{
				    	  final Sprite touched=this;
				    	  if(pSceneTouchEvent.isActionDown() && !isPaused)
				    	  {
				    	  		if(this.detachSelf() || mMainScene.detachChild(this))
				    	  		{
				    	  		Log.e("Child", "Detatched");
			    	  			Score=Score+10;
			    	  			String me="";
			    	  			me=Integer.toString(Score);
			    	  			scoreTextDisplay.setText(scoreText+me);
			    	  			plus10= new Sprite(this.getX(),this.getY(),PlusTen, getVertexBufferObjectManager());
			    	  			mMainScene.attachChild(plus10);
			    	  			mMainScene.registerUpdateHandler(removePlusTenTimer);
			    	  			popSound.play();
				    	  		theBalls.remove(this);
				    	  		}
				    	  		mMainScene.detachChild(this);
				    	  		//this.dispose();
				    	  		//mMainScene.unregisterTouchArea(ball);
				         		Log.e("Score Updated on Blue Ball:", Integer.toString(Score));
				      		
				    	  }
				    	  		return true;
				      		}
					   };
					   theBalls.add(s2);
						registrations(s2,ballChoice, s2.getX(), s2.getY());
					redCount++;
					break;
				case 2:
					Sprite s3=new Sprite(posX,posY, BlueBall, getVertexBufferObjectManager()){
					
				      @Override
					      public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY)
				      		{
				    	  		final Sprite touched=this;
				    	  		
				    	  	if(pSceneTouchEvent.isActionDown() && !isPaused)
				    	  	{
				    	  	if(this.detachSelf() || mMainScene.detachChild(this))
				    	  	{
				    	  		Log.e("Child", "Detatched");
			    	  			Score=Score+10;
			    	  			String me="";
			    	  			me=Integer.toString(Score);
			    	  			scoreTextDisplay.setText(scoreText+me);
			    	  			 plus10= new Sprite(this.getX(),this.getY(),PlusTen, getVertexBufferObjectManager());
			    	  			mMainScene.attachChild(plus10);
			    	  			mMainScene.registerUpdateHandler(removePlusTenTimer);
			    	  			theBalls.remove(this);
			    	  			popSound.play();
				    	  	}
				    	  		
				    	  		}
				    	  		mMainScene.detachChild(this);
				    	  		//this.dispose();
				    	  		//mMainScene.unregisterTouchArea(ball);
				         		Log.e("Score Updated on Blue Ball:", Integer.toString(Score));
				      		
				    	  	
				    	  		return true;
				      		}
					   };
					   theBalls.add(s3);
						registrations(s3,ballChoice, s3.getX(), s3.getY());
					blueCount++;
					break;
			}
			
			
	
	}
	private void registrations (Sprite s, int ballChoice, float f, float g)
	{
		s.setTag(ballChoice);
		//theBalls.add(ball);
		  this.mMainScene.attachChild(s);
		  this.mMainScene.registerTouchArea(s);
		  this.mMainScene.setTouchAreaBindingOnActionDownEnabled(true);
		  
		MoveModifier drop = new MoveModifier(speed, f, f,
				g,CAMERA_HEIGHT){
					@Override
					protected void onModifierFinished(IEntity pItem) 
					{
						
						super.onModifierFinished(pItem);
					}
					};
					s.registerEntityModifier(drop);
	
	}
    IUpdateHandler collisionDetecter = new IUpdateHandler()
    {
        @Override
        public void onUpdate(float pSecondsElapsed) {

            //	elapsedTimeDisplay.setText("Time:"+Float.toString(pSecondsElapsed));
            for(int i=0; i<theBalls.size();i++)
            {
                if(thornsOnBackgroundSprite.collidesWith(theBalls.get(i)) )
                {


                    if(theBalls.get(i).getTag()!=0)
                    {
                        dyingBeep.play();

                        life--;
                        livesTextDisplay.setText("Lives:"+life);
                        if(life==0)
                        {
                            endTheGame();
                        }


                        Log.e("Update", "A good ball died");
                    }
                    mMainScene.detachChild(theBalls.get(i));
                    theBalls.remove(i);
                }
            }

        }

        @Override
        public void reset() {
            // TODO Auto-generated method stub

        }
    };
	

}