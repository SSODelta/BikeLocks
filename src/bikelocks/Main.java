package bikelocks;

import bikelocks.solvers.PragmaticSolver;
import bikelocks.solvers.SATSolver;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        test(3,2,1);
        test(3,2,2);
        test(4,2,1);
        test(4,2,2);
        test(3,3,2);
    }

    public static void test(int c, int s, int l) throws IOException{
        System.out.println("test("+c+", "+s+", "+l+")");
        PragmaticSolver solver = new PragmaticSolver(new SATSolver(), "stats.dat");
        Settings settings = new Settings(c, s, l, solver);
        Graph g = new Graph(settings, new BikeLock(settings.columns).adjacencySet(settings.spins, settings.spinLength));
        new Cracker(settings, true).solve(g);
        solver.close();
    }
}
