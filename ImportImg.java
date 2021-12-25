import java.awt.image.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.util.Stack;

public class ImportImg {

    private static int THRESHOLD = 200;
    private static int MAXDEPTH = 16;
    private static int MINDEPTH = 8;
    public ImportImg (String fileName, int max, int min) throws IOException {
        
        MAXDEPTH = max; MINDEPTH = min;
        File openFile = new File("./test_img/" + fileName);
        File openFile2 = new File("./test_img/" + fileName); // file for line output source
        File saveFile = new File("./test_img/output.png"); // file for line output
        
        FileWriter writer = new FileWriter("./test_img/output.txt");
        BufferedImage img = ImageIO.read(openFile); // file for image read
        BufferedImage outputImage = ImageIO.read(openFile2); // file for path draw
        

        // run time output
        Date time = new Date();
        long start = time.getTime();
        System.out.printf("Started at %tH:%tM:%tS\n", time, time, time);

        // set up linear pixel array
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < pixels.length; i++) pixels[i] += 128;
        int[] xy = {img.getWidth(), img.getHeight()}; // width x height
        int counter = 0;

        // set up colour array
        byte[][][] rgb = new byte[3][xy[0]][xy[1]]; // [channel][xCord][yCord], 0=B, 1=G, 2=R
        for (int y = 0; y < xy[1]; y++) { for (int x = 0; x < xy[0]; x++) {
                rgb[0][x][y] = pixels[counter*3];
                rgb[1][x][y] = pixels[counter*3+1];
                rgb[2][x][y] = pixels[counter++*3+2];
        } } counter = 0; // reset counter value


        // calculate red/blue dot mean and boundary
        boolean[][] boundary = new boolean[xy[0]][xy[1]];
        int rAvgX, rAvgY, bAvgX, bAvgY, countR, countB;
        rAvgX = rAvgY = bAvgX = bAvgY = countR = countB = 0;
        for (int y = 0; y < xy[1]; y++) { for (int x = 0; x < xy[0]; x++) {
            boundary[x][y] = ((pixels[counter*3] + pixels[counter*3+1] + pixels[counter++*3+2]) < -THRESHOLD);
            if (rgb[2][x][y] - ((rgb[1][x][y] + rgb[0][x][y])/2) > THRESHOLD) { rAvgX += x; rAvgY += y; countR++; }
            else if (rgb[0][x][y] - ((rgb[1][x][y] + rgb[2][x][y])/2) > THRESHOLD) { bAvgX += x; bAvgY += y; countB++; }
        } }
        try {
            rAvgX /= countR; rAvgY /= countR; bAvgX /= countB; bAvgY /= countB; // calculate averages
        } catch (ArithmeticException e) { System.out.println("Warning: no pixels above the threshold were found for entrance/exit."); System.exit(1); }


        Cell entrance, exit;
        int imgHalfX = xy[0] / 2;
        int imgHalfY = xy[1] / 2;

        // initial splicing
        if (xy[0] != xy[1]) System.out.println("Warning: image size is not square ~ cells will be inefficiently spliced.");
        Cell q0 = new Cell(0, 0, imgHalfX, imgHalfY, 0); // top left
        Cell q1 = new Cell(imgHalfX+1, 0, xy[0]-1, imgHalfY, 0); // top right
        Cell q2 = new Cell(0, imgHalfY+1, imgHalfX, xy[1]-1, 0); // bottom left
        Cell q3 = new Cell(imgHalfX+1, imgHalfY+1, xy[0]-1, xy[1]-1, 0); // bottom right

        // link to adjacent nodes
        q0.addLink(q1); q0.addLink(q2);
        q1.addLink(q0); q1.addLink(q3);
        q2.addLink(q0); q2.addLink(q3);
        q3.addLink(q1); q3.addLink(q2);

        // divide working space to given depth around incidents
        for (int i = 0; i < MAXDEPTH; i++) {
            splice(q0, boundary, i); 
            flipState(q0); // must be group-unmarked after splice level, or else it will resplice processed cells
        }

        // find target nodes
        Search item = new Search(q0, q0);
        entrance = item.findPoint(q0, rAvgX, rAvgY);
        exit = item.findPoint(q0, bAvgX, bAvgY);
        if (entrance == null) { System.out.println("Node on starting point was not found."); System.exit(2); }
        if (exit == null) { System.out.println("Node on exit point was not found."); System.exit(2); }
        item.setEntrance(entrance);
        item.setExit(exit);
        
        ArrayList<Cell> sol = item.aStar();
        drawToImage(outputImage, sol, saveFile);
        /*

        // create image files for node border outline
        File sendNodes = new File("./test_img/nodes.png"); // file for node output
        File openFile3 = new File("./test_img/" + fileName); // file for node output source
        BufferedImage outputNodes = ImageIO.read(openFile3); // file for node draw


        flipState(q0, true); // reset check status

        // initialise pen object
        Graphics2D pen = outputNodes.createGraphics();
        Color green = new Color(0.1f, 0.5f, 0.5f);
        pen.setColor(green);
        pen.setStroke(new BasicStroke(1.0f));

        // edit buffer image and save
        printNodes(q0, 0, outputNodes, sendNodes, writer, pen);
        ImageIO.write(outputNodes, "png", sendNodes);*/
        

