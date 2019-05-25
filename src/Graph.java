import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by nikol on 25-05-2019.
 */
public class Graph {

    public static void main(String[] args) throws FileNotFoundException {
        int i =0;
        while(++i<10000) {
            try {
                if(i%100 == 1)
                    System.out.println("Iteration "+(i-1));
                Graph g = new Graph(new BikeLock().adjacencySet(2));
                PrintWriter pw = new PrintWriter("out.dot");
                pw.print(g);
                pw.close();

                List<BikeLock> l =g.computePath();
                System.out.println(i+"\t"+l);
                System.out.println("\t"+moves(l));
                System.out.println("\t"+moves(l).size()+" rotations ("+((int)(1000*(1 - 2*moves(l).size() / Math.pow(10, BikeLock.COLS))))/10.0+"% saved)");
                System.exit(0);
            } catch(NoSuchElementException e){}
        }
        System.out.println("Timed out");
    }

    private static String rotFormat(int i, int cols){
        String s = "";

        for(int j=0; j<BikeLock.COLS; j++){
            s+=(j < i || j >= cols+i ? ".": "o");
        }

        return s;
    }

    private static String move(BikeLock from, BikeLock to){
        if(from.equals(to))
            return "";

        for(int cols = 1; cols <= BikeLock.COLS; cols++){
            for(int i=0; i<BikeLock.COLS+1-cols; i++){
                if(from.rotate(i, cols, 1).equals(to)){
                    return "-"+rotFormat(i,cols);
                }
                if(from.rotate(i, cols, -1).equals(to)){
                    return "+"+rotFormat(i,cols);
                }
            }
        }
        throw new IllegalArgumentException("no such move: "+from+" -> "+to);
    }

    private static List<String> moves(List<BikeLock> list){
        List<String> strs = new ArrayList<>();
        for(int i=1; i<list.size(); i++)
            strs.add(move(list.get(i-1),list.get(i)));

        return strs;
    }

    private Set<BikeLock> nodes = new TreeSet<>();
    private Map<BikeLock, TreeSet<BikeLock>> edges = new HashMap<>();
    private Map<BikeLock, Integer> weights = new HashMap<>();

    private List<BikeLock> computePath(){
        TreeSet nds = new TreeSet<>(nodes);
        BikeLock bl = new BikeLock(new int[BikeLock.COLS]);
        nds.remove(bl);
        List<BikeLock> bls = computePath(bl, nds);
        bls.add(0, bl);
        return bls;
    }

    private List<BikeLock> computePath(BikeLock lock, TreeSet<BikeLock> nodes){
        if(nodes.isEmpty()){
            ArrayList<BikeLock> ll = new ArrayList<>();
            ll.add(lock);
            return ll;
        }

        Optional<BikeLock> olock = nodes.stream().filter(l -> reachable(lock).contains(l) && !l.equals(lock)).sorted((o1, o2) -> ThreadLocalRandom.current().nextInt(-1, 2)).findFirst();
        BikeLock next = olock.get();
        nodes.remove(next);
        List<BikeLock> rec = computePath(next, nodes);
        rec.add(0, next);
        return rec;
    }

    public void add(BikeLock l){
        nodes.add(l);
        edges.put(l, new TreeSet<>());
        weights.put(l, 0);
    }

    public void addEdge(BikeLock from, BikeLock to){
        edges.get(from).add(to);
        edges.get(to).add(from);
    }

    public TreeSet<BikeLock> reachable(BikeLock l){
        return edges.getOrDefault(l, new TreeSet<>());
    }

    public Graph(Collection<BikeLock> locks){
        for(BikeLock l : locks)
            add(l);
        for(BikeLock from : locks){
            Set<BikeLock> adj = from.adjacencySet();
            for(BikeLock to : adj)
                if(locks.contains(to))
                    addEdge(from, to);
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("graph {\n");
        for(BikeLock from : nodes)
        for(BikeLock to : reachable(from))
            if(from.compareTo(to) < 0)
                sb.append("  "+from+" -- "+to+";\n");
        return sb.append("}").toString();
    }
}
