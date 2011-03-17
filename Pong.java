// imports
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;                 // Rectangle2D
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.media.opengl.awt.*;        // GLCanvas
import javax.media.opengl.fixedfunc.*;
import com.jogamp.opengl.util.*;        // Animator
import com.jogamp.opengl.util.awt.*;    // TextRenderer
import java.lang.Math;


public class Pong implements GLEventListener, KeyListener
{
	// constants
	private static final int SCREEN_WIDTH = 800;
	private static final int SCREEN_HEIGHT = 600;
	private static final int FPS = 120;              //frames per second
	private static final double PADDLE_SPEED = 400;   //default paddle speed
	private static final int MAX_SCORE = 10;         //game restarts when either player reaches this score
	private static final double READY_TIME = 1.0;
	private static final int BALL_RADIUS = 5;        //radius of the ball

	// static vars
	private static GLCanvas canvas;
	//private static Animator animator;
	private static FPSAnimator animator;
	private static GLU glu;
	private static Frame frame;
	private static double AI_UPDATE_TIME = 1.5;
	private static double BALL_SPEED = 300;    //default ball speed

	// instance vars
	private double gameTime;     // sec
	private double frameTime;    // sec
	private long prevTime;      // nano sec
	private double aiTime;
	private double readyTime;

	private int screenWidth;
	private int screenHeight;

	private Ball ball;
	private Paddle player;
	private Paddle computer;

	private TextRenderer textRenderer;
	private SoundPlayer sound = new SoundPlayer(); /// for sound effects
	private int playerScore = 0;
	private int computerScore = 0;

	public enum GameState { MENU, START, READY, GAME }
	private GameState gameState = GameState.MENU;

	public static void main(String[] args)
	{
		Pong pong = new Pong();
	}

	/**Start a new game of Pong.**/
	public Pong()
	{
		System.out.println("Starting Pong...");

		initPong();
		initJOGL();

		// reset timer
		prevTime = System.nanoTime();
		gameTime = frameTime = 0;
	}

	/**Initialize the ball and paddles.**/
	private void initPong()
	{
		// ball
		ball = new Ball();
		ball.setPosition(SCREEN_WIDTH/2.0, SCREEN_HEIGHT/2.0);
		ball.setRadius(BALL_RADIUS);
		ball.setColor(0.2f, 1, 0.2f);    //color green

		// paddle for player
		player = new Paddle();
		player.setPosition(10.0, SCREEN_HEIGHT/2.0);
		player.setWidth(10);
		player.setHeight(70);
		player.setColor(1, 0.2f, 0.2f);    //color red
		player.setSpeed(PADDLE_SPEED);
		player.setBounds(player.getHeight()/2, SCREEN_HEIGHT - player.getHeight()/2);

		// paddle for computer
		computer = new Paddle();
		computer.setPosition(SCREEN_WIDTH-10.0, SCREEN_HEIGHT/2.0);
		computer.setWidth(10);
		computer.setHeight(70);
		computer.setColor(0.2f, 0.2f, 1);    //color blue
		computer.setSpeed(PADDLE_SPEED);
		computer.setBounds(player.getHeight()/2, SCREEN_HEIGHT - computer.getHeight()/2);
	}

	/**Initialize JOGL.**/
	private void initJOGL()
	{
		// OpenGL capabilities
		GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
		caps.setDoubleBuffered(true);
		caps.setHardwareAccelerated(true);

		// create objects
		frame = new Frame("Pong");
		canvas = new GLCanvas(caps);
		//animator = new Animator(canvas);
		animator = new FPSAnimator(canvas, FPS);

		// config frame
		frame.add(canvas);
		frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		frame.setLocation(100, 100);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		frame.setVisible(true);

		//config canvas
		canvas.addGLEventListener(this);
		canvas.requestFocus();

		//create a text renderer
		textRenderer = new TextRenderer(new Font("Dialog", Font.BOLD, 60));

		//start animator
		animator.start();

		//debug
		System.out.println("Initialized JOGL.");
	}

