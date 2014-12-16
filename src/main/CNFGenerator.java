package main;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class CNFGenerator {
    
    public String filename;
    
    public CNFGenerator(String filename) {
        this.filename = filename;
    }
    
    public int getRandom(int max) { //get a random integer from 1 to max
        return 1 + (int)(Math.random() * max);
    }
    
    public void run(int N, int K, int L) {
        String ret = "c This is a random generated CNF input file\n";
        ret += "p cnf " + N + " " + L +"\n";
        String line;
        Set<Integer> s_l = new HashSet<Integer>();
        for (int i = 0 ; i < L ;i ++) {
            s_l.clear();
            line = "";
            do {
                Integer a = getRandom(N);
                if (s_l.contains(a) || s_l.contains(-a)) {
                    continue;
                }
                if (getRandom(2) == 1)
                    a = -a;
                s_l.add(a);
            } while (s_l.size() < K);
            for (Integer prop : s_l) {
                line += (prop + " ");
            }
            if (i < L - 1) {
                line += 0;
            }
            line += "\n";
            ret += line;
        }
        //System.out.print(ret);
        Writer writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                  new FileOutputStream(filename), "utf-8"));
            writer.write(ret);
            writer.close();
        } catch (IOException ex) {
            System.err.println("Error in writting");
        }
        
    }
    
}
