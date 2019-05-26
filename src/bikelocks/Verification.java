package bikelocks;

import java.util.HashSet;
import java.util.List;

public class Verification {


    public static final boolean verify(List<BikeLock> locks, Graph g){

        // Verify list starts with 00..0
        if(!locks.get(0).equals(new BikeLock(g.settings.columns))){
            System.out.println("\tERROR: The path does not start with 00..0.");
            return false;
        }

        // Verify connected
        try{
            List<String> moves = g.moves(locks);
            // Verify all nodes in list
            StringBuilder sb = new StringBuilder();
            for(BikeLock l : g.nodes){
                if(!locks.contains(l)){
                    sb.append((sb.length()==0?"":", ") + l);
                }
            }
            if(sb.length() != 0){
                System.out.println("\tERROR: The path is missing the following locks: "+sb.toString()+".");
                return false;
            }

            // Verify no duplicates
            double coeff = (double)(g.nodes.size()-1) / moves.size();
            if(coeff < 1.0) {
                System.out.println("\tWARNING: The path is only "+((int)(coeff*1000))/10.0+"% optimal.");
                return false;
            } else {
                System.out.println("\tThe path is optimal :-)");
            }
        } catch(IllegalArgumentException e){
            System.out.println("\tERROR: The path is not connected.");
            return false;
        }

        // Test no extra nodes

        return true;
    }

}
