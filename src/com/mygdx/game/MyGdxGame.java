package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;

/**
 * Startklasse: Legt wichtige Groessen fest und laedt Musik, Sounds und Einstellungen 8globale Ressourcen)<br>
 * Setzt ersten Screen
 * @author Fabian, Tim
 */
public class MyGdxGame extends Game {
	
	/**
	 * Standard-Fensterbreite
	 */
	public static final int V_WIDTH = 1280;
	/**
	 * Standard-Fensterhoehe
	 */
	public static final int V_HEIGHT = 720;
	/**
	 * Pixel per Meter Verhaeltnis fuer Box2D
	 */
	public static final float PPM = 150;
	
	//Musik
	Music menuTheme;
	Music levelThemeW1;
	Music levelThemeW2;
	Music levelThemeW3;
	Music levelThemeW4;
	Music bossTheme1;
	Music bossTheme2;
	Music bossTheme3;
	Music bossTheme4;
	
	//Sounds
	Sound shoot;
	Sound heroShot;
	Sound laserShot;
	Sound turretShot;
	Sound enemySoldierShot;
	Sound bossShot;
	Sound powerUp;
	Sound checkpoint;
	Sound clickButton;
	Sound bossTremble;
	Sound bossExplode;
	Sound spawnAlien;
	Sound despawnAlien;
	Sound spawnAdds;
	Sound homing;
	Sound hurt;
	Sound death;
	Sound levelUp;
	Sound gameOver;
	Sound intro;
	
