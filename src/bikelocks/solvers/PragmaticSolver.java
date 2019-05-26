package bikelocks.solvers;

import bikelocks.BikeLock;
import bikelocks.Graph;

import java.util.List;

public class PragmaticSolver implements PathSolver {

    private PathSolver ps;
    public PragmaticSolver(PathSolver solver){
        ps = solver;
    }

    @Override
    public List<BikeLock> solve(Graph g) {
        if(g.numEdges < 1600){
            return ps.solve(g);
        }

        Graph[] gs = g.cut(2);
        List<BikeLock> first, second;

        if(gs[0].nodes.contains(new BikeLock(g.settings.columns))) {
            first = solve(gs[0]);
            second = solve(gs[1]);
        } else {
            first = solve(gs[1]);
            second = solve(gs[0]);
        }

        BikeLock c1 = first.get(first.size()-1),
                 c2 = second.get(0);

        first.addAll(BikeLock.movesBetween(c1, c2));
        first.addAll(second);

        return first;
    }
}
