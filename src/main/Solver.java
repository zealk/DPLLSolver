package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import structs.Clause;

public class Solver {
    
    private DPLLSolver s;
    private Set<Integer> model;         //save the model already decided
    private Set<Integer> undecided;
    
    private int split_count = 0;
    
    private long begintime;
    private long endtime;
    
    private long max_time = 5000;
    
    private boolean killed = false;
    
    private int MODE = 1;
    
    public Solver(DPLLSolver s, int mode) {
        this.s = s;
        this.MODE = mode;
        model = new HashSet<Integer>();
        undecided = new HashSet<Integer>();
        for (int i = 1 ; i <= s.vars ; i++) {
            undecided.add(new Integer(i));
        }
    }
    
    public void run() {
        begintime = System.currentTimeMillis();
        boolean ret = DPLL();
        outputFormat(ret);
    }
    
    /**
     * Main function, DPLL procedule, in a recursive form
     * @return
     */
    public boolean DPLL() {
        //check every clause
        int ret = checkEveryClause();
        if (ret == 1) {
            //success, should print the solution
            //outputFormat(true);
            //System.exit(0);
            endtime = System.currentTimeMillis();
            return true;
        }
        if (ret == -1) {
            //failed in one branch
            return false;
        }

        
        if (killed) {
            return false;
        } else {
            long timenow = System.currentTimeMillis();
            if (timenow-begintime > max_time) {
                killed = true;
                return false;
            }
        }
        
        //else ret == 0, there may be a solution, need more calc
        Set<Clause> decide_now = new HashSet<Clause>();
        boolean b_ret;
        
        int prop = uniformProp();
        if (prop != 0 ) {
            decide(prop, decide_now);
            b_ret = DPLL();
            resume(prop, decide_now);
            return  b_ret;
        }
        
        prop = unitClause();
        if (prop != 0 ) {
            decide(prop, decide_now);
            b_ret = DPLL();
            resume(prop, decide_now);
            return  b_ret;
        }
        
        prop = chooseSplit();      //should return a value, not 0
        decide(prop, decide_now);
        b_ret = DPLL();
        resume(prop, decide_now);
        if (b_ret)
            return b_ret;
        
        decide(-prop, decide_now);
        b_ret = DPLL();
        resume(-prop, decide_now);
        return b_ret;
    }

    private void outputFormat(boolean res) {
        
        if (!res) {
            endtime = System.currentTimeMillis();
        }
        long costTime = (endtime - begintime);
        
        if (killed)
            System.out.print("!");
        System.out.print(costTime + "\t");
        System.out.print(split_count + "\t");
        System.out.print(res + "\t");
        
        /*
        int r;
        if (res)
            r = 1;
        else
            r = 0;

        System.out.println("c " + costTime + " ms.");
        System.out.println("c split count : " + split_count + ".");
        if (res) {
            printResult();
        }
        System.out.println("p cnf " + r + " " + s.vars + " " + s.cl_count);
        */
    }
    
    private void printResult() {
        System.out.print("c Result is: ");
        for (int i = 1 ; i <= s.vars ;i++) {
            if (model.contains(i)) {
                System.out.print(i + " ");
            } else if (model.contains(-i)) {
                System.out.print(-i + " ");
            }
        }
        System.out.println();
    }

    /**
     * Choose which prop to split, depends on MODE
     * MODE 0: random
     * MODE 1: 2-clause max heuristic
     * MODE 2: my undecided prop square weighted heuristic 
     * @return 
     *      positive number as prop to be true
     *      negative as to be false
     */
    private int chooseSplit() {
        split_count ++;
        if (MODE == 0) {    //randomly
            for (Integer prop : undecided) {
                return prop;
            }
        } else if (MODE == 1) { //2-clause heuristic
            return two_clause();
        } else if (MODE == 2) {
            return my_choice();
        }
        return 0;
    }

    public int getRandomFrom0(int max) { //get a random integer from 0 to max-1
        return (int)(Math.random() * max);
    }
    