        writer.close();
        time = new Date();
        System.out.printf("Ended at %tH:%tM:%tS\n", time, time, time);
        System.out.printf("EXECUTION TIME: %d ms\n", eTime(start));

        // conclude program
    }

    /**
     * Draw solution path to source image
     * @param img Separate image for output
     * @param sol Solution sequence of cells
     * @param saveFile File output directory
     * @throws IOException
     */
    private void drawToImage (BufferedImage img, ArrayList<Cell> sol, File saveFile) throws IOException {
        Graphics2D pen = img.createGraphics();
        if (sol.size() < 2) { System.out.println("Not enough points in solution to output."); System.exit(1); }
        Iterator<Cell> iter = sol.iterator();
        Cell prev = iter.next();
        Cell curr = iter.next();
        while (iter.hasNext()) {
            pen.drawLine(prev.xAvg(), prev.yAvg(), curr.xAvg(), curr.yAvg());
            Color green = new Color(0.1f, 0.5f, 0.5f);
            pen.setColor(green);
            pen.setStroke(new BasicStroke(2.0f));
            prev = curr;
            curr = iter.next();
        } ImageIO.write(img, "png", saveFile);
    }
    
    /**
     * Recursively generates boundary outlines of all nodes for debugging
     * @param current Starting node, recursively iterates onwards
     * @param use Combination of outlines and centers - 0 for both centers and outlines, 1 for only outlines, and 2 for only centers
     * @param pen Pen object to draw lines
     * @throws IOException
     */
    /*private static void printNodes (Cell current, int use, Graphics2D pen) throws IOException {
        if (!current.state() && (use == 0 || use == 1)) {
            pen.drawLine(current.getXS(), current.getYS(), current.getXE(), current.getYS());
            pen.drawLine(current.getXS(), current.getYS(), current.getXS(), current.getYE());
            pen.drawLine(current.getXE(), current.getYS(), current.getXE(), current.getYE());
            pen.drawLine(current.getXS(), current.getYE(), current.getXE(), current.getYE());
        }
        else if (!current.state() && (use == 0 || use == 2)) pen.drawLine(current.xAvg(), current.yAvg(), current.xAvg(), current.yAvg());
        Iterator<Cell> iter = current.incident().iterator();
        current.setCheck(true);
        while (iter.hasNext()) {
            Cell adj = iter.next();
            if (!adj.check()) printNodes(adj, use, pen);
        }
    }*/

    /**
     * Sets all statuses of the same-state connected graph to false (unchecked)
     * @param current Start point, typically entrance node
     */
    private static void flipState (Cell start) {
        Stack<Cell> list = new Stack<Cell>();
        list.add(start);
        Cell curr, next;
        Iterator<Cell> incidents;
        while (!list.isEmpty()) {
            curr = list.pop();
            curr.setCheck(false); // set to unvisited
            incidents = curr.incident().iterator(); // store all adjacent nodes
            if (!incidents.hasNext()) System.exit(3); // terminate if node has no adjacent nodes (error)
            while (incidents.hasNext()) { next = incidents.next(); if (next.check()) list.add(next); } // add all adjacent unchecked to list
        }
    }

    /**
     * Splices all candidate cells by a single level
     * @param current Current node to splice incident cells
     * @param collider Boolean array for splice decision
     * @param level Finest allowed splice level
     */
    private static void splice (Cell current, boolean[][] collider, int level) { // fractal patterns seen at larger scales
        ArrayList<Cell> cells = current.forwardIncident(); // current list of cells
        current.setCheck(true); // mark as checked
        if (current.state() && current.diceLevel() <= level) {
            current.quadSplice();
        } else if (!current.state() && current.diceLevel() < MINDEPTH) current.quadSpliceOld();
        current.set(checkColliding(current, collider)); // update collision for parent node, children assumed colliding
        int size = cells.size();
        for (int i = 0; i < size; i++) {
            if (!cells.get(i).check()) splice(cells.get(i), collider, level);
        }
    }

    /**
     * Checks if given cell has any collider within its boundaries
     * @param current Cell to check
     * @param collider Collider array of binary collision status
     * @return True if colliding, false if not
     */
    private static boolean checkColliding (Cell current, boolean[][] collider) {
        int[] xDim = {current.getXS(), current.getXE()};
        int[] yDim = {current.getYS(), current.getYE()};
        for (int y = yDim[0]; y < yDim[1]+1; y++) { for (int x = xDim[0]; x < xDim[1]+1; x++) {
            if (collider[x][y]) return (true);
        } } return (false);
    }

    /**
     * Returns time difference between given Unix epoch time
     * @param start
     * @return
     */
    private static long eTime (long start) {
        Date test = new Date();
        return (test.getTime() - start);
    }
}
