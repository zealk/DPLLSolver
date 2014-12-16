package structs;

import java.util.HashSet;
import java.util.Set;

public class Clause {

    public Set<Integer> clause;
    public int decided;
    
    public Clause() {
        clause = new HashSet<Integer>();
        decided = 0;
    }
    
    public Clause(Set<Integer> cl) {
        clause = cl;
        decided = 0;
    }

}