    /**
     * Choose value by 2-clause heuristic
     * @return
     */
    private int two_clause() {
        int count_2cl[] = new int[s.vars + 1];
        for (int i = 1 ; i < count_2cl.length ;i++) {
            count_2cl[i] = 0;
        }
        for (Clause cl : s.clauses) {
            if (cl.decided < 1) {
                if (remainProp(cl) == 2) {
                    for (Integer prop : cl.clause) {
                        count_2cl[Math.abs(prop)] ++;
                    }
                }
            }
        }
        
        List<Integer> candidates = new ArrayList<Integer>();
        int max_2cl_count = 0;
        for (int i = 1 ; i < count_2cl.length ;i++) {
            if (model.contains(i) || model.contains(-i)) {
                continue;
            }
            if (count_2cl[i] == max_2cl_count) {
                candidates.add(new Integer(i));
            } else if (count_2cl[i] > max_2cl_count) {
                max_2cl_count = count_2cl[i];
                candidates.clear();
                candidates.add(new Integer(i));
            }
        }
        int idx = getRandomFrom0(candidates.size());
        int ret = candidates.get(idx);
        return ret;
    }
    
    /**
     * calc the undecided prop in a clause
     * @param cl
     * @return number of undecided prop in a clause
     */
    private int remainProp(Clause cl) {
        int i = 0;
        for (Integer prop : cl.clause) {
            if (truthValue(prop) == 0) {
                i++;
            }
        }
        return i;
    }

    /**
     * My heuristic choose function
     * @return
     */
    private int my_choice() {
        float weight[] = new float[s.vars + 1];     //record the weight of every prop
        int pos_neg[] = new int[s.vars + 1];        // > 0 if a prop appears positively more than negatively
        for (int i = 1 ; i < weight.length ;i++) {
            weight[i] = 0;
            pos_neg[i] = 0;
        }
        for (Clause cl : s.clauses) {
            if (cl.decided < 1) {
                int undecided_prop = remainProp(cl);
                for (Integer prop : cl.clause) {
                    weight[Math.abs(prop)] += 1.0f/(float)(undecided_prop * undecided_prop);
                    if (prop > 0)
                        pos_neg[Math.abs(prop)] ++;
                    else
                        pos_neg[Math.abs(prop)] --;
                }
            }
        }
        
        List<Integer> candidates = new ArrayList<Integer>();
        float max_weight_abs = 0;
        for (int i = 1 ; i < weight.length ;i++) {
            if (model.contains(i) || model.contains(-i)) {
                continue;
            }
            if (weight[i] == max_weight_abs) {
                candidates.add(new Integer(i));
            } else if (weight[i] > max_weight_abs) {
                max_weight_abs = weight[i];
                candidates.clear();
                candidates.add(new Integer(i));
            }
        }
        int idx = getRandomFrom0(candidates.size());
        int ret = candidates.get(idx);
        
        if (pos_neg[ret] < 0) {
            ret = -ret;
        }
        return ret;

    }


    /**
     * Create a environment for recursive call of DPLL.
     * decide prop to be true if positive or false if negative
     * @param prop
     * @param decide_now
     */
    private void decide(int prop, Set<Clause> decide_now) {
        model.add(prop);
        undecided.remove(Math.abs(prop));
        Set<Clause> cls = s.cl_idx.get(prop);
        if (cls == null)
            return;
        for (Clause cl : cls) {
            if (cl.decided < 1) {
                cl.decided = 1;
                decide_now.add(cl);
            }
        }
        
    }
    
    /**
     * Recover the environment after recursive call.
     * @param prop
     * @param decide_now
     */
    private void resume(int prop, Set<Clause> decide_now) {
        model.remove(prop);
        undecided.add(new Integer(Math.abs(prop)));
        for (Clause cl : decide_now) {
            cl.decided = 0;
        }
        decide_now.clear();
    }

    /**
     * Look for assigned value of prop in model set.
     * @param prop
     * @return
     *      -1 if value in model is different from prop
     *      1 if same
     *      0 if not present in model
     */
    private int truthValue(int prop) {
        if (model.contains(-prop)) {
            return -1;
        }
        if (model.contains(prop)) {
            //it should not be here, because we flag this by cl.decided = 1
            //it will skip this clause
            return 1;
        }
        return 0;
    }
    