	/**Terminate game.**/
	public static void exit()
	{
		animator.stop();
		frame.dispose();

		System.out.println("Pong is terminated.");
		System.exit(0);
	}

	/**Return the frame time in seconds.**/
	private double getFrameTime()
	{
		long currTime = System.nanoTime();
		double deltaTime = (currTime - prevTime) / 1000000000.0; //nanosec to sec
		prevTime = currTime;
		return deltaTime;
	}

	/**Set the state of the game.**/
	private void setGameState(GameState state)
	{
		gameState = state;
		if(state == GameState.START){
			playerScore = computerScore = 0;
			AI_UPDATE_TIME = 1.0;    //reset AI
			BALL_SPEED = 300;    //reset ball speed
			ball.setPosition(SCREEN_WIDTH/2, SCREEN_HEIGHT/2);
			ball.setVelocity(0, 0);
			setGameState(GameState.READY);
		}else if(state == GameState.READY){
			readyTime = 0;  // reset ready timer
		}else if(state == GameState.GAME){
			ball.fire(SCREEN_WIDTH/2, SCREEN_HEIGHT/2, BALL_SPEED);
		}
	}

	/**Update the frame.**/
	private void update()
	{
		if(gameState == GameState.MENU) return;
		else if(gameState == GameState.READY){
			readyTime += frameTime;
			if(readyTime > READY_TIME)
				setGameState(GameState.GAME);
			return;
		}

		//System.out.println(frameTime);
		ball.update(frameTime);        //updates the ball
		player.update(frameTime);      //updates the player paddle
		computer.update(frameTime);    //updates the computer paddle
		updateAI();                    //updates the AI

		int hit = hitTest();
		if(hit == 1) sound.play("blip01.wav");
		else if(hit == 2) sound.play("blip02.wav");
		else if(hit == 3){
			sound.play("blip03.wav");
			if(playerScore >= MAX_SCORE || computerScore >= MAX_SCORE){
				//game ends when either player reaches MAX_SCORE
				setGameState(GameState.MENU);
			}else{
				//shoot the next ball
				AI_UPDATE_TIME *= 0.5;     //increase refresh speed of AI
				BALL_SPEED += 20;			//increase speed of ball
				setGameState(GameState.READY);
			}
		}
	}

	/**Computer AI.**/
	private void updateAI()
	{
		aiTime += frameTime;
		if(aiTime < AI_UPDATE_TIME) return;
		if(ball.getVelocity().x < 0) return; // not incoming

		computer.setMovingDown(false);
		computer.setMovingUp(false);
		aiTime = 0;

		double ballY = ball.getPosition().y;
		double paddleY = computer.getPosition().y;
		double offset = computer.getHeight() / 2.0;

		if(ballY > (paddleY + offset))
			computer.setMovingUp(true);
		else if(ballY < (paddleY - offset))
			computer.setMovingDown(true);
	}

	/**Return 1 if ball hits wall, 2 if it hits paddle, 3 if it goes behind paddle.**/
	private int hitTest()
	{
		int hit = 0;
		Vector2 pos = ball.getPosition();
		Vector2 vel = ball.getVelocity();
		double rad = ball.getRadius();

		// testing with wall
		if(pos.y < 0) // hit bottom wall
		{
			ball.setPosition(pos.x, 0);
			ball.setVelocity(vel.x, -vel.y);
			hit = 1;
		}
		else if(pos.y > SCREEN_HEIGHT) // hit top wall
		{
			ball.setPosition(pos.x, SCREEN_HEIGHT);
			ball.setVelocity(vel.x, -vel.y);
			hit = 1;
		}

		// testing with paddles
		Vector2 left = player.getPosition();    // left paddle
		Vector2 right = computer.getPosition(); // right paddle
		double offset = player.getHeight() / 2.0;

		// test with left paddle
		if(pos.x + rad < 0)
		{
			computerScore++;
			hit = 3;
		}
		else if(pos.x < left.x)
		{
			if(pos.y > (left.y - offset) && pos.y < (left.y + offset))
			{
				ball.setPosition(left.x, pos.y);
				vel = english(vel);
				ball.setVelocity(-vel.x, vel.y);
				hit = 2;
			}
		}

		// continue hitTest() for right paddle 
		else if(pos.x - rad > SCREEN_WIDTH)
		{
			playerScore++;
			hit = 3;
		}
		else if(pos.x > right.x)
		{
			if(pos.y > (right.y-offset) && pos.y < (right.y+offset))
			{
				ball.setPosition(right.x, pos.y);
				vel = english(vel);
				ball.setVelocity(-vel.x, vel.y);
				hit = 2;
			}
		}

		return hit;
	}

