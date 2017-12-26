package gol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.Arrays;

public class GameOfLife extends Component {
	private int width;
	private int height;
	private int scale;
	private BufferedImage currentFrame;
	private BufferedImage neighbourCount;
	private LookupOp setLiveCellsOp;
	private LookupOp lookupDisplayValuesOp;
	private ConvolveOp countNeighboursOp;
	private AffineTransformOp scaleOp;

	public GameOfLife(int width, int height, int scale) {
		super();
		this.width = width;
		this.height = height;
		this.scale = scale;
		
		this.currentFrame = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		this.neighbourCount = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);

		Kernel kernel = new Kernel(3, 3, new float[] { 1f, 1f, 1f, 1f, 10f, 1f,
				1f, 1f, 1f });

		countNeighboursOp = new ConvolveOp(kernel);

		byte[] lookupLiveCells = new byte[19];
		Arrays.fill(lookupLiveCells, (byte) 0);
		lookupLiveCells[3] = 1;
		lookupLiveCells[12] = 1;
		lookupLiveCells[13] = 1;

		LookupTable lookupLiveCellsTable = new ByteLookupTable(0, lookupLiveCells);
		setLiveCellsOp = new LookupOp(lookupLiveCellsTable, null);

		byte[] lookupRedCells = new byte[19];
		Arrays.fill(lookupRedCells, (byte) 0);
		lookupRedCells[3] = (byte) 0xFF;
		lookupRedCells[13] = (byte) 0xFF;

		byte[] lookupGreenCells = new byte[19];
		Arrays.fill(lookupGreenCells, (byte) 0);
		lookupGreenCells[3] = (byte) 0xFF;
		lookupGreenCells[12] = (byte) 0xFF;

		byte[] lookupBlueCells = new byte[19];
		Arrays.fill(lookupBlueCells, (byte) 0);
		lookupBlueCells[3] = (byte) 0xFF;

		LookupTable lookupDisplayValuesTable = new ByteLookupTable(0,
				new byte[][]{lookupRedCells, lookupGreenCells, lookupBlueCells});
		lookupDisplayValuesOp = new LookupOp(lookupDisplayValuesTable, null);

		AffineTransform at = new AffineTransform();
		at.scale(scale, scale);
		scaleOp = 
		   new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	}

	private void randomise() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				currentFrame.setRGB(x, y, Math.random() < 0.5 ? 0 : 0x010101);
			}
		}
	}

	public void paint(Graphics g) {
		BufferedImage pixelValues = lookupDisplayValuesOp.filter(neighbourCount,
				null);
		
		g.drawImage(scaleOp.filter(pixelValues, null), 0, 0, null);
	}

	public Dimension getPreferredSize() {
		if (currentFrame == null) {
			return new Dimension(100, 100);
		} else {
			return new Dimension(currentFrame.getWidth(null)*scale,
					currentFrame.getHeight(null)*scale);
		}
	}

	public void tick() {
		neighbourCount = countNeighboursOp.filter(currentFrame,
				null);
		currentFrame = setLiveCellsOp.filter(neighbourCount, null);
	}

	public static void main(String[] args) throws InterruptedException {

		JFrame f = new JFrame("GOL");

		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		GameOfLife gol = new GameOfLife(100, 100, 10);
		gol.randomise();

		f.add(gol);
		f.pack();
		f.setVisible(true);

		while(true) {
			Thread.sleep(1000);
			gol.tick();
			f.repaint();
		}
	}
}