    /**
     * find a clause only contains one undecided prop
     * @return
     *      the prop contained in that unit clause
     */
    private int unitClause() {
        for (Clause cl : s.clauses) {
            if (cl.decided == 1)
                continue;
            int unit_prop = 0;
            for (Integer prop : cl.clause) {
                int res = truthValue(prop);
                if (res == 0) {  //prop is undecided
                    if (unit_prop != 0) {   //already has an undecided one, try next clause
                        unit_prop = 0;
                        break;
                    } else {
                        unit_prop = prop;   //find one undecide prop
                    }
                } else if (res == 1) {      //clause is already true, try next one
                    unit_prop = 0;
                    break;
                }
            }
            if (unit_prop != 0) {
                return unit_prop;
            }
        }
        return 0;
    }
    
    
    /**
     * uniform prop is which present in the same form in all available clause
     * @return
     *      positive number if uniform prop is all in positive
     *      negative number if uniform prop is all in negation
     *      0 if can not find one
     */
    private int uniformProp() {
        for (Integer prop : undecided) {
            Set<Clause> cls_related = s.cl_idx.get(prop);
            Set<Clause> cls_m_related = s.cl_idx.get(-prop);
            int prev = 0;
            if (cls_related != null) {
                for (Clause cl : cls_related) {
                    if (cl.decided < 1) {
                        prev = prop;
                        break;
                    }
                }
            }
            if (cls_m_related != null) {
                for (Clause cl : cls_m_related) {
                    if (cl.decided < 1) {
                        if (prev != 0) {
                            prev = 0;
                            break;
                        } else {
                            prev = -prop;
                            break;
                        }
                    }
                }
            }
            if (prev != 0)
                return prev;
        }
        return 0;
        
        /*
        Set<Clause> cls_related;
        for (Integer prop : undecided) {
            cls_related = s.cl_idx.get(prop);           //only look for related clause, by index
            boolean uniform = false;
            int previous = 0; //record all previous form ( +x or -x )
            if (cls_related != null) {
                for (Clause c : cls_related) {   //for every clause
                    if (c.decided == 1) {
                        break;
                    }
                    Set<Integer> cl = c.clause;
                    for (Integer p_t : cl) {            //for every prop in the clause
                        if (Math.abs(p_t) == prop) {    //the same prop we want to looking for
                            if (previous == 0) {        //first one
                                uniform = true;
                                previous = p_t;         //record
                            } else {                    //not the first one, need to compare with previous one
                                if (previous != p_t) {  //not the same ,this prop is not uniform
                                    uniform = false;
                                    break;
                                } else {                //only one time should a prop present in a clause 
                                    break;
                                }
                            }
                        }
                    }
                    if (!uniform)   //is not uniform, just no use to continue scanning
                        break;
                }
            } else {
                uniform = false;
            }
            if (uniform) {
                return previous;
            }
        }
        return 0;*/
    }

    
    /**
     * Check the truth value of every clause
     * @return 
     *      1 if all clauses are true
     *      -1 if one of them is false
     *      0 can not determine
     */
    private int checkEveryClause() {
        int ret = 1;
        //TODO: will not have to check every clause, only check changed one!
        for (Clause c : s.clauses) {
            int cl_ret = -1;    //if all of the props in a clause is false, it is false
            if (c.decided == 1) {
                cl_ret = 1;
            } else {
                Set<Integer> clause = c.clause;
                for (Integer prop : clause) {
                    int prop_ret = checkProp(prop);
                    if (prop_ret == 1) {    //if disjunction contains one true, it is true
                        cl_ret = 1;
                        break;
                    } else if (prop_ret == 0) { //if contains one undetermined
                        cl_ret = 0;
                    }
                }
            }
            if (cl_ret == -1) { //one of the clauses is false
                return -1;
            } else if (cl_ret == 0) { //one of the clauses is undetermined
                ret = 0;
            }
        }
        return ret;
    }

    /**
     * Nearly the same as truthValue(), but check positive one first
     * @param prop
     * @return
     */
    private int checkProp(Integer prop) {
        if (model.contains(prop)) {
            return 1;
        }
        if (model.contains(-prop)) {
            return -1;
        }
        return 0;
    }
    
}
