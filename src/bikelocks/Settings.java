package bikelocks;

import bikelocks.solvers.PathSolver;

public class Settings {

    public final int columns, spins, spinLength;
    public final PathSolver solver;

    public Settings(int columns, int spins, int spinLength, PathSolver solver){
        this.columns = columns;
        this.spinLength = spinLength;
        this.spins = spins;
        this.solver = solver;
    }
}
