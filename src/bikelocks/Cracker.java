package bikelocks;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class Cracker {

    private final Settings settings;
    private boolean print;

    public static final void solve(Settings s, Graph g) throws FileNotFoundException {
        new Cracker(s, true).solve(g);
    }

    public Cracker(Settings settings, boolean print){
        this.settings = settings;
        this.print = print;
    }

    private void print(String x){
        if(print)
            System.out.println(x);
    }

    public void solveNew() throws FileNotFoundException {
        solve(new Graph(settings, new BikeLock(settings.columns).adjacencySet(settings.spins, settings.spinLength)));
    }

    public void solve(Graph g) throws FileNotFoundException {

        print("+-------------------------+");
        print("|--  BIKE LOCK CRACKER  --|");
        print("+-------------------------+");
        print("   N="+settings.columns+", spins="+settings.spins+", len="+settings.spinLength);
        print("\n- Computing graph...");
        print("\t"+g.nodes.size()+" nodes, "+g.numEdges+" edges");

        PrintWriter pw = new PrintWriter("out.dot");
        pw.print(g);
        pw.close();

        print("\n- Computing Hamiltonian path...");
        List<BikeLock> l = settings.solver.solve(g);

        print("\n- Verifying path...");
        Verification.verify(l, g);

        print("\n -- RESULTS -- \n");
        print("Codes:\t" + l);
        try {
            List<String> moves = g.moves(l);
            print("Moves:\t" + moves);
            print("\t\t"+ moves.size()+" rotations ("+((int)(1000*(1 - g.moves(l).size() / Math.pow(10, settings.columns))))/10.0+"% saved)");

            print("\nBreaking the lock would take "+estimate(moves.size()));
        } catch(IllegalArgumentException e){
            print("Moves:\t-invalid-");
        }

    }

    private final static double lo = 1.0, hi = 2.0;
    private String estimate(int x) {

        int l = (int)(lo * x),
            h = (int)(hi * x);

        if(h < 60){
            return l+"-"+h+" seconds";
        }

        if(h < 60*60){
            int hm = (int)Math.round(h/60.0);
            if(l < 60){
                return l + " seconds to "+hm+" minutes";
            }
            int lm = (int)Math.round(h/60.0);
            return lm+"-"+hm+" minutes";
        }

        return "a long time";
    }
}
