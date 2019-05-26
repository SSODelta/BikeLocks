package bikelocks.solvers;

import bikelocks.BikeLock;
import bikelocks.Graph;

import java.util.List;

public interface PathSolver {

    List<BikeLock> solve(Graph g);
}
