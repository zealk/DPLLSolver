package main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import structs.Clause;

public class DPLLSolver {
    
    private CNFGenerator gn;
    private Scanner sc;
    private Solver sl;
    
    public Set<Clause> clauses;
    public Map<Integer, Set<Clause>> cl_idx;   //invert index for clauses
    public int vars;
    public int cl_count;
    
    public DPLLSolver() {
        clauses = new HashSet<Clause>();
        cl_idx = new HashMap<Integer,Set<Clause>>();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Parameter number incorrect, " + 
                    args.length + " given, 1 expected.");
            return;
        }
        
        DPLLSolver s = new DPLLSolver();
        s.run(args);
    }
    
    public void run(String[] args) {
        gn = new CNFGenerator(args[0]);
        
        final int N = 100;
        final int K = 3;
        final int L = 300;
        gn.run(N,K,L);
        
        sc = new Scanner(this);
        sc.run(args);
        //printData();
        sl = new Solver(this);
        sl.run();
    }
    
    public void printData() {
        for (Clause cls : clauses) {
            for (Integer i : cls.clause) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
        System.out.println("=== Invert index of each prop ===");
        for (Entry<Integer,Set<Clause>> entry : cl_idx.entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (Clause clause : entry.getValue()) {
                for (Integer prop : clause.clause) {
                    System.out.print(prop + " ");
                }
                System.out.println();
            }
        }
    }
    

}
