import java.util.ArrayList;
import java.util.Iterator;

public class Cell {
    private int xStart, yStart, xEnd, yEnd, dicingFactor;
    private int pathLength = 0;
    private int fVal = 0;
    private boolean state = true; // true if colliding, false if empty
    private boolean checked = true; // nodes assumed checked on creation, will be verified next pass
    private ArrayList<Cell> neighbours = new ArrayList<Cell>();
    private Cell parent = null;

    /**
     * Creates island cell with no linkages to other cells.
     * @param xS Left (first) x-coordinate
     * @param yS Top (first) y-coordinate
     * @param xE Right (end) x-coordinate
     * @param yE Bottom (end) y-coordinate
     * @param diceInput Upwards scaling level of dicing applied to cell, from 0
     */
    public Cell (int xS, int yS, int xE, int yE, int diceInput) {
        this.xStart = xS;
        this.yStart = yS;
        this.xEnd = xE;
        this.yEnd = yE;
        this.dicingFactor = diceInput;
    }

    /**
     * Creates child cell with linkage to default other cell.
     * @param xS Left (first) x-coordinate
     * @param yS Top (first) y-coordinate
     * @param xE Right (end) x-coordinate
     * @param yE Bottom (end) y-coordinate
     * @param parent Node to assume linkage to
     * @param diceInput Upwards scaling level of dicing applied to cell, from 0
     */
    public Cell (int xS, int yS, int xE, int yE, Cell parent, int diceInput) {
        this.xStart = xS;
        this.yStart = yS;
        this.xEnd = xE;
        this.yEnd = yE;
        this.neighbours.add(parent);
        this.dicingFactor = diceInput;
    }

    /**
     * Checks if cell is incident with boundary line(s)
     * @return Returns status of incidence
     */
    public boolean state () { return (this.state); } // gets boundary incident state
    /**
     * Updates incident status with boundary line(s)
     * @param update New status to update with
     */
    public void set (boolean update) { this.state = update; } // sets boundary incident state

    /**
     * Checks if node has been visited
     * @return Visitation status
     */
    public boolean check () { return (this.checked); } // checks if visited
    /**
     * Updates visitation status
     * @param update Update variable
     */
    public void setCheck (boolean update) { this.checked = update; } // updates visited/marked state

    public int yAvg () { return ((yStart + yEnd)/2); } // average vertical coordinate
    public int xAvg () { return ((xStart + xEnd)/2); } // average horizontal coordinate
    public int xSize () { return (this.xEnd - this.xStart); }
    public int ySize () { return (this.yEnd - this.yStart); }

    public void addLink (Cell newLink) { this.neighbours.add(newLink); } // adds new one-way connection to cell
    public void deLink (Cell oldCell) { this.neighbours.remove(oldCell); } // removes old link from cell

    public int diceLevel () { return (this.dicingFactor); }
    public void addDice () { this.dicingFactor++; }

    public int getXS () { return (this.xStart); }
    public int getXE () { return (this.xEnd); }
    public int getYS () { return (this.yStart); }
    public int getYE () { return (this.yEnd); }

    public int numChild () { return (this.neighbours.size()); }

    public int getF () { return (this.fVal); }
    public void setF (Cell pre, Cell end) { this.fVal = this.man(pre) + this.man(end); }

    public void setParent (Cell parent) { this.parent = parent; }
    public boolean hasParent () { return (this.parent != null); }
    public Cell getParent () { return (this.parent); }

    /**
     * Finds Manhattan distance between this and another given node.
     * @param other Other node to compare against
     * @return Manhattan distance between nodes
     */
    public int man (Cell other) {
        if (other == null) return (0);
        int x = other.xAvg() - this.xAvg(); x *= (x < 0) ? -1:1; // horizontal positive distance
        int y = other.yAvg() - this.yAvg(); y *= (y < 0) ? -1:1; // vertical positive distance
        return (x + y);
    }

    /**
     * Stores integer value, typically used for heuristic path length between two nodes
     * @param input Path length to set
     */
    public void setPath (int input) { this.pathLength = input; }

    /**
     * Returns previously set value
     * @return Path length, may be used for other purposes
     */
    public int getPath () { return (this.pathLength); }

