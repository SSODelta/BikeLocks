package bikelocks;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

import java.util.*;

/**
 * Created by nikol on 25-05-2019.
 */
public class Graph {


    public List<BikeLock> nodes = new ArrayList<>();
    private Map<BikeLock, List<BikeLock>> edges = new HashMap<>();
    private Map<BikeLock, Integer> weights = new HashMap<>();
    public int numEdges = 0;
    public Settings settings;


    public Graph(Settings settings, Collection<BikeLock> locks){
        this.settings = settings;

        for(BikeLock l : locks)
            add(l);
        for(BikeLock from : locks){
            Set<BikeLock> adj = from.adjacencySet(1, settings.spinLength);
            for(BikeLock to : adj)
                if(locks.contains(to))
                    addEdge(from, to);
        }

        normalize();
    }

    private int x(int i, int j, int N){
        return N* (i+1) + j;
    }

    private int neg(int x){
        return -x;
    }

    private int i(int x, int N){
        return x / N-1;
    }

    public static int j(int x, int N){
        return x % N;
    }

    public void normalize(){
        Collections.sort(nodes, Comparator.comparing(BikeLock::toString));
    }

    private static class Edge implements Comparable<Edge> {
        private int weight = (int)(Math.random()*100000);
        private BikeLock from, to;
        public Edge(BikeLock from, BikeLock to){
            this.from = from;
            this.to = to;
        }

        @Override
        public String toString(){
            return from + " -> "+to;
        }
        @Override
        public int compareTo(Edge o) {
            return weight - o.weight;
        }
        @Override
        public boolean equals(Object o){
            if(!(o.getClass() == Edge.class))
                return false;
            Edge e = ((Edge) o);
            return from.equals(e.from) && to.equals(e.to);
        }
        @Override
        public int hashCode(){
            return Objects.hash(from,to);
        }
    }
    private class Forest implements Comparable<Forest> {
        private Random r = new Random();
        private List<BikeLock> edges = new ArrayList<>();
        public Forest(BikeLock e){
            edges.add(e);
        }
        private <T> T rand(List<T> ts){
            return ts.get(r.nextInt(ts.size()));
        }
        public BikeLock bite(){
            return rand(reachable(rand(edges)));
        }
        public void consume(Forest f){
            edges.addAll(f.edges);
        }
        public String toString(){
            return edges.toString();
        }

        @Override
        public int compareTo(Forest o) {
            return toString().compareTo(o.toString());
        }
    }
    public Graph[] cut(){
        return cut(2);
    }
    public Graph[] cut(int pieces){
        Queue<Forest> forests = new LinkedList<>();
        TreeSet<Edge> edges = new TreeSet<>();
        Map<BikeLock, Forest> forestMap = new HashMap<>();
        for(BikeLock node : nodes) {
            Forest f = new Forest(node);
            forestMap.put(node, f);
            forests.add(f);
            for (BikeLock to : reachable(node)) {
                edges.add(new Edge(node, to));
            }
        }

        while(forests.size() > pieces){
            Forest f1 = forests.poll();
            BikeLock lock = f1.bite();
            Forest f2 = forestMap.get(lock);

            if(!f1.equals(f2)){
                f1.consume(f2);
                forests.remove(f2);
                for(BikeLock l : f2.edges)
                    forestMap.put(l, f1);
            }

            forests.add(f1);
        }

        Forest f1 = forests.poll(),
               f2 = forests.poll();

        Graph g1 = new Graph(settings, f1.edges),
              g2 = new Graph(settings, f2.edges);

        return new Graph[]{g1, g2};
    }


