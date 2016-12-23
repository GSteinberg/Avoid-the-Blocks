import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Graphics;
@SuppressWarnings("serial")

public class ATBApplet extends Applet {

	private MazeCanvas m;
	
	public void init() {
		m = new MazeCanvas();
		m.setPreferredSize(new Dimension(620, 480));
		m.setVisible(true);
		m.setFocusable(true);
		this.add(m);
		this.setVisible(true);
		this.setSize(new Dimension(620, 480));
	}
	
	public void paint(Graphics g) {
		this.setSize(new Dimension(620, 480));
	}
}
