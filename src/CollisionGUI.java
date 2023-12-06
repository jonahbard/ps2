import java.awt.*;

import javax.swing.*;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Using a quadtree for collision detection
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015
 * @author CBK, Spring 2016, updated for blobs
 * @author CBK, Fall 2016, using generic PointQuadtree
 * @author Jonah Bard, Daniel Katz
 */
public class CollisionGUI extends DrawingGUI {
	private static final int width=800, height=600;		// size of the universe

	private List<Blob> blobs;						// all the blobs
	private List<Blob> colliders;					// the blobs who collided at this step
	private char blobType = 'b';						// what type of blob to create
	private char collisionHandler = 'c';				// when there's a collision, 'c'olor them, or 'd'estroy them
	private int delay = 100;							// timer control
	private static final int collisionRadius = 0;

	/**
	 * Constructor for class
	 */
	public CollisionGUI() {
		super("super-collider", width, height);

		blobs = new ArrayList<>();

		// Timer drives the animation.
		startTimer();
	}

	/**
	 * Adds a blob of the current blobType at the location
	 * @param x
	 * @param y
	 */
	private void add(int x, int y) {
		if (blobType == 'b') {
			blobs.add(new Bouncer(x,y,width,height));
		} else if (blobType == 'w') {
			blobs.add(new Wanderer(x,y));
		} else {
			System.err.println("Unknown blob type "+blobType);
		}
	}

	/**
	 * DrawingGUI method, here creating a new blob
	 * @param x		x coordinate of mouse press
	 * @param y		y coordinate of mouse press
	 */
	public void handleMousePress(int x, int y) {
		add(x,y);
		repaint();
	}

	/**
	 * DrawingGUI method
	 * @param k	the key that was pressed
	 */
	public void handleKeyPress(char k) {
		if (k == 'f') { // faster
			if (delay>1) delay /= 2;
			setTimerDelay(delay);
			System.out.println("delay:"+delay);
		} else if (k == 's') { // slower
			delay *= 2;
			setTimerDelay(delay);
			System.out.println("delay:"+delay);
		} else if (k == 'r') { // add some new blobs at random positions
			for (int i=0; i<10; i++) {
				add((int)(width*Math.random()), (int)(height*Math.random()));
				repaint();
			}			
		} else if (k == 'c' || k == 'd') { // control how collisions are handled
			collisionHandler = k;
			System.out.println("collision:"+k);
		} else { // set the type for new blobs
			blobType = k;			
		}
	}

	/**
	 * DrawingGUI method, here drawing all the blobs and then re-drawing the colliders in red
	 * @param g
	 */
	public void draw(Graphics g) {
		// Ask all the blobs to draw themselves.

		// create a set of blobs that we will draw as red if collided or black if not
		//	a set enables O(1) lookup for if a blob has collided or not
		Set<Blob> collidersSet = (colliders != null) ? new HashSet<>(colliders) : new HashSet<>();

		for (Blob b: blobs){
			if (collidersSet.contains(b)) {
				// Ask the colliders to draw themselves in red.
				g.setColor(Color.red);
			} else {
				// Ask the non-colliders to draw themselves in black
				g.setColor(Color.black);
			}
			// Draw each dot in the required color
			g.fillOval((int)(b.getX()-b.getR()), (int)(b.getY()-b.getR()), (int)b.getR()*2, (int)b.getR()*2);
		}
	}

	/**
	 * Sets colliders to include all blobs in contact with another blob
	 */
	private void findColliders() {
		// Create the tree based on all blobs
		PointQuadtree<Blob> tree = new PointQuadtree<>(blobs.get(0), 0, 0, width, height);
		for (Blob b: blobs) {
			if (b != tree.getPoint()) tree.insert(b);
		}

		// For each blob, see if anybody else collided with it

		// create a set to hold unique instances of collided blobs
		// this prevents us from manually checking for duplicate collisions as the set will automatically deduplicate
		Set<Blob> collidedBlobsSet = new HashSet<>();

		for (Blob b: blobs) {
			// create a set of collisions found in the circle around a dot
			List<Blob> dotsFound = tree.findInCircle(b.getX(), b.getY(), b.getR() * 2 + collisionRadius);

			// if there are less than 2 dots found, then the current dot is not colliding and we can move on
			if (dotsFound.size() < 2) continue;

			// if collisions have been found, add collided blobs
			collidedBlobsSet.addAll(dotsFound);
		}
		// Turn the arraylist into a
		colliders = new ArrayList<>(collidedBlobsSet);
	}

	/**
	 * DrawingGUI method, here moving all the blobs and checking for collisions
	 */
	public void handleTimer() {
		// Ask all the blobs to move themselves.
		for (Blob b : blobs) {
			b.step();
		}
		// Check for collisions
		if (!blobs.isEmpty()) {
			findColliders();
			//if in d mode, remove all blobs in that colliders group
			if (collisionHandler=='d') {
				blobs.removeAll(colliders);
				colliders = null;
			}
		}
		// Now update the drawing
		repaint();
	}

	/**
	 * Run an instance of this class visually
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CollisionGUI();
			}
		});
	}
}