	/**Draw the frame.**/
	private void drawFrame(GLAutoDrawable drawable)
	{
		final GL2 gl = drawable.getGL().getGL2();

		// clear screen
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glLoadIdentity();

		// draw scene
		drawBackground(gl);
		drawPaddles(gl, player);    //draw player paddle
		drawPaddles(gl, computer);  //draw computer paddle
		drawBall(gl);

		// draw text
		drawScores();
		drawMessage();

		// tell OpenGL ready to draw
		gl.glFlush();
	}

	/**Return vector that is angled depending on the directon of movement of the paddle.**/
	private Vector2 english(Vector2 vec)
	{
		Vector2 newVec = new Vector2(vec);
		double length = vec.getLength();

		if(vec.x < 0) {
			if(player.isMovingUp()) {
				newVec.normalize();
				if(vec.y > 0)      newVec.y *= 2.0;
				else if(vec.y < 0) newVec.y *= 0.5;
				newVec.scale(length);
			}
			else if(player.isMovingDown()) {
				newVec.normalize();
				if(vec.y > 0)      newVec.y *= 0.5;
				else if(vec.y < 0) newVec.y *= 2.0;
				newVec.scale(length);
			}
		}else { // for computer paddle side
			if(computer.isMovingUp()) {
				newVec.normalize();
				if(vec.y > 0)      newVec.y *= 2.0;
				else if(vec.y < 0) newVec.y *= 0.5;
				newVec.scale(length);
			}
			else if(computer.isMovingDown()) {
				newVec.normalize();
				if(vec.y > 0)      newVec.y *= 0.5;
				else if(vec.y < 0) newVec.y *= 2.0;
				newVec.scale(length);
			}
		}
		return newVec;
	}

	/**Draw the ball.**/
	private void drawBall(GL2 gl)
	{
		// get position and color of ball
		Vector2 center = ball.getPosition();
		double radius = ball.getRadius();
		Color color = ball.getColor();

		// draw 360 triangles
		gl.glColor3f(color.red, color.green, color.blue);
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex2d(center.x, center.y);
		for(int angle = 0; angle < 360; angle++){
			gl.glVertex2d(center.x + Math.sin(angle) * radius, center.y + Math.cos(angle) * radius);
		}
		gl.glEnd();
	}

	/**Draw the paddles.**/
	private void drawPaddles(GL2 gl, Paddle p)
	{
		// get position and color
		Vector2 center = p.getPosition();
		double offsetX = p.getWidth() / 2.0;
		double offsetY = p.getHeight() / 2.0;
		Color color = p.getColor();

		// draw player's paddle
		gl.glColor3f(color.red, color.green, color.blue);
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glVertex2d(center.x - offsetX, center.y - offsetY);
		gl.glVertex2d(center.x + offsetX, center.y - offsetY);
		gl.glVertex2d(center.x + offsetX, center.y + offsetY);
		gl.glVertex2d(center.x - offsetX, center.y - offsetY);
		gl.glVertex2d(center.x + offsetX, center.y + offsetY);
		gl.glVertex2d(center.x - offsetX, center.y + offsetY);
		gl.glEnd();
	}

