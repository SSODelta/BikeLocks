package bikelocks.solvers;


import bikelocks.BikeLock;
import bikelocks.Graph;
import org.sat4j.minisat.core.Solver;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.TimeoutException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SATSolver implements PathSolver {
    public static List<Integer> solveSat(IProblem solver, int N){
        // we are done. Working now on the IProblem interface
        IProblem problem = solver;
        try {
            if (problem.isSatisfiable()) {
                int[] ints = problem.model();
                List<Integer> locks = new ArrayList<>();
                for(int i=0; i<ints.length; i++)
                    if(ints[i] > 0)
                        locks.add(Graph.j(ints[i],N));
                return locks;
            } else {
                throw new SolverException("The graph does not have a Hamiltonian path");
            }
        } catch (TimeoutException e) {
            throw new SolverException("The computation timed out: "+e.getLocalizedMessage());
        }
    }

    @Override
    public List<BikeLock> solve(Graph g) {
        return solveSat(g.reduceToSAT(), g.nodes.size())
                .stream()
                .map(i -> g.nodes.get(i))
                .collect(Collectors.toList());
    }
}
