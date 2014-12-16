package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import structs.Clause;

public class Scanner {

    private DPLLSolver s;
    
    public Scanner(DPLLSolver s) {
        this.s = s;
    }
    
    public void run(String[] args) {
        String line;
        String filename = args[0];
        try {
            File file = new File(filename);
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            
            boolean ReadingContent = false;
            Set<Integer> cl = new HashSet<Integer>();
            String[] tmp;
            
            while ((line = br.readLine()) != null) {
                if (!ReadingContent) {
                    if (line.startsWith("c ")) {    //Comment line, ignore it.
                        continue;
                    }
                    if (line.startsWith("p ")) {
                        String[] param = line.split("\\s+");
                        try {
                            if (!param[1].equals("cnf")) {
                                throw new Exception("\""+ param[1] + "\" no recognized, should be \"cnf\"");
                            }
                            s.vars = Integer.parseInt(param[2]);
                            s.cl_count = Integer.parseInt(param[3]);
                            ReadingContent = true;
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                            System.exit(0);
                        }
                    }
                } else {
                    //System.out.println(line);
                    tmp = line.split("\\s+");
                    for (String i : tmp) {
                        if (i.equals("0")) {
                            addClause(cl);
                            
                            cl = new HashSet<Integer>();
                            //System.out.println();
                            
                        } else {
                            try {
                                Integer in = Integer.parseInt(i);
                                if (Math.abs(in) > s.vars) {
                                    System.err.println("Absolute value of number " + in + " is greater than expected " + s.vars + ".");
                                    continue;
                                }
                                cl.add(in);
                                //System.out.print(in + " ");
                            } catch (Exception e) {
                                System.err.println("\""+ i +"\" can not be recognized as an integer.");
                            }
                        }
                    }
                }
                
            }
            addClause(cl);
            br.close();
            
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void addClause(Set<Integer> cl) {
        Set<Integer> to_eliminate = new HashSet<Integer>();
        for (Integer prop : cl) {
            //eliminate -x,x if they present in the same clause!
            if (cl.contains(-prop)) {
                to_eliminate.add(prop);
                to_eliminate.add(new Integer(-prop));
            }
            
        }
        for (Integer prop : to_eliminate) {
            cl.remove(prop);
        }
        
        Clause C_cl = new Clause(cl);
        for (Integer prop : cl) {
            //Integer pos_prop = Math.abs(prop);
            if (!s.cl_idx.containsKey(prop)) {
                Set<Clause> empty_cl_set = new HashSet<Clause>();
                s.cl_idx.put(prop, empty_cl_set);
            }
            s.cl_idx.get(prop).add(C_cl);
        }
             
        s.clauses.add(C_cl);
    }

}
