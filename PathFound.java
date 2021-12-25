/**
 * Custom exception to terminate entire recursive stack instead of relying on recursive returns
 * Stores data through pass-by-reference to an ArrayList in the solve() method's stack frame
 * Final path is not stored in each recursion's activation record
 * @author jchan926
 *
 */
public class PathFound extends Exception{
	public PathFound (String msg) { super(msg); }
	public PathFound () { super("Path successfully found."); }
}
