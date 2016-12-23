import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;
import javax.swing.JOptionPane;
@SuppressWarnings("serial")

public class MazeCanvas extends Canvas implements Runnable, KeyListener {

	private Thread runThread;
	
	private int boxWidth = 40, boxHeight = 40, gridWidth = 15, gridHeight = 11;
	
	public double seconds = 0;
	private int count = 0;
	
	private String stringHiscore;
	private double hiScore = -1.0;
	
	private Point person = new Point(20,20);
	private int personDirection = PersonDirection.noDirection;
	
	private Point obstacle1, obstacle2, obstacle3, obstacle4;
	private int obstacle1Direction, obstacle2Direction, obstacle3Direction, obstacle4Direction;
	private int[] directions = {ObstacleDirection.northEast, ObstacleDirection.northWest, ObstacleDirection.southEast, ObstacleDirection.southWest};
	private int randomNum;
	
	private Image menuImage = null;
	private boolean isInMenu = true, isAtEndGame = false;
	
	public void DrawMenu(Graphics g) {
		if (this.menuImage == null) {
			try {
				URL imagePath = MazeCanvas.class.getResource("Cover.jpg");
				this.menuImage = Toolkit.getDefaultToolkit().getImage(imagePath);
			} catch (Exception e) {
				System.out.println("File not found");
			}
		}	
		g.drawImage(menuImage, 0, 0, 620, 480, this);
	}
	
	//resetting place for person and obstacles
	public void Start() {
		person = new Point(7,5);
		obstacle1 = new Point(3,2);
		obstacle2 = new Point(12,3);
		obstacle3 = new Point(4,8);
		obstacle4 = new Point(7,7);
		
		obstacle1Direction = ObstacleDirection.startDirection;
		obstacle2Direction = ObstacleDirection.startDirection;
		obstacle3Direction = ObstacleDirection.startDirection;
		obstacle4Direction = ObstacleDirection.startDirection;
	}
	
	public void DrawEndGame(Graphics g) {
		BufferedImage endGameImage = new BufferedImage(this.getPreferredSize().width, this.getPreferredSize().height, BufferedImage.TYPE_INT_ARGB);
		Graphics endGameGraphics = endGameImage.getGraphics();
		endGameGraphics.setColor(Color.black);
		
		endGameGraphics.drawString("You Lost!", this.getPreferredSize().width / 2, this.getPreferredSize().height /2);
		endGameGraphics.drawString("Your score: " + seconds, this.getPreferredSize().width / 2, (this.getPreferredSize().height / 2) + 10);
		endGameGraphics.drawString("Press \"space\" to start a new game.", this.getPreferredSize().width / 2, (this.getPreferredSize().height / 2) + 40);
		g.drawImage(endGameImage, 0, 0, this);
	}
	
	public void paint(Graphics g) {
		if (runThread == null) {
			this.setPreferredSize(new Dimension(620, 490));
			this.addKeyListener(this);
			runThread = new Thread(this);
			runThread.start();
		}
		if (isInMenu == true) {
			DrawMenu(g);
		} else if (isAtEndGame) {
			DrawEndGame(g);
		} else {
			if (person.x == 20 && person.y == 20) {
				Start();
			}
			if (hiScore == -1.0) {
				hiScore = this.GetHighScore();
			}
			DrawPerson(g);
			DrawGrid(g);
			DrawObstacles(g);
			DrawScore(g);
		}
	}
	
	//defaut update method that contains double buffer
	public void update(Graphics g) {
		Graphics offscreenGraphics;
		BufferedImage offscreen = null;
		Dimension d = this.getSize();
		
		offscreen = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
		offscreenGraphics = offscreen.getGraphics();
		offscreenGraphics.setColor(this.getBackground());
		offscreenGraphics.fillRect(0, 0, d.width, d.height);
		offscreenGraphics.setColor(this.getForeground());
		paint(offscreenGraphics);
		
		//flip
		g.drawImage(offscreen, 0, 0, this);
	}
	