	/**Draw the background.**/
	private void drawBackground(GL2 gl)
	{
		gl.glLineWidth(10);
		gl.glColor3f(1.0f, 1.0f, 1.0f);

		// bottom line
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(0, 0);
		gl.glVertex2f(SCREEN_WIDTH, 0);
		gl.glEnd();

		// top line
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2f(0, SCREEN_HEIGHT);
		gl.glVertex2f(SCREEN_WIDTH, SCREEN_HEIGHT);
		gl.glEnd();

		// center dotted line
		final int DOT_LENGTH = 10;
		gl.glLineWidth(2);
		gl.glBegin(GL.GL_LINES);
		for(int i = 0; i <= SCREEN_HEIGHT; i += DOT_LENGTH * 2)
		{
			gl.glVertex2f(SCREEN_WIDTH/2, i);
			gl.glVertex2f(SCREEN_WIDTH/2, i + DOT_LENGTH);
		}
		gl.glEnd();

		// reset line width to default 
		gl.glLineWidth(1);
	}

	/**Draw the scores.**/
	private void drawScores()
	{
		String score;

		textRenderer.beginRendering(SCREEN_WIDTH, SCREEN_HEIGHT);
		textRenderer.setColor(1, 1, 1, 0.5f);    //white with 50% transparency

		score = Integer.toString(playerScore);
		textRenderer.draw(score, 312, 525);

		score = Integer.toString(computerScore);
		textRenderer.draw(score, 466, 525);

		textRenderer.endRendering();
	}

	/**Draw a message.**/
	private void drawMessage()
	{
		if(gameState != GameState.MENU)	return;

		String message = "Press Space to start";

		// compute text bounds to draw text at the center of screen
		Rectangle2D rect = textRenderer.getBounds(message);

		textRenderer.beginRendering(SCREEN_WIDTH, SCREEN_HEIGHT);
		textRenderer.setColor(1, 0, 0, 1.0f);
		textRenderer.draw(message,
				(int)(SCREEN_WIDTH/2.0 - rect.getCenterX()),
				(int)(SCREEN_HEIGHT/2.0 - rect.getCenterY()));

		textRenderer.setColor(0, 0, 1, 1.0f);
		if(playerScore > computerScore)
			textRenderer.draw("You Won.", 250, 400);
		else if(playerScore < computerScore)
			textRenderer.draw("You Lose.", 250, 400);

		textRenderer.endRendering();
	}

	///////////////////////////////////////////////////////////////////////////
	// Methods for the implementation of GLEventListener
	///////////////////////////////////////////////////////////////////////////
	public void init(GLAutoDrawable drawable)
	{
		((Component)drawable).addKeyListener(this);

		// init OpenGL
		GL2 gl = drawable.getGL().getGL2();
		gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
		gl.glClearColor(0.1f, 0.1f, 0.1f, 0.0f); //change background color
		gl.glClearDepth(1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		GL2 gl = drawable.getGL().getGL2();

		// remember current window dimension
		screenWidth = w;
		screenHeight = h;

		gl.glViewport(0, 0, w, h);

		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(0, SCREEN_WIDTH, 0, SCREEN_HEIGHT, -1, 1);

		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public void display(GLAutoDrawable drawable)
	{
		// get frameTime
		frameTime = getFrameTime();
		gameTime += frameTime;

		// update scene before drawing
		update();

		// draw scene
		drawFrame(drawable);
	}

	public void dispose(GLAutoDrawable gLDrawable)
	{
	}

	///////////////////////////////////////////////////////////////////////////
	// Methods required for the implementation of KeyListener
	///////////////////////////////////////////////////////////////////////////
	public void keyPressed(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE:
				System.out.println("Escape key is pressed. Exiting Pong...");
				exit();
				break;
	
			case KeyEvent.VK_UP:
				player.setMovingUp(true);
				break;
	
			case KeyEvent.VK_DOWN:
				player.setMovingDown(true);
				break;
		}
	}

	public void keyReleased(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_SPACE:
				setGameState(GameState.START);
				//System.out.println("Space key is up.");
				break;
	
			case KeyEvent.VK_UP:
				player.setMovingUp(false);
				break;
	
			case KeyEvent.VK_DOWN:
				player.setMovingDown(false);
				break;
		}
	}

	public void keyTyped(KeyEvent e)
	{
	}
}