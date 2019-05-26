package bikelocks.solvers;

import bikelocks.BikeLock;
import bikelocks.Graph;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PragmaticSolver implements PathSolver {

    private PrintWriter pw = null;
    private ExecutorService exec = Executors.newWorkStealingPool(4);
    private PathSolver ps;
    private boolean print;

    public PragmaticSolver(PathSolver solver, String path) throws IOException {
        this(solver);
        FileWriter fileWriter = new FileWriter(path, true); //Set true for append mode
        pw = new PrintWriter(fileWriter);
    }

    public void close(){
        pw.close();
    }

    public PragmaticSolver(PathSolver solver, boolean print){
        ps = solver;
        this.print = print;
    }

    public PragmaticSolver(PathSolver solver){
        this(solver, false);
    }



    private List<BikeLock> solveGraph(int its, Graph g) {
        long before = System.nanoTime();
        print("["+its+"]: Solving. The graph has "+g.numEdges+" edges.");
        if(g.numEdges < 500){
            print("["+its+"]: Solving exact...");
            List<BikeLock> ls = ps.solve(g);
            long dt = System.nanoTime() - before;
            pw.println(g.numEdges+"\t"+dt);
            pw.flush();
            print("["+its+"]: Done!");
            return ls;
        }

        try {
            print("["+its+"]: Splitting into two...");
            Graph[] gs = g.cut(2);
            Graph g1, g2;

            if (gs[0].nodes.contains(new BikeLock(g.settings.columns))) {
                g1 = gs[0];
                g2 = gs[1];
            } else {
                g1 = gs[1];
                g2 = gs[0];
            }

            Future<List<BikeLock>> f1  = exec.submit(() -> solveGraph(its+1,g1)),
                                   f2 = exec.submit(() -> solveGraph(its+1,g2));

            List<BikeLock> first = f1.get(),
                           second = f2.get();

            print("["+its+"]: Combining...");

            BikeLock c1 = first.get(first.size() - 1),
                    c2 = second.get(0);

            first.addAll(BikeLock.movesBetween(c1, c2));
            first.addAll(second);

            long dt = System.nanoTime() - before;
            pw.println(g.numEdges+"\t"+dt);
            pw.flush();
            return first;
        } catch(SolverException | InterruptedException | ExecutionException e){
            print("["+its+"]: Infeasible: retrying.");
            return solve(g);
        }
    }

    private void print(String s) {
        if(print)
            System.out.println(s);
    }

    @Override
    public List<BikeLock> solve(Graph g) {
        return solveGraph(0, g);
    }
}
