package main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
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
        //gn = new CNFGenerator(args[0]);
        
        int N = 125;
        final int K = 3;
        //int L = 300;
        
        PrintStream out;
        
        try {
            out = new PrintStream(new FileOutputStream("test/output.o"));
            System.setOut(out);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
        for (int step = N/5, L = N*4 ; L <= N*6 ; L += step) {
            System.out.println("N = " + N + " , L = " + L + " , L/N = " + (float)(L)/(float)(N));
            for (int loop = 0 ; loop < 100 ; loop++) { 
                gn = new CNFGenerator(args[0]);
                gn.run(N,K,L);
                
                sc = new Scanner(this);
                sc.run(args);
                //printData();
                
                for (int i = 1 ; i < 3 ;i++) {
                    sl = new Solver(this,i);
                    sl.run();
                    clean();
                }
                cleanAll();
                System.out.println();
            }
            System.out.println();
        }
    }
    
    private void cleanAll() {
        clauses.clear();
        cl_idx.clear();
    }

    private void clean() {
        for (Clause cl : clauses) {
            cl.decided = 0;
        }
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