    /**
     * Splits cell horizontally, current cell is top cell. (cell with lower y average)
     * @return Newly created cell, with links created.
     */
    private Cell hSplice () { // splits cell with horizontal division, returns new lower half
        if (this.yEnd - this.yStart < 2) { this.checked = true; return (null); } // cell resolution floor of 3 pixels
        this.checked = false; // assume spliced cell is unchecked, will have to revisit
        int halfPoint = this.yAvg();
        this.dicingFactor++;
        Cell child = new Cell (this.xStart, halfPoint+1, this.xEnd, this.yEnd, this, this.dicingFactor);
        this.yEnd = halfPoint;
        Iterator<Cell> temp = this.incident().iterator();
        Cell[] adj = new Cell[this.numChild()];
        int counter = 0;
        while (temp.hasNext()) adj[counter++] = temp.next();
        for (int i = 0; i < adj.length; i++) { // go through all cells connected to parent
            if (child.getYS() < adj[i].getYE() && child.getYS() > adj[i].getYS()) { // borders both parent and child
                adj[i].addLink(child);
                child.addLink(adj[i]);
            } else if (this.getYE() < adj[i].getYS()) { // only borders child
                adj[i].addLink(child);
                child.addLink(adj[i]);
                this.deLink(adj[i]);
                adj[i].deLink(this);
            }
        } this.neighbours.add(child); return (child);
    }

    /**
     * Splits cell vertically, current cell is left cell.
     * @return Newly created cell, with links created.
     */
    private Cell vSplice () { // splits cell with horizontal division, returns new lower half
        if (this.xEnd - this.xStart < 2) { this.checked = true; return (null); } // cell resolution floor of 3 pixels
        this.checked = false; // assume spliced cell is unchecked, will have to revisit
        int halfPoint = this.xAvg();
        this.dicingFactor++;
        Cell child = new Cell (halfPoint+1, this.yStart, this.xEnd, this.yEnd, this, this.dicingFactor);
        this.xEnd = halfPoint;
        Iterator<Cell> temp = this.incident().iterator();
        Cell[] adj = new Cell[this.numChild()];
        int counter = 0;
        while (temp.hasNext()) adj[counter++] = temp.next();
        for (int i = 0; i < adj.length; i++) { // go through all cells connected to parent
            if (child.getXS() < adj[i].getXE() && child.getXS() > adj[i].getXS()) { // borders both parent and child
                adj[i].addLink(child);
                child.addLink(adj[i]);
            } else if (this.getXE() < adj[i].getXS()) { // only borders child
                adj[i].addLink(child);
                child.addLink(adj[i]);
                this.deLink(adj[i]);
                adj[i].deLink(this);
            } // change nothing if only bordering parent
        } this.neighbours.add(child); return (child);
    }

    /**
     * Uses vSplice and hSplice to split a cell into 4 equal sections
     */
    public void quadSplice () {
        if (this.xEnd - this.xStart < 2) return;
        if (this.yEnd - this.yStart < 2) return;
        Cell rightChild = vSplice();
        hSplice();
        rightChild.hSplice();
    }
    
    /**
     * Splices into quarters while retaining old statuses. Used when cell is known to be unoccupied, and a recheck is not required.
     */
    public void quadSpliceOld () {
        if (this.xEnd - this.xStart < 2) return;
        if (this.yEnd - this.yStart < 2) return;
        Cell rightChild = vSplice(); Cell bl = hSplice(); Cell br = rightChild.hSplice();
        rightChild.set(false); bl.set(false); br.set(false); // revert if something breaks;
        rightChild.set(this.state); bl.set(this.state); br.set(this.state);
    }

    /**
     * Returns ArrayList<Cell> of all cells incident to current node.
     * @return ArrayList of all adjacent cells, null if no cells bordering (theoretically impossible).
     */
    public ArrayList<Cell> incident () {
        return (this.neighbours);
    }
    
    /**
     * Finds all cells in front (with greater x or y value) than the current node, does not return anything behind
     * @return ArrayList of all incident cells with greater coordinates than current
     */
    public ArrayList<Cell> forwardIncident () {
        Iterator<Cell> iter = this.neighbours.iterator();
        ArrayList<Cell> list = new ArrayList<Cell>();
        Cell curr;
        while (iter.hasNext()) {
            curr = iter.next();
            if (curr.getXS() >= this.getXS() && curr.getYS() >= this.getYS()) list.add(curr);
        } return (list);
    }
    /**
     * Checks by cell center alignment
     */
    @Override
    public boolean equals (Object other) {
        if (!(other instanceof Cell)) return (false);
        return (this.xAvg() == ((Cell)other).xAvg() && this.yAvg() == ((Cell)other).yAvg());
    } 
}