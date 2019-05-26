package bikelocks;

import java.util.*;

/**
 * Created by nikol on 24-05-2019.
 */
public class BikeLock {

    public static final List<BikeLock> movesBetween(BikeLock from, BikeLock to){
        List<BikeLock> locks = moveTo(from, to);

        return locks.subList(1, locks.size()-1);
    }

    public static final List<BikeLock> moveTo(BikeLock from, BikeLock to){

        if(from.equals(to))
            return new ArrayList<>();

        // Find one move to apply
        int[] doffsets = new int[from.offset.length];
        for(int i=0; i<from.offset.length; i++)
            doffsets[i] = to.offset[i] - from.offset[i];

        // Apply move
        BikeLock next = from.rotate(0,0,0);

        // Compute rest of moves
        List<BikeLock> locks = moveTo(next, to);

        // Add from
        locks.add(0, from);

        // Return
        return locks;
    }

    private final int id = (int)(Math.random()*2560000);
    private final int[] offset;

    public BikeLock(int COLS){
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
    private Map<Integer, Map<Integer, Set<BikeLock>>> adjacencies = new HashMap<>();

    public Set<BikeLock> adjacencySet(int depth, int spinlen){
        if(!adjacencies.containsKey(depth)){
            adjacencies.put(depth, new HashMap<>());
        }
        if(adjacencies.get(depth).containsKey(spinlen)){
            return adjacencies.get(depth).get(spinlen);
        }


        Set<BikeLock> locks = new HashSet<>();

        locks.add(this);
        if(depth == 0)
            return locks;

        for(int cols = 1; cols <= offset.length; cols++){
            for(int i=0; i<offset.length+1-cols; i++){
                for(int s=1; s<=spinlen; s++) {
                    locks.addAll(rotate(i, cols, s).adjacencySet(depth - 1, spinlen));
                    locks.addAll(rotate(i, cols, -s).adjacencySet(depth - 1, spinlen));
                }
            }
        }
        adjacencies.get(depth).put(spinlen, locks);

        return locks;
    }

    public BikeLock rotate(int start, int len, int offset){
        int[] newOffset = new int[this.offset.length];
        for(int i=0; i<this.offset.length; i++)
            newOffset[i] = this.offset[i];
        for(int i=0; i<len; i++)
            newOffset[start+i] = (10+newOffset[start+i] + offset) % 10;
        return new BikeLock(newOffset);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<this.offset.length; i++)
            sb.append(offset[i]);
        return sb.toString();
    }

    public int hashCode(){
        return Arrays.hashCode(offset);
    }

    @Override
    public boolean equals(Object o){
        if(o.getClass() != BikeLock.class)
            return false;
        return Arrays.equals(((BikeLock) o).offset, offset);
    }

    public int getId() {
        return id;
    }
}
