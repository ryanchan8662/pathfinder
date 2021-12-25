import java.util.ArrayList;
import java.util.Iterator;

public class Search {
    private Cell entrance;
    private Cell exit;


    public Search (Cell startNode, Cell endNode) {
        this.entrance = startNode;
        this.exit = endNode;
    }

    public void setEntrance (Cell entranceCell) { this.entrance = entranceCell; }
    public void setExit (Cell exitCell) { this.exit = exitCell; }

    /**
     * Finds shortest path for given maze using A* with Manhattan heuristics
     * @return ArrayList containing all cells among solution path
     */
    public ArrayList<Cell> aStar () {
        Cell curr = aStarSolve(this.entrance);
        ArrayList<Cell> path = new ArrayList<Cell>();
        while (curr != null) {
            path.add(curr);
            curr = curr.getParent();
        } return (path.size() != 0 ? path:null);
    }

    private Cell aStarSolve (Cell node) {
        ArrayList<Cell> open = new ArrayList<Cell>(); // sorted off f heuristic
        ArrayList<Cell> closed = new ArrayList<Cell>(); // stores closed
        open.add(node);
        Cell q, a;
        Iterator<Cell> iter;
        while (!open.isEmpty()) {
            q = this.getSmallest(open); // find node with smallest heuristic value
            iter = q.incident().iterator(); // find all successors of node with smallest heuristic
            while (iter.hasNext()) { // for each successor node
                a = iter.next();
                if (!a.state()) { // for all unblocked incident nodes
                    Cell samePos = this.getPos(open, a); // any node with same position
                    if (samePos == null || samePos.getF() < a.getF()) { // only evaluate if there is no same position with lower
                        samePos = this.getPos(closed, a);
                        if (samePos == null || samePos.getF() < a.getF()) {
                            a.setParent(q); // set parent to source node
                            if (a.equals(this.exit)) return (a); // if exit found, terminate
                            a.setF(q, this.exit); // calculate heuristic value
                            open.add(a);
                        }
                    }
                }
            } open.remove(q); closed.add(q); // once node is processed, add to closed list
        } return (null);
    }

    private Cell getSmallest (ArrayList<Cell> list) {
        int size = list.size();
        Cell smallest = list.get(0);
        Cell curr;
        int record = smallest.getF();
        for (int i = 1; i < size; i++) {
            curr = list.get(i);
            if (curr.getF() < record) smallest = curr; record = curr.getF();
        } return (smallest);
    }

    private Cell getPos (ArrayList<Cell> list, Cell node) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            Cell curr = list.get(i);
            if (curr.xAvg() == node.xAvg() && curr.yAvg() == node.yAvg()) return (curr);
        } return (null);
    }

    /**
     * Recursively looks for the node containing target coordinates, uses Manhattan distance and ignores collisions
     * @param current
     * @param xTarget
     * @param yTarget
     * @return
     */
    public Cell findPoint (Cell current, int xTarget, int yTarget) {
        if (current == null) return (null);
        if (current.getXS() <= xTarget && current.getXE() >= xTarget && current.getYS() <= yTarget && current.getYE() >= yTarget) return (current);
        current.setCheck(true);
        Iterator<Cell> neighbours = current.incident().iterator();
        Cell iter;
        Cell best = null;
        int lowestMan = -1;
        while (neighbours.hasNext()) {
            iter = neighbours.next(); // for all neighbours
            if (!iter.check()) { // if node is unexplored
                int x = xTarget - iter.xAvg(); x *= (x < 0) ? -1:1; // horizontal positive distance
                int y = yTarget - iter.yAvg(); y *= (y < 0) ? -1:1; // vertical positive distance
                int man = x + y;
                if (lowestMan == -1) { best = iter; lowestMan = man; }
                else if (man < lowestMan) { best = iter; lowestMan = man; }
            }
        } 
        best = findPoint(best, xTarget, yTarget);
        current.setCheck(false); return (best);
    }
}
