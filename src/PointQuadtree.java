import java.util.ArrayList;
import java.util.List;

/**
 * A point quadtree: stores an element at a 2D position, 
 * with children at the subdivided quadrants.
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2015.
 * @author CBK, Spring 2016, explicit rectangle.
 * @author CBK, Fall 2016, generic with Point2D interface.
 * 
 */


public class PointQuadtree<E extends Point2D> {
	private E point;							// the point anchoring this node
	private int x1, y1;							// upper-left corner of the region
	private int x2, y2;							// bottom-right corner of the region
	private PointQuadtree<E> c1, c2, c3, c4;	// children

	/**
	 * Initializes a leaf quadtree, holding the point in the rectangle
	 */
	public PointQuadtree(E point, int x1, int y1, int x2, int y2) {
		this.point = point;
		this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
	}

	// Getters
	
	public E getPoint() {
		return point;
	}

	public int getX1() {
		return x1;
	}

	public int getY1() {
		return y1;
	}

	public int getX2() {
		return x2;
	}

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
	 */
	public boolean hasChild(int quadrant) {
		return (quadrant==1 && c1!=null) || (quadrant==2 && c2!=null) || (quadrant==3 && c3!=null) || (quadrant==4 && c4!=null);
	}

	/**
	 * Inserts the point into the tree
	 */


	//TODO: MAKE SURE TO USE CORRECT Y SCALING FOR CG!!
	public void insert(E p2) {
		int quadrant = findQuadrant(p2);
		PointQuadtree<E> child = getChild(quadrant);

		if (child != null){
			child.insert(p2);
		} else {
			double newx1, newy1, newx2, newy2;

			newx1 = (quadrant == 2 || quadrant == 3) ? x1 : point.getX();
			newy1 = (quadrant == 1 || quadrant == 2) ? y1 : point.getY();
			newx2 = (quadrant == 1 || quadrant == 4) ? x2 : point.getX();
			newy2 = (quadrant == 3 || quadrant == 4) ? y2 : point.getY();

			if (quadrant == 1){
				c1 = new PointQuadtree<E>(p2, (int)newx1, (int)newy1, (int)newx2, (int)newy2);
			} else if (quadrant ==2) {
				c2 = new PointQuadtree<E>(p2, (int)newx1, (int)newy1, (int)newx2, (int)newy2);
			} else if (quadrant ==3) {
				c3 = new PointQuadtree<E>(p2, (int)newx1, (int)newy1, (int)newx2, (int)newy2);
			} else {
				c4 = new PointQuadtree<E>(p2, (int)newx1, (int)newy1, (int)newx2, (int)newy2);
			}
		}
	}

	/**
	 * finds which region quadrant, with respect to local point,
	 * a new point will fall into. Extends each quadrant by one unit
	 * in the counterclockwise direction (to account for when new
	 * point lies on same vertical or horizontal).
	 * @param p2
	 * @return
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
	 * Builds a list of all the points in the quadtree (including its descendants)
	 */
	public List<E> allPoints() {
		ArrayList<E> list = new ArrayList<E>();
		allPointsHelper(list);
		return list;
	}

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

	private void findInCircleHelper(List<E> list, double cx, double cy, double cr){
		if (Geometry.circleIntersectsRectangle(cx, cy, cr, x1, y1, x2, y2)) {
			if (Geometry.pointInCircle(point.getX(), point.getY(), cx, cy, cr)) list.add(point);
			for (int i = 1; i < 5; i++){
				PointQuadtree<E> child = getChild(i);
				if (child == null) continue;
				child.findInCircleHelper(list, cx, cy, cr);
			}
		}
	}

	public static void main(String args[]) {
		//lol maybe just do this later
	}

}
