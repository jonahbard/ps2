import java.util.ArrayList;
import java.util.List;

/**
 * A point quadtree: stores an element at a 2D position, 
 * with children at the subdivided quadrants.
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015.
 * @author CBK, Spring 2016, explicit rectangle.
 * @author CBK, Fall 2016, generic with Point2D interface.
 * @author Jonah Bard, Daniel Katz
 * 
 */


public class PointQuadtree<E extends Point2D> {
	private E point;							// the point anchoring this node
	private int x1, y1;							// upper-left corner of the region
	private int x2, y2;							// bottom-right corner of the region
	private PointQuadtree<E> c1, c2, c3, c4;	// children


	/**
	 * Initializes a leaf quadtree, holding the point in the rectangle
	 * @param point		point placed in the region
	 * @param x1		top left x coord
	 * @param y1		top left y coord
	 * @param x2		bottom right x coord
	 * @param y2		bottom right y coord
	 */
	public PointQuadtree(E point, int x1, int y1, int x2, int y2) {
		this.point = point;
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
	}

	// Getters

	/**
	 * Get Point
	 * @return
	 */
	public E getPoint() {
		return point;
	}

	/**
	 * Get x1
	 * @return
	 */
	public int getX1() {
		return x1;
	}

	/**
	 * Get y1
	 * @return
	 */
	public int getY1() {
		return y1;
	}

	/**
	 * get x2
	 * @return
	 */
	public int getX2() {
		return x2;
	}

	/**
	 * get y2
	 * @return
	 */
	public int getY2() {
		return y2;
	}

	/**
	 * Returns the child (if any) at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 */
	public PointQuadtree<E> getChild(int quadrant) {
		if (quadrant==1) return c1;
		if (quadrant==2) return c2;
		if (quadrant==3) return c3;
		if (quadrant==4) return c4;
		return null;
	}

	/**
	 * Returns whether or not there is a child at the given quadrant, 1-4
	 * @param quadrant	1 through 4
	 * @return			returns true or false for if the region has a child
	 */
	public boolean hasChild(int quadrant) {
		// Check if the quadrants passed in has a child present
		return (quadrant==1 && c1!=null)||
				(quadrant==2 && c2!=null) ||
				(quadrant==3 && c3!=null) ||
				(quadrant==4 && c4!=null);
	}

	/**
	 * Inserts the point into the tree with its own proper
	 * region corner coordinates, at its proper local
	 * variable (c1, c2, c3, or c4)
	 * @param p2
	 */
	public void insert(E p2) {
		int quadrant = findQuadrant(p2); //calls findQuadrant to easily store quadrant
		PointQuadtree<E> child = getChild(quadrant);

		if (child != null){
			// if this element's quadrant already has a child, insert new point into it
			child.insert(p2);
		} else {
			double newX1, newY1, newX2, newY2;

			//update corner coordinates to the new region boundaries
			newX1 = (quadrant == 2 || quadrant == 3) ? x1 : point.getX();
			newY1 = (quadrant == 1 || quadrant == 2) ? y1 : point.getY();
			newX2 = (quadrant == 1 || quadrant == 4) ? x2 : point.getX();
			newY2 = (quadrant == 3 || quadrant == 4) ? y2 : point.getY();

			PointQuadtree<E> newChild = new PointQuadtree<E>(p2, (int) newX1, (int)newY1, (int)newX2, (int)newY2);

			// set new quadtree in the appropriate quadrant
			if (quadrant == 1){
				c1 = newChild;
			} else if (quadrant == 2) {
				c2 = newChild;
			} else if (quadrant == 3) {
				c3 = newChild;
			} else {
				c4 = newChild;
			}
		}
	}

