import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by nikol on 24-05-2019.
 */
public class BikeLock implements Comparable<BikeLock> {

    public static final int COLS = 4;
    public static final int SPINS = 1;
    private final int id = (int)(Math.random()*2560000);
    private final int[] offset;

    public BikeLock(){
        this(new int[COLS]);
    }

    public BikeLock(int[] offset){
        this.offset = offset;
    }

    public BikeLock(String s) {
        this(s.toCharArray());
    }

    public BikeLock(char[] chars) {
        this(ints(chars));
    }

    private static int[] ints(char[] chars) {
        int[] ints = new int[chars.length];
        for(int i=0; i<chars.length; i++)
            ints[i] = ((int)chars[i])-48;
        return ints;
    }

    public Set<BikeLock> adjacencySet(int depth){
        Set<BikeLock> locks = new TreeSet<>();

        locks.add(this);
        if(depth == 0)
            return locks;

        for(int cols = 1; cols <= COLS; cols++){
            for(int i=0; i<COLS+1-cols; i++){
                locks.addAll(rotate(i, cols, 1).adjacencySet(depth-1));
                locks.addAll(rotate(i, cols, -1).adjacencySet(depth-1));
            }
        }

        return locks;
    }

    public BikeLock rotate(int start, int len, int offset){
        int[] newOffset = new int[COLS];
        for(int i=0; i<COLS; i++)
            newOffset[i] = this.offset[i];
        for(int i=0; i<len; i++)
            newOffset[start+i] = (10+newOffset[start+i] + offset) % 10;
        return new BikeLock(newOffset);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<COLS; i++)
            sb.append(offset[i]);
        return sb.toString();
    }

    public int hashCode(){
        return Arrays.hashCode(offset);
    }

    public boolean equals(Object o){
        if(o.getClass() != BikeLock.class)
            return false;
        return Arrays.equals(((BikeLock) o).offset, offset);
    }

    @Override
    public int compareTo(BikeLock o) {
        return toString().compareTo(o.toString());
    }

    public Set<BikeLock> adjacencySet() {
        return adjacencySet(SPINS);
    }
}
