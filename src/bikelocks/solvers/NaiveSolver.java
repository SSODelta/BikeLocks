package bikelocks.solvers;

import bikelocks.BikeLock;
import bikelocks.Graph;

import java.util.*;

public class NaiveSolver implements PathSolver {

    private List<BikeLock> hamiltonianNaive(Graph g, BikeLock lock, Set<BikeLock> nodes, List<BikeLock> accum){
        if(nodes.isEmpty()){
            return accum;
        }

        for(BikeLock next : new HashSet<>(nodes)) {
            if (!next.equals(lock) && g.reachable(lock).contains(next))
                try {
                    nodes.remove(next);
                    accum.add(next);
                    return hamiltonianNaive(g, next, nodes, accum);
                } catch (NoSuchElementException e) {
                    nodes.add(next);
                    accum.remove(next);
                }
        }
        throw new NoSuchElementException("shit wack fam from "+lock);
    }

    @Override
    public List<BikeLock> solve(Graph g) {
        Set nds = new HashSet<>(g.nodes);
        BikeLock bl = new BikeLock(new int[g.settings.columns]);
        List<BikeLock> locks = new ArrayList<>();
        nds.remove(bl);
        locks.add(bl);
        return hamiltonianNaive(g, bl, nds, locks);
    }
}