    /**
     * reduction from: https://www.csie.ntu.edu.tw/~lyuu/complexity/2011/20111018.pdf
     * @return
     */
    public IProblem reduceToSAT(){
        int N = nodes.size();

        final int MAXVAR = (N+1)*N;
        final int NBCLAUSES = 10*N*N*N;

        ISolver solver = SolverFactory.newDefault();

        // prepare the solver to accept MAXVAR variables. MANDATORY for MAXSAT solving
        solver.newVar(MAXVAR);
        solver.setExpectedNumberOfClauses(NBCLAUSES);


        // Construct N x N matrix where x[i,j] = 1 iff i'th position in path is node j
        // Since we use Sat4j format we encode x[i,j] as the integer N*i + j

        // add clauses
        try {
            // 0. start with 000...00
            int[] zs = new int[1];
            zs[0] = x(0, 0, N);
            solver.addClause(new VecInt(zs));

            // 1. Each node j must appear in the path.
            for(int j=0; j<N; j++) {
                int[] ints = new int[N];
                for(int i=0; i<N; i++){
                    ints[i] = x(i,j,N);
                }
                solver.addClause(new VecInt(ints));
            }

            // 2. No node j appears twice in the path
            for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
            for(int k=0; k<N; k++)
                if(i != k){
                    int[] ints = new int[2];
                    ints[0] = neg(x(i,j,N));
                    ints[1] = neg(x(k,j,N));
                    solver.addClause(new VecInt(ints));
                }

            // 3. Every position i on the path must be occupied
            for(int i=0; i<N; i++){
                int[] ints = new int[N];
                for(int j=0; j<N; j++){
                    ints[j] = x(i,j,N);
                }
                solver.addClause(new VecInt(ints));
            }

            // 4. No two nodes j and k occupy the same position in the path.
            for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
            for(int k=0; k<N; k++)
                if(j != k){
                    int[] ints = new int[2];
                    ints[0] = neg(x(i,j,N));
                    ints[1] = neg(x(i,k,N));
                    solver.addClause(new VecInt(ints));
                }

            // 5. Nonadjacent nodes i and j cannot be adjacent in the path.
            for(int i=0; i<N; i++)
            for(int j=0; j<N; j++)
            for(int k=0; k<N-1; k++)
                if(!(reachable(nodes.get(i)).contains(nodes.get(j)))){
                    int[] ints = new int[2];
                    ints[0] = neg(x(k,i,N));
                    ints[1] = neg(x(k+1,j,N));
                    solver.addClause(new VecInt(ints));
                }

        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        return solver;
    }

    private String rotFormat(int i, int cols){
        String s = "";

        for(int j=0; j<settings.columns; j++){
            s+=(j < i || j >= cols+i ? ".": "o");
        }

        return s;
    }

    private String move(BikeLock from, BikeLock to){
        if(from.equals(to))
            return "";

        for(int cols = 1; cols <= settings.columns; cols++){
            for(int i=0; i<settings.columns+1-cols; i++){
                for(int s=1; s<=settings.spinLength; s++) {
                    if (from.rotate(i, cols, s).equals(to)) {
                        return "-" + rotFormat(i, cols);
                    }
                    if (from.rotate(i, cols, -s).equals(to)) {
                        return "+" + rotFormat(i, cols);
                    }
                }
            }
        }
        throw new IllegalArgumentException("no such move: "+from+" -> "+to);
    }

    public List<String> moves(List<BikeLock> list){
        List<String> strs = new ArrayList<>();
        for(int i=1; i<list.size(); i++)
            strs.add(move(list.get(i-1),list.get(i)));

        return strs;
    }

    public void add(BikeLock l){
        nodes.add(l);
        edges.put(l, new ArrayList<>());
        weights.put(l, 0);
    }

    public void addEdge(BikeLock from, BikeLock to){
        edges.get(from).add(to);
        numEdges++;
    }

    public List<BikeLock> reachable(BikeLock l){
        return edges.getOrDefault(l, new ArrayList<>());
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("graph {\n");
        for(BikeLock from : nodes)
        for(BikeLock to : reachable(from))
            if(from.getId() < to.getId())
                sb.append("  "+from+" -- "+to+";\n");
        return sb.append("}").toString();
    }

}