	//retrieving the high score
	public double GetHighScore() {
		Scanner diskScanner;
		String[] split;
		try {
			diskScanner = new Scanner(new File("C:\\Users\\Gabriel\\MyWorkspace\\AvoidTheBlocks\\HighScore.txt"));
			stringHiscore = diskScanner.nextLine();
			split = stringHiscore.split(": ");
			hiScore = Double.parseDouble(split[1]);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return hiScore;
	}
	
	//drawing game
	public void DrawScore(Graphics g) {
		g.drawString("Seconds: " + seconds, 0, boxHeight * gridHeight + 15);
		g.drawString("Highscore: " + stringHiscore, 0, boxHeight * gridHeight + 28);
	}
	
	public void DrawGrid(Graphics g) {
		//drawing the rectangle
		g.drawRect(0, 0, gridWidth*boxWidth, gridHeight*boxHeight);
			
		//drawing verticle lines
		for (int x = boxWidth; x < gridWidth*boxWidth; x+=boxWidth) {
			g.drawLine(x, 0, x, boxHeight*gridHeight);
		}
			
		//drawing horizontal lines
		for (int y = boxWidth; y < gridWidth*boxWidth; y+=boxWidth) {
			g.drawLine(0, y, gridWidth*boxWidth, y);
		}
	}	
		
	public void DrawPerson(Graphics g) {
		g.setColor(Color.blue);
		g.fillOval(person.x * boxWidth, person.y * boxHeight, boxWidth, boxHeight);
		g.setColor(Color.black);
	}
	
	public void DrawObstacles(Graphics g) {
		g.setColor(Color.red);
		g.fillRect(obstacle1.x * boxWidth, obstacle1.y * boxHeight, boxWidth, boxHeight);
		g.setColor(Color.black);
		g.fillRect(obstacle2.x * boxWidth, obstacle2.y * boxHeight, boxWidth, boxHeight);
		g.setColor(Color.gray);
		g.fillRect(obstacle3.x * boxWidth, obstacle3.y * boxHeight, boxWidth, boxHeight);
		g.setColor(Color.green);
		g.fillRect(obstacle4.x * boxWidth, obstacle4.y * boxHeight, boxWidth, boxHeight);
		g.setColor(Color.black);
	}
	
	//setting random direction for obstacles
	public void ReleaseObstacles() {
		Random random = new Random();
		randomNum = random.nextInt(4);
		obstacle1Direction = directions[randomNum];
		obstacle2Direction = directions[randomNum];
		obstacle3Direction = directions[randomNum];
		obstacle4Direction = directions[randomNum];
	}
		
	//stopping obstacles
	public void Stop() {
		person.move(20,20);
		obstacle1Direction = ObstacleDirection.endDirection;
		obstacle2Direction = ObstacleDirection.endDirection;
		obstacle3Direction = ObstacleDirection.endDirection;
		obstacle4Direction = ObstacleDirection.endDirection;
	}
	
	public void ObstacleOne() {
		//setting obstacle1 directions
		if (obstacle1Direction == ObstacleDirection.northWest) {
			obstacle1 = new Point(obstacle1.x - 1, obstacle1.y - 1);
		} else if (obstacle1Direction == ObstacleDirection.northEast){
			obstacle1 = new Point(obstacle1.x + 1, obstacle1.y - 1);
		} else if (obstacle1Direction == ObstacleDirection.southWest) {
			obstacle1 = new Point(obstacle1.x - 1, obstacle1.y + 1);
		} else if (obstacle1Direction == ObstacleDirection.southEast) {
			obstacle1 = new Point(obstacle1.x + 1, obstacle1.y + 1);
		}
			
		//bouncing obstacle1
		if (obstacle1.x == 0) {
			if (obstacle1Direction == ObstacleDirection.northWest) {
				obstacle1Direction = ObstacleDirection.northEast;
			} else if (obstacle1Direction == ObstacleDirection.southWest) {
				obstacle1Direction = ObstacleDirection.southEast;
			}
		} else if (obstacle1.x == gridWidth - 1) {
			if (obstacle1Direction ==  ObstacleDirection.northEast) {
				obstacle1Direction = ObstacleDirection.northWest;
			} else if (obstacle1Direction == ObstacleDirection.southEast) {
				obstacle1Direction = ObstacleDirection.southWest;
			}
		} else if (obstacle1.y == 0) {
			if (obstacle1Direction == ObstacleDirection.northEast) {
				obstacle1Direction = ObstacleDirection.southEast;
			} else if (obstacle1Direction == ObstacleDirection.northWest) {
				obstacle1Direction = ObstacleDirection.southWest;	
			}	
		} else if (obstacle1.y == gridHeight - 1) {
			if (obstacle1Direction == ObstacleDirection.southEast) {
				obstacle1Direction = ObstacleDirection.northEast;
			} else if (obstacle1Direction == ObstacleDirection.southWest) {
				obstacle1Direction = ObstacleDirection.northWest;
			}
		} else if (obstacle1.x == 1 && obstacle1.y == 1) {
			obstacle1Direction = ObstacleDirection.southEast;
		} else if (obstacle1.x == 1 && obstacle1.y == gridHeight - 2) {
			obstacle1Direction = ObstacleDirection.northEast;
		} else if (obstacle1.x == gridWidth - 2 && obstacle1.y == 1) {
			obstacle1Direction = ObstacleDirection.southWest;
		} else if (obstacle1.x == gridWidth - 2 && obstacle1.y == gridHeight - 2) {
			obstacle1Direction = ObstacleDirection.northWest;
		}	
	}
	
	public void ObstacleTwo() {
		//setting obstacle2 directions
		if (obstacle2Direction == ObstacleDirection.northWest) {
			obstacle2 = new Point(obstacle2.x - 1, obstacle2.y - 1);
		} else if (obstacle2Direction == ObstacleDirection.northEast){
			obstacle2 = new Point(obstacle2.x + 1, obstacle2.y - 1);
		} else if (obstacle2Direction == ObstacleDirection.southWest) {
			obstacle2 = new Point(obstacle2.x - 1, obstacle2.y + 1);
		} else if (obstacle2Direction == ObstacleDirection.southEast) {
			obstacle2 = new Point(obstacle2.x + 1, obstacle2.y + 1);
		}
			
		//bouncing obstacle2
		if (obstacle2.x == 0) {
			if (obstacle2Direction == ObstacleDirection.northWest) {
				obstacle2Direction = ObstacleDirection.northEast;
			} else if (obstacle2Direction == ObstacleDirection.southWest) {
				obstacle2Direction = ObstacleDirection.southEast;
			}
		} else if (obstacle2.x == gridWidth - 1) {
			if (obstacle2Direction ==  ObstacleDirection.northEast) {
				obstacle2Direction = ObstacleDirection.northWest;
			} else if (obstacle2Direction == ObstacleDirection.southEast) {
				obstacle2Direction = ObstacleDirection.southWest;
			}
		} else if (obstacle2.y == 0) {
			if (obstacle2Direction == ObstacleDirection.northEast) {
				obstacle2Direction = ObstacleDirection.southEast;
			} else if (obstacle2Direction == ObstacleDirection.northWest) {
				obstacle2Direction = ObstacleDirection.southWest;	
			}
		} else if (obstacle2.y == gridHeight - 1) {
			if (obstacle2Direction == ObstacleDirection.southEast) {
				obstacle2Direction = ObstacleDirection.northEast;
			} else if (obstacle2Direction == ObstacleDirection.southWest) {
				obstacle2Direction = ObstacleDirection.northWest;
			}
		} else if (obstacle2.x == 1 && obstacle2.y == 1) {
			obstacle2Direction = ObstacleDirection.southEast;
		} else if (obstacle2.x == 1 && obstacle2.y == gridHeight - 2) {
			obstacle2Direction = ObstacleDirection.northEast;
		} else if (obstacle2.x == gridWidth - 2 && obstacle2.y == 1) {
			obstacle2Direction = ObstacleDirection.southWest;
		} else if (obstacle2.x == gridWidth - 2 && obstacle2.y == gridHeight - 2) {
			obstacle2Direction = ObstacleDirection.northWest;
		}	
	}
	
	public void ObstacleThree() {
		//setting obstacle3 directions
		if (obstacle3Direction == ObstacleDirection.northWest) {
			obstacle3 = new Point(obstacle3.x - 1, obstacle3.y - 1);
		} else if (obstacle3Direction == ObstacleDirection.northEast){
			obstacle3 = new Point(obstacle3.x + 1, obstacle3.y - 1);
		} else if (obstacle3Direction == ObstacleDirection.southWest) {
			obstacle3 = new Point(obstacle3.x - 1, obstacle3.y + 1);
		} else if (obstacle3Direction == ObstacleDirection.southEast) {
			obstacle3 = new Point(obstacle3.x + 1, obstacle3.y + 1);
		}
			
		//bouncing obstacle3
		if (obstacle3.x == 0) {
			if (obstacle3Direction == ObstacleDirection.northWest) {
				obstacle3Direction = ObstacleDirection.northEast;
			} else if (obstacle3Direction == ObstacleDirection.southWest) {
				obstacle3Direction = ObstacleDirection.southEast;
			}
		} else if (obstacle3.x == gridWidth - 1) {
			if (obstacle3Direction ==  ObstacleDirection.northEast) {
				obstacle3Direction = ObstacleDirection.northWest;
			} else if (obstacle3Direction == ObstacleDirection.southEast) {
				obstacle3Direction = ObstacleDirection.southWest;
			}
		} else if (obstacle3.y == 0) {
			if (obstacle3Direction == ObstacleDirection.northEast) {
				obstacle3Direction = ObstacleDirection.southEast;
			} else if (obstacle3Direction == ObstacleDirection.northWest) {
				obstacle3Direction = ObstacleDirection.southWest;	
			}
		} else if (obstacle3.y == gridHeight - 1) {
			if (obstacle3Direction == ObstacleDirection.southEast) {
				obstacle3Direction = ObstacleDirection.northEast;
			} else if (obstacle3Direction == ObstacleDirection.southWest) {
				obstacle3Direction = ObstacleDirection.northWest;
			}
		} else if (obstacle3.x == 1 && obstacle3.y == 1) {
			obstacle3Direction = ObstacleDirection.southEast;
		} else if (obstacle3.x == 1 && obstacle3.y == gridHeight - 2) {
			obstacle3Direction = ObstacleDirection.northEast;
		} else if (obstacle3.x == gridWidth - 2 && obstacle3.y == 1) {
			obstacle3Direction = ObstacleDirection.southWest;
		} else if (obstacle3.x == gridWidth - 2 && obstacle3.y == gridHeight - 2) {
			obstacle3Direction = ObstacleDirection.northWest;
		}	
	}
	
	public void ObstacleFour() {
		//setting obstacle4 directions
		if (obstacle4Direction == ObstacleDirection.northWest) {
			obstacle4 = new Point(obstacle4.x - 1, obstacle4.y - 1);
		} else if (obstacle4Direction == ObstacleDirection.northEast){
			obstacle4 = new Point(obstacle4.x + 1, obstacle4.y - 1);
		} else if (obstacle4Direction == ObstacleDirection.southWest) {
			obstacle4 = new Point(obstacle4.x - 1, obstacle4.y + 1);
		} else if (obstacle4Direction == ObstacleDirection.southEast) {
			obstacle4 = new Point(obstacle4.x + 1, obstacle4.y + 1);
		}
			
		//bouncing obstacle4
		if (obstacle4.x == 0) {
			if (obstacle4Direction == ObstacleDirection.northWest) {
				obstacle4Direction = ObstacleDirection.northEast;
			} else if (obstacle4Direction == ObstacleDirection.southWest) {
				obstacle4Direction = ObstacleDirection.southEast;
			}
		} else if (obstacle4.x == gridWidth - 1) {
			if (obstacle4Direction ==  ObstacleDirection.northEast) {
				obstacle4Direction = ObstacleDirection.northWest;
			} else if (obstacle4Direction == ObstacleDirection.southEast) {
				obstacle4Direction = ObstacleDirection.southWest;
			}
		} else if (obstacle4.y == 0) {
			if (obstacle4Direction == ObstacleDirection.northEast) {
				obstacle4Direction = ObstacleDirection.southEast;
			} else if (obstacle4Direction == ObstacleDirection.northWest) {
				obstacle4Direction = ObstacleDirection.southWest;	
			}
		} else if (obstacle4.y == gridHeight - 1) {
			if (obstacle4Direction == ObstacleDirection.southEast) {
				obstacle4Direction = ObstacleDirection.northEast;
			} else if (obstacle4Direction == ObstacleDirection.southWest) {
				obstacle4Direction = ObstacleDirection.northWest;
			}
		} else if (obstacle4.x == 1 && obstacle4.y == 1) {
			obstacle4Direction = ObstacleDirection.southEast;
		} else if (obstacle4.x == 1 && obstacle4.y == gridHeight - 2) {
			obstacle4Direction = ObstacleDirection.northEast;
		} else if (obstacle4.x == gridWidth - 2 && obstacle4.y == 1) {
			obstacle4Direction = ObstacleDirection.southWest;
		} else if (obstacle4.x == gridWidth - 2 && obstacle4.y == gridHeight - 2) {
			obstacle4Direction = ObstacleDirection.northWest;
		}	
	}
	
	public void Move() {
		//setting the direction of person
		switch(personDirection) {
		case PersonDirection.north:
			person = new Point(person.x, person.y - 1);
			personDirection= PersonDirection.noDirection;
			break;
		case PersonDirection.south:
			person = new Point(person.x, person.y + 1);
			personDirection= PersonDirection.noDirection;
			break;
		case PersonDirection.east:
			person = new Point(person.x + 1, person.y);
			personDirection= PersonDirection.noDirection;
			break;
		case PersonDirection.west:
			person = new Point(person.x - 1, person.y);
			personDirection= PersonDirection.noDirection;
			break; 
		}
		
		//situating all the obstacles
		ObstacleOne();
		ObstacleTwo();
		ObstacleThree();
		ObstacleFour();
	}
	
	//counting the score
	public void Counting() {
		if (obstacle1Direction == ObstacleDirection.startDirection) {
			count = 0;
		} else if (obstacle1Direction != ObstacleDirection.endDirection) {
			count++;
		}
		seconds = (double) count / 10;
	}
	
	//ending on contact and out of bounds
	public void EndGame() {
		if (person.equals(obstacle1) || person.equals(obstacle2) || person.equals(obstacle3) || person.equals(obstacle4)) {
			Stop();
			WinorLose();
		} else if (person.x == 0 || person.x == gridWidth - 1 || person.y == 0 || person.y == gridHeight - 1) {
			Stop();
			WinorLose();
		}
	}
	
	//determining your fate
	public void WinorLose() {
		String[] splitArray;
		if (seconds > hiScore) {
			String name = JOptionPane.showInputDialog("NEW HIGHSCORE!! What is your name?");
			stringHiscore = name + ": " + seconds;
			splitArray = stringHiscore.split(": ");
			hiScore = Double.parseDouble(splitArray[1]);
		} else {
			isAtEndGame = true;
		}
		PrintStream diskWriter;
		try {
			diskWriter = new PrintStream(new File("C:\\Users\\Gabriel\\MyWorkspace\\AvoidTheBlocks\\HighScore.txt"));
			diskWriter.println(stringHiscore);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (true) {
			if (!isInMenu && !isAtEndGame) {
				Move();
				Counting();
			}
			EndGame();
			repaint();
			
			try {
				Thread.currentThread();
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {	
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		switch(arg0.getKeyCode()) {
		case KeyEvent.VK_UP:
			if (obstacle1Direction == ObstacleDirection.startDirection) {
				ReleaseObstacles();
			}
			personDirection= PersonDirection.north;
			break;
		case KeyEvent.VK_DOWN:
			if (obstacle1Direction == ObstacleDirection.startDirection) {
				ReleaseObstacles();
			}
			personDirection= PersonDirection.south;
			break;
		case KeyEvent.VK_RIGHT:
			if (obstacle1Direction == ObstacleDirection.startDirection) {
				ReleaseObstacles();
			}
			personDirection= PersonDirection.east;
			break;
		case KeyEvent.VK_LEFT:
			if (obstacle1Direction == ObstacleDirection.startDirection) {
				ReleaseObstacles();
			}
			personDirection= PersonDirection.west;

			break;
		case KeyEvent.VK_ENTER:
			if (isInMenu) {
				isInMenu = false;
				repaint();
			};
			break;
		case KeyEvent.VK_ESCAPE:
			isInMenu = true;
		case KeyEvent.VK_SPACE:
			if (isAtEndGame) {
				isAtEndGame = false;
				repaint();
				Start();
			}
			break;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}
}