	private float sfxVolume;
	private String username;
	
	
	@Override
	public void create () {
		
		
		//Musik laden und Lautstaerke aus gespeicherten Einstellungen laden
		
		//Musik fuer Hauptmenue
		menuTheme = Gdx.audio.newMusic(Gdx.files.internal("music/gbbs8bit.mp3"));
		menuTheme.setVolume(XMLInteraction.loadMusicVolume());
		menuTheme.setLooping(true);
		
		//Musik fuer Base-Welt
		levelThemeW1 = Gdx.audio.newMusic(Gdx.files.internal("music/boom8bit.mp3"));
		levelThemeW1.setVolume(XMLInteraction.loadMusicVolume());
		levelThemeW1.setLooping(true);
		
		//Musik fuer Antarctica-Welt
		levelThemeW2 = Gdx.audio.newMusic(Gdx.files.internal("music/snowblind8bit.mp3"));
		levelThemeW2.setVolume(XMLInteraction.loadMusicVolume());
		levelThemeW2.setLooping(true);
		
		//Musik fuer Desert-Welt
		levelThemeW3 = Gdx.audio.newMusic(Gdx.files.internal("music/mrJack8bit.mp3"));
		levelThemeW3.setVolume(XMLInteraction.loadMusicVolume());
		levelThemeW3.setLooping(true);
		
		//Musik fuer Sci-Fi-Welt
		levelThemeW4 = Gdx.audio.newMusic(Gdx.files.internal("music/forest8bit.mp3"));
		levelThemeW4.setVolume(XMLInteraction.loadMusicVolume());
		levelThemeW4.setLooping(true);
		
		//Musik fuer Mech-Boss (Base-Welt)
		bossTheme1 = Gdx.audio.newMusic(Gdx.files.internal("music/bounce8bit.mp3"));
		bossTheme1.setVolume(XMLInteraction.loadMusicVolume());
		bossTheme1.setLooping(true);	
		
		//Musik fuer Boss (Antarctica-Welt)
		bossTheme2 = Gdx.audio.newMusic(Gdx.files.internal("music/byob8bit.mp3"));
		bossTheme2.setVolume(XMLInteraction.loadMusicVolume());
		bossTheme2.setLooping(true);	
		
		//Musik fuer Boss (Desert-Welt)
		bossTheme3 = Gdx.audio.newMusic(Gdx.files.internal("music/prisonsong8bit.mp3"));
		bossTheme3.setVolume(XMLInteraction.loadMusicVolume());
		bossTheme3.setLooping(true);	
		
		//Musik fuer Alien-Boss (SciFi-Welt)
		bossTheme4 = Gdx.audio.newMusic(Gdx.files.internal("music/fckthesystem8bit.mp3"));
		bossTheme4.setVolume(XMLInteraction.loadMusicVolume());
		bossTheme4.setLooping(true);	
		
		//Sounds laden
		
		//Quelle: http://soundbible.com/1087-Laser.html
		shoot = Gdx.audio.newSound(Gdx.files.internal("sounds/shot.wav"));
		heroShot = Gdx.audio.newSound(Gdx.files.internal("sounds/heroShot.wav"));
		laserShot = Gdx.audio.newSound(Gdx.files.internal("sounds/laserShot.wav"));
		turretShot = Gdx.audio.newSound(Gdx.files.internal("sounds/turretShot.wav"));
		enemySoldierShot = Gdx.audio.newSound(Gdx.files.internal("sounds/enemySoldierShot.wav"));
		bossShot = Gdx.audio.newSound(Gdx.files.internal("sounds/bossShot.wav"));
		powerUp = Gdx.audio.newSound(Gdx.files.internal("sounds/powerup.wav"));
		checkpoint = Gdx.audio.newSound(Gdx.files.internal("sounds/checkpoint.wav"));
		clickButton = Gdx.audio.newSound(Gdx.files.internal("sounds/clickButton.wav"));
		bossTremble = Gdx.audio.newSound(Gdx.files.internal("sounds/bossTremble.wav"));
		bossExplode = Gdx.audio.newSound(Gdx.files.internal("sounds/bossExplode.wav"));
		spawnAlien = Gdx.audio.newSound(Gdx.files.internal("sounds/spawnAlien.wav"));
		despawnAlien = Gdx.audio.newSound(Gdx.files.internal("sounds/despawnAlien.wav"));
		spawnAdds = Gdx.audio.newSound(Gdx.files.internal("sounds/spawnAdds.wav"));
		//Quelle: http://www.soundjay.com/beep-sounds-3.html
		homing = Gdx.audio.newSound(Gdx.files.internal("sounds/homing.wav"));
		hurt = Gdx.audio.newSound(Gdx.files.internal("sounds/hurt.wav"));
		death = Gdx.audio.newSound(Gdx.files.internal("sounds/death.wav"));
		levelUp = Gdx.audio.newSound(Gdx.files.internal("sounds/levelup.wav"));
		gameOver = Gdx.audio.newSound(Gdx.files.internal("sounds/gameover.wav"));
		intro = Gdx.audio.newSound(Gdx.files.internal("sounds/intro.wav"));
		
		//Lautstaerke fuer Sounds aus gespeicherten Einstellungen laden
		sfxVolume = XMLInteraction.loadSFXVolume();

		//Cursor wird zum Crosshair, Auswahl aus gespeicherten Einstellungen laden
		if(XMLInteraction.loadCrosshair().equals(0f)){
			//Quelle: https://giganticubegames.wordpress.com/2012/11/25/day-one-hundred-and-ninety-nine-0-08-change-weapon-rocket-part-1/
			Pixmap pm = new Pixmap(Gdx.files.internal("sprites/crosshair_black.png"));
			Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, pm.getWidth()/2, pm.getHeight()/2));
			pm.dispose();
		} else if (XMLInteraction.loadCrosshair().equals(1f)){
			//Quelle: http://addcomponent.com/lesson-2-create-first-person-weapon/
			Pixmap pm = new Pixmap(Gdx.files.internal("sprites/crosshair_red.png"));
			Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, pm.getWidth()/2, pm.getHeight()/2));
			pm.dispose();
		} else if (XMLInteraction.loadCrosshair().equals(2f)){
			//Quelle: http://www.clipartbest.com/crosshair-png
			Pixmap pm = new Pixmap(Gdx.files.internal("sprites/crosshair_round.png"));
			Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, pm.getWidth()/2, pm.getHeight()/2));
			pm.dispose();
		}
		
		//Username aus gespeicherten Einstellungen laden
		username = XMLInteraction.loadName();
		
		//Ersten Screen setzen -> Intro
		setScreen(new IntroScreen(this));
	}
	
	public void setMusicVolume(int vol){
		if(vol > -1 && vol < 11){
			menuTheme.setVolume((float) (vol * vol * 0.01));
			levelThemeW1.setVolume((float) (vol * vol * 0.01));
			levelThemeW2.setVolume((float) (vol * vol * 0.01));
			levelThemeW3.setVolume((float) (vol * vol * 0.01));
			levelThemeW4.setVolume((float) (vol * vol * 0.01));
			bossTheme1.setVolume((float) (vol * vol * 0.01));
			bossTheme2.setVolume((float) (vol * vol * 0.01));
			bossTheme3.setVolume((float) (vol * vol * 0.01));
			bossTheme4.setVolume((float) (vol * vol * 0.01));
			XMLInteraction.saveSettings("MusicVolume", Float.toString((float) (vol * vol * 0.01)));
		}
	}
	
	public int getMusicVolume(){
		return (int) (0.5 + Math.sqrt(menuTheme.getVolume() * 100));
	}
	
	public int getSFXVolume(){
		return (int) (0.5 + Math.sqrt(sfxVolume * 100));
	}
	
	public void setSFXVolume(int vol){
		if(vol > -1 && vol < 11){
			sfxVolume = (float) (vol * vol * 0.01);
			XMLInteraction.saveSettings("SFXVolume", Float.toString(sfxVolume));
		}
	}
	
	
	//Methoden zum Abspielen von Musik
		
	public void playMenuTheme(){
		menuTheme.play();
		
		levelThemeW1.pause();
		levelThemeW2.pause();
		levelThemeW3.pause();
		levelThemeW4.pause();
		
		bossTheme1.stop();
		bossTheme2.stop();
		bossTheme3.stop();
		bossTheme4.stop();
	}
	
	public void playLevelThemeW1(){
		levelThemeW1.play();
		
		menuTheme.stop();
		levelThemeW2.pause();
		levelThemeW3.pause();
		levelThemeW4.pause();
		
		bossTheme1.stop();
		bossTheme2.stop();
		bossTheme3.stop();
		bossTheme4.stop();
	}
	
	public void playLevelThemeW2(){
		levelThemeW2.play();
		
		menuTheme.stop();
		levelThemeW1.pause();
		levelThemeW3.pause();
		levelThemeW4.pause();
		
		bossTheme1.stop();
		bossTheme2.stop();
		bossTheme3.stop();
		bossTheme4.stop();
	}
	
	public void playLevelThemeW3(){
		levelThemeW3.play();
		
		menuTheme.stop();
		levelThemeW1.pause();
		levelThemeW2.pause();
		levelThemeW4.pause();
		
		bossTheme1.stop();
		bossTheme2.stop();
		bossTheme3.stop();
		bossTheme4.stop();
	}
	
	public void playLevelThemeW4(){
		levelThemeW4.play();
		
		menuTheme.stop();
		levelThemeW1.pause();
		levelThemeW2.pause();
		levelThemeW3.pause();
		
		bossTheme1.stop();
		bossTheme2.stop();
		bossTheme3.stop();
		bossTheme4.stop();
	}
	
	
	public void playBossTheme1(){
		bossTheme1.play();
		
		menuTheme.stop();
		levelThemeW1.pause();
		levelThemeW2.pause();
		levelThemeW3.pause();
		levelThemeW4.pause();
		
		bossTheme2.stop();
		bossTheme3.stop();
		bossTheme4.stop();
	}
	
	public void playBossTheme2(){
		bossTheme2.play();
		
		menuTheme.stop();
		levelThemeW1.pause();
		levelThemeW2.pause();
		levelThemeW3.pause();
		levelThemeW4.pause();
		
		bossTheme1.stop();
		bossTheme3.stop();
		bossTheme4.stop();
	}
	
	public void playBossTheme3(){
		bossTheme3.play();
		
		menuTheme.stop();
		levelThemeW1.pause();
		levelThemeW2.pause();
		levelThemeW3.pause();
		levelThemeW4.pause();
		
		bossTheme1.stop();
		bossTheme2.stop();
		bossTheme4.stop();
	}
	
	public void playBossTheme4(){
		bossTheme4.play();
		
		menuTheme.stop();
		levelThemeW1.pause();
		levelThemeW2.pause();
		levelThemeW3.pause();
		levelThemeW4.pause();
		
		bossTheme1.stop();
		bossTheme2.stop();
		bossTheme3.stop();
	}
	
	
	//Methoden zum Abspielen von Sounds
	
	public void playShoot(){
		shoot.play(sfxVolume);
	}
	
	public void playHeroShot(){
		heroShot.play(sfxVolume);
	}
	
	public void playLaserShot(){
		laserShot.play(sfxVolume);
	}
	
	public void playTurretShot(){
		turretShot.play(sfxVolume);
	}
	
	public void playEnemySoldierShot(){
		enemySoldierShot.play(sfxVolume);
	}
	
	public void playBossShot(){
		bossShot.play(sfxVolume);
	}
	
	public void playPowerUp(){
		powerUp.play(sfxVolume);
	}
	
	public void playCheckpoint(){
		checkpoint.play(sfxVolume);
	}
	
	public void playButtonClicked(){
		clickButton.play(sfxVolume);
	}
	
	public void playBossTremble(){
		bossTremble.play(sfxVolume);
	}
	
	public void playBossExplode(){
		bossExplode.play(sfxVolume);
	}
	
	public void playSpawnAlien(){
		spawnAlien.play(sfxVolume);
	}
	
	public void playDespawnAlien(){
		despawnAlien.play(sfxVolume);
	}
	
	public void playSpawnAdds(){
		spawnAdds.play(sfxVolume);
	}
	
	public void playHoming(){
		homing.play(sfxVolume);
	}
	
	public void playHurt(){
		hurt.play(sfxVolume);
	}
	
	public void playDeath(){
		death.play(sfxVolume);
	}
	
	public void playLevelUp(){
		levelUp.play(sfxVolume);
	}
	
	public void playGameOver(){
		gameOver.play(sfxVolume);
	}
	
	public void playIntro(){
		intro.play(sfxVolume);
	}
	
	
	public String getUsername(){
		return username;
	}
	
	public void setUsername(String name){
		if(name.isEmpty()){
			this.username = "Default";
		} else {
			this.username = name;
		}
		XMLInteraction.saveSettings("Username", this.username);
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		menuTheme.dispose();
		levelThemeW1.dispose();
		levelThemeW2.dispose();
		levelThemeW3.dispose();
		levelThemeW4.dispose();
		bossTheme1.dispose();
		bossTheme2.dispose();
		bossTheme3.dispose();
		bossTheme4.dispose();
		
		shoot.dispose();
		heroShot.dispose();
		laserShot.dispose();
		turretShot.dispose();
		enemySoldierShot.dispose();
		bossShot.dispose();
		powerUp.dispose();
		checkpoint.dispose();
		clickButton.dispose();
		bossTremble.dispose();
		bossExplode.dispose();
		spawnAlien.dispose();
		despawnAlien.dispose();
		spawnAdds.dispose();
		homing.dispose();
		hurt.dispose();
		death.dispose();
		levelUp.dispose();
		gameOver.dispose();
		intro.dispose();
	}
	
}
