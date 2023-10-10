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
 */
public class CollisionGUI extends DrawingGUI {
	private static final int width=800, height=600;		// size of the universe

	private List<Blob> blobs;						// all the blobs
	private List<Blob> colliders;					// the blobs who collided at this step
	private char blobType = 'b';						// what type of blob to create
	private char collisionHandler = 'c';				// when there's a collision, 'c'olor them, or 'd'estroy them
	private int delay = 100;							// timer control

	private static final int collisionRadius = 20;

	public CollisionGUI() {
		super("super-collider", width, height);

		blobs = new ArrayList<Blob>();

		// Timer drives the animation.
		startTimer();
	}

	/**
	 * Adds a blob of the current blobType at the location
	 */
	private void add(int x, int y) {
		if (blobType=='b') {
			blobs.add(new Bouncer(x,y,width,height));
		}
		else if (blobType=='w') {
			blobs.add(new Wanderer(x,y));
		}
		else {
			System.err.println("Unknown blob type "+blobType);
		}
	}

	/**
	 * DrawingGUI method, here creating a new blob
	 */
	public void handleMousePress(int x, int y) {
		add(x,y);
		repaint();
	}

	/**
	 * DrawingGUI method
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
	 */
	public void draw(Graphics g) {
		// Ask all the blobs to draw themselves.
		for (Blob b: blobs){
			g.fillOval((int)(b.getX()-b.getR()), (int)(b.getY()-b.getR()), (int)b.getR()*2, (int)b.getR()*2);
		}
		// Ask the colliders to draw themselves in red.
		g.setColor(Color.red);
		if (colliders != null) {
			for (Blob b : colliders) {
				g.fillOval((int) (b.getX() - b.getR()), (int) (b.getY() - b.getR()), (int) b.getR() * 2, (int) b.getR() * 2);
			}
		}
	}

	/**
	 * Sets colliders to include all blobs in contact with another blob
	 */
	private void findColliders() {
		// Create the tree
		PointQuadtree<Blob> tree = new PointQuadtree<>(blobs.get(0), 0, 0, width, height);
		for (Blob b: blobs) {
			if (b != tree.getPoint()) tree.insert(b);
		}
		// For each blob, see if anybody else collided with it
		colliders = new ArrayList<>();
		for (Blob b: blobs){
			// make it a set so it runs faster because there are no duplicates
			// cr: b.getR() + collisionRadius just in case r changes to be greater than collisionradius, ensuring that all overlapping blobs are considered colliding
			Set<Blob> curCollisions = new HashSet<>(tree.findInCircle(b.getX(), b.getY(), b.getR() + collisionRadius));
			if (curCollisions.size() < 2) continue;
			colliders.addAll(curCollisions);
		}
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

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CollisionGUI();
			}
		});
	}
}
