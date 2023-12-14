package main;

import java.awt.Graphics;
import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import gamestates.Gamestate;
import gamestates.Menu;
import gamestates.Playing;

public class Game implements Runnable {

	private GamePanel gamePanel;
	private Thread gameThread;
	private final int FPS_SET = 120;
	private final int UPS_SET = 200;

	private Playing playing;
	private Menu menu;
	
	public final static int TILES_DEFAULT_SIZE = 32;
	public final static float SCALE = 5.0f;
	public final static float MENU_SCALE = 2f;
	public final static int TILES_IN_WIDTH = 12;
	public final static int TILES_IN_HEIGHT = 7;
	public final static int TILES_SIZE = (int) (TILES_DEFAULT_SIZE * SCALE);
	public final static int GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
	public final static int GAME_HEIGHT = TILES_SIZE * TILES_IN_HEIGHT;
	
	private Clip menuMusicClip;
	private Clip inGameMusicClip;
	
	public Game() {
		initClasses();
		
		gamePanel = new GamePanel(this);
		new GameWindow(gamePanel);
		gamePanel.requestFocus();

		playMusic("audio/menu.wav");
		playInGameMusic("audio/ingame.wav");

		startGameLoop();
	}

	private void initClasses() {
		menu = new Menu(this);
		playing = new Playing(this);
	}

	private void startGameLoop() {
		gameThread = new Thread(this);
		gameThread.start();
	}

	public void update() {
		switch (Gamestate.state) {
		case MENU:
			menu.update();
			startMenuMusic();
			stopInGameMusic();
			break;
		case PLAYING:
			playing.update();
			stopMenuMusic();
			startInGameMusic();
			break;
		case OPTIONS:
		case QUIT:
		default:
			System.exit(0);
			break;
		}
	}

	public void render(Graphics g) {
		switch (Gamestate.state) {
		case MENU:
			menu.draw(g);
			break;
		case PLAYING:
			playing.draw(g);
			break;
		default:
			break;
		}
	}

	@Override
	public void run() {

		double timePerFrame = 1000000000.0 / FPS_SET;
		double timePerUpdate = 1000000000.0 / UPS_SET;

		long previousTime = System.nanoTime();

		int frames = 0;
		int updates = 0;
		long lastCheck = System.currentTimeMillis();

		double deltaU = 0;
		double deltaF = 0;

		while (true) {
			long currentTime = System.nanoTime();

			deltaU += (currentTime - previousTime) / timePerUpdate;
			deltaF += (currentTime - previousTime) / timePerFrame;
			previousTime = currentTime;

			if (deltaU >= 1) {
				update();
				updates++;
				deltaU--;
			}

			if (deltaF >= 1) {
				gamePanel.repaint();
				frames++;
				deltaF--;
			}

			if (System.currentTimeMillis() - lastCheck >= 1000) {
				lastCheck = System.currentTimeMillis();
				System.out.println("FPS: " + frames + " | UPS: " + updates);
				frames = 0;
				updates = 0;
			}

		}

	}

	public void windowFocusLost() {
		if (Gamestate.state == Gamestate.PLAYING)
			playing.getPlayer().resetDirBooleans();
	}

	public Menu getMenu() {
		return menu;
	}

	public Playing getPlaying() {
		return playing;
	}
	
	private void playMusic(String filepath) {
	    try {
	        File musicFile = new File(System.getProperty("user.dir") + File.separator + filepath);
	        if (musicFile.exists()) {
	            AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
	            menuMusicClip = AudioSystem.getClip();
	            menuMusicClip.open(audioInput);
	            menuMusicClip.start();
	            menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
	        } else {
	            System.out.println("Can't find file!");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void stopMenuMusic() {
	    if (menuMusicClip != null && menuMusicClip.isRunning()) {
	        menuMusicClip.stop();
	    }
	}
	
	private void startMenuMusic() {
	    if (menuMusicClip != null && !menuMusicClip.isRunning()) {
	        menuMusicClip.start();
	        menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
	    }
	}

	private void playInGameMusic(String filepath) {
	    try {
	        File musicFile = new File(System.getProperty("user.dir") + File.separator + filepath);
	        if (musicFile.exists()) {
	            AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
	            inGameMusicClip = AudioSystem.getClip();
	            inGameMusicClip.open(audioInput);
	            inGameMusicClip.start();
	            inGameMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
	        } else {
	            System.out.println("Can't find file!");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void startInGameMusic() {
		if (inGameMusicClip != null && !inGameMusicClip.isRunning()) {
			inGameMusicClip.start();
			inGameMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
	    }
	}
	
	private void stopInGameMusic() {
	    if (inGameMusicClip != null && inGameMusicClip.isRunning()) {
	    	inGameMusicClip.stop();
	    }
	}
}