	/**
	 * finds which region quadrant, with respect to local point,
	 * a new point will fall into. Extends each quadrant by one unit
	 * in the counterclockwise direction (to account for when new
	 * point lies on same vertical or horizontal).
	 * @param p2
	 * @return		int corresponding to the quadrant the point is in
	 */
	private int findQuadrant(E p2){
		if (p2.getX() >= point.getX() && p2.getY() < point.getY()){
			return 1;
		} else if (p2.getX() < point.getX() && p2.getY() <= point.getY()) {
			return 2;
		} else if (p2.getX() <= point.getX() && p2.getY() > point.getY())  {
			return 3;
		} else {
			return 4;
		}
	}
	
	/**
	 * Finds the number of points in the quadtree (including its descendants)
	 */
	public int size() {
		int sum = 1;
		for (int i = 1; i < 5; i++){
			if (getChild(i) != null) sum += getChild(i).size();
		}
		return sum;
	}
	
	/**
	 * Builds a list of all the points in the quadtree (including its descendants).
	 * Uses an accumulator that's built up by allPointsHelper.
	 * @return list of all points
	 */
	public List<E> allPoints() {
		ArrayList<E> list = new ArrayList<E>();
		allPointsHelper(list);
		return list;
	}

	/**
	 * adds point to accumulator, and if there's a child recurse on it
	 * @param list
	 */
	private void allPointsHelper(List<E> list){
		list.add(point);
		for (int i = 1; i < 5; i++){
			if (hasChild(i)){
				getChild(i).allPointsHelper(list);
			}
		}
	}

	/**
	 * Uses the quadtree to find all points within the circle
	 * @param cx	circle center x
	 * @param cy  	circle center y
	 * @param cr  	circle radius
	 * @return    	the points in the circle (and the qt's rectangle)
	 */
	public List<E> findInCircle(double cx, double cy, double cr) {
		ArrayList<E> list = new ArrayList<>();
		findInCircleHelper(list, cx, cy, cr);
		return list;
	}

	/**
	 * if circle overlaps w/ rectangle, add point or recursively move to children until points added to accumulator
	 * @param list	list of existing found points
	 * @param cx	circle center x
	 * @param cy	circle center y
	 * @param cr	circle radius
	 */
	private void findInCircleHelper(List<E> list, double cx, double cy, double cr){
		if (Geometry.circleIntersectsRectangle(cx, cy, cr, x1, y1, x2, y2)) {
			//if the current point is in the circle add it to the accumulator
			if (Geometry.pointInCircle(point.getX(), point.getY(), cx, cy, cr)) list.add(point);
			//iterate through all quadrants
			for (int i = 1; i < 5; i++){
				if (!hasChild(i)) continue; //ignore if child doesn't exist
				getChild(i).findInCircleHelper(list, cx, cy, cr);
			}
		}
	}

	/**
	 * Driver method to test PointQuadtree implementation before moving onto GUI
	 * @param args
	 */
	public static void main(String[] args) {
		// test PointQuadtree class by ensuring proper element order insertion
		List<Dot> dotsInOrder = new ArrayList<>();


		dotsInOrder.add(new Dot(400, 300)); // 1st quadrant of center dot
		dotsInOrder.add(new Dot(450, 250)); // 1st quadrant of above dot
		dotsInOrder.add(new Dot(100, 100)); // 2nd quadrant
		dotsInOrder.add(new Dot(200, 350)); // 3rd quadrant
		dotsInOrder.add(new Dot(550, 550)); // 4th quadrant

		Dot centerDot = new Dot(300, 400); // center dot

		PointQuadtree<Dot> tree = new PointQuadtree<>(centerDot, 0,0,800,600);

		for (Dot d : dotsInOrder) {
			tree.insert(d);
		}

		dotsInOrder.add(0, centerDot);

		System.out.println("Size of dots list: "+dotsInOrder.size()+", Size of tree: "+tree.size());
		System.out.println("Dot's in order of proper tree to array order: "+dotsInOrder);
		System.out.println("Tree to array list dot order: "+tree.allPoints());
	}
}
