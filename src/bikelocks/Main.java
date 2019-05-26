package bikelocks;

import bikelocks.solvers.PragmaticSolver;
import bikelocks.solvers.SATSolver;

import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {

        Settings settings = new Settings(4, 2, 1, new PragmaticSolver(new SATSolver()));
        Graph g = new Graph(settings, new BikeLock(settings.columns).adjacencySet(settings.spins, settings.spinLength));

        new Cracker(settings, true).solve(g);
    }
}
