package Algo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import Base.Node;
import Base.paramSDD_case;
import Base.route2;
import Base.trip;
import ilog.concert.*;
import ilog.cplex.*;
import util.Stopwatch;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;

//import edu.princeton.cs.algs4.Stopwatch;

public class Solve_dytsp2 {
    int numCity;
    ArrayList<Node> tnodes = new ArrayList<Node>();
    // double[][] distance;
    double[][] currentx;
    int maxrounds;
    List<List<Integer>> subtour;
    int[] optRoute;
    boolean opt;
    double checkdistance = 0;
    int mousecount = 0;
    int edgeH = 0;

    public double startime = 0;
    public double timeinter = 0;
    double input_startime;
    double obj;
    paramSDD_case userparam;
    boolean export = false;
    String prefix = "";

    public Solve_dytsp2() {

    }

    public Solve_dytsp2(boolean export, String prefix, paramSDD_case userparam, ArrayList<Node> snodes, int maxrounds, double startime) {
        this.maxrounds = maxrounds;
        this.tnodes = snodes;//include n+1
        this.numCity = snodes.size();//include depot include n+1
        this.optRoute = new int[numCity];
        this.userparam = userparam;
        this.input_startime = startime;
        this.edgeH = userparam.maxEH;

        this.export = export;
        this.prefix = prefix;

    }

    public void printNodes() {
        ArrayList<Integer> cus = new ArrayList<Integer>();
        for (Node e : this.tnodes) {
            cus.add(e.index);
        }
        System.out.println("Current Node to solve is " + cus.toString() + " MAX_rd= " + this.input_startime);
    }

    public void TeachingPaper_Solve() throws IloException {

        Stopwatch timer = new Stopwatch();
        double time = timer.elapsedTime();
        //System.out.println("Now the time                    = " + time);
        int variable_count = 0;
        int st_count = 0;
        int M = 1000000;

        IloCplex cplex = new IloCplex();
        IloIntVar[][][] x = new IloIntVar[numCity][numCity][edgeH];
        IloNumVar[] a = new IloNumVar[numCity];
        IloNumVar[][][] t = new IloNumVar[numCity][numCity][edgeH];

        //IloNumVar[] temp1 = new IloNumVar[numCity];
        //IloNumVar[] u = new IloNumVar[numCity];
        //IloNumVar Tdis = cplex.numVar(0, Double.MAX_VALUE,"Tdis");

        // ---build model---//
        IloLinearNumExpr exprObj = cplex.linearNumExpr();
        for (int i = 0; i < numCity; i++) {
            for (int j = 0; j < numCity; j++) {
                int c1 = tnodes.get(i).index;
                int c2 = tnodes.get(j).index;
                for (int h = 0; h < userparam.edge_Hs[c1][c2]; h++) {
                    if (i != j) {
                        x[i][j][h] = cplex.intVar(0, 1, "x" + i + "," + j + "," + h);
                        t[i][j][h] = cplex.numVar(0, Double.MAX_VALUE, "t" + i + "," + j + "," + h);
                    } else {
                        x[i][j][h] = cplex.intVar(0, 0, "x" + i + "," + j + "," + h);
                        t[i][j][h] = cplex.numVar(0, 0, "t" + i + "," + j + "," + h);
                    }
                    variable_count += 2;
                }
            }
        }


        for (int i = 0; i < numCity; i++) {
            a[i] = cplex.numVar(0, Double.MAX_VALUE, "a" + i);
        }

        exprObj.addTerm(1, a[numCity - 1]);
        cplex.addMinimize(exprObj);

        // ---constraint 1 --- ��0��ȥֻ��һ���� //
        IloLinearIntExpr con1 = cplex.linearIntExpr();
        for (int j = 1; j < numCity; j++) {//j in 0-(vc n+1)
            int c2 = tnodes.get(j).index;
            for (int h = 0; h < userparam.edge_Hs[0][c2]; h++) {
                con1.addTerm(x[0][j][h], 1);
                //con1_1.addTerm(x[0][j][k][c][h],1);
            }
        }
        cplex.addEq(con1, 1);
        st_count += 1;
        // ---constraint 2 ---����˴γ�����n+1�ĵ�ֻ��һ��//
        IloLinearIntExpr con2 = cplex.linearIntExpr();
        for (int i = 0; i < numCity - 1; i++) {//i in 0,vc
            int c1 = tnodes.get(i).index;
            for (int h = 0; h < userparam.edge_Hs[c1][userparam.nbclients + 1]; h++) {
                con2.addTerm(x[i][numCity - 1][h], 1);
            }
        }
        cplex.addEq(con2, 1);
        st_count += 1;
        // ---constraint 3 --- flow //
        for (int i = 1; i < numCity - 1; i++) {//i in vc
            IloLinearIntExpr con2_1 = cplex.linearIntExpr();
            IloLinearIntExpr con2_2 = cplex.linearIntExpr();
            for (int j = 1; j < numCity; j++) {//i-j vc.n+1
                int c1 = tnodes.get(i).index;
                int c2 = tnodes.get(j).index;
                for (int h = 0; h < userparam.edge_Hs[c1][c2]; h++) {//j={0}Ucustomer\{i} i-j j-i h��ͬ�ֿ�д
                    con2_1.addTerm(1, x[i][j][h]);
                }
            }
            for (int j = 0; j < numCity - 1; j++) {//j-i 0,vc
                int c1 = tnodes.get(i).index;
                int c2 = tnodes.get(j).index;
                for (int h = 0; h < userparam.edge_Hs[c2][c1]; h++) {//j={0}Ucustomer\{i} i-j j-i h��ͬ�ֿ�д
                    con2_2.addTerm(1, x[j][i][h]);
                }
            }

            cplex.addEq(con2_1, 1);
            st_count += 1;// c2
            cplex.addEq(con2_2, 1);
            st_count += 1;// c2
        }
        // ---constraint (4)  �ӵ�i depart ��ʱ�� û��nbclients+1 ---//
        for (int i = 0; i < numCity - 1; i++) {
            IloLinearNumExpr con4 = cplex.linearNumExpr();
            for (int j = 1; j < numCity; j++) {//j in vc n+1
                int c1 = tnodes.get(i).index;
                int c2 = tnodes.get(j).index;
                for (int h = 0; h < userparam.edge_Hs[c1][c2]; h++) {//
                    con4.addTerm(1, t[i][j][h]);
                }
            }
            con4.addTerm(-1, a[i]);
            cplex.addEq(con4, 0);
            st_count += 1;
        }
        //---constraint (5)
        double r1 = 0.0001;
        for (int i = 0; i < numCity - 1; i++) {//i in 0,vc
            for (int j = 1; j < numCity; j++) {// j in vc n+1
                int c1 = tnodes.get(i).index;
                int c2 = tnodes.get(j).index;
                for (int h = 0; h < userparam.edge_Hs[c1][c2]; h++) {
                    if (h < userparam.edge_Hs[c1][c2] - 1) {
                        IloLinearNumExpr con5_1 = cplex.linearNumExpr();
                        IloLinearNumExpr con5_2 = cplex.linearNumExpr();

                        con5_1.addTerm(1, t[i][j][h]);//TODO  ע��������ҿ��Ƿ�����Ҫ �ұ߼�ȥһ��ֵ
                        con5_1.addTerm(-1 * userparam.time_breakpoint[c1][c2].getLeft().get(h), x[i][j][h]);

                        con5_2.addTerm(-1, t[i][j][h]);
                        con5_2.addTerm(userparam.time_breakpoint[c1][c2].getRight().get(h) - r1, x[i][j][h]);
                        cplex.addGe(con5_1, 0);
                        cplex.addGe(con5_2, 0);
                    } else {
                        IloLinearNumExpr con5_1 = cplex.linearNumExpr();
                        IloLinearNumExpr con5_2 = cplex.linearNumExpr();

                        con5_1.addTerm(1, t[i][j][h]);//TODO  ע������ҿ��Ƿ�����Ҫ �ұ߼�ȥһ��ֵ
                        con5_1.addTerm(-1 * userparam.time_breakpoint[c1][c2].getLeft().get(h), x[i][j][h]);

                        con5_2.addTerm(-1, t[i][j][h]);
                        con5_2.addTerm(2 * userparam.time_horizon + userparam.time_breakpoint[c1][c2].getRight().get(h) - r1, x[i][j][h]);
                        cplex.addGe(con5_1, 0);
                        cplex.addGe(con5_2, 0);

                    }
                }
            }
        }
        //---constraint (6)  vehicle travel time ---//
        for (int i = 0; i < numCity - 1; i++) {// i in 0,vc
            for (int j = 1; j < numCity; j++) {// j in vc,n+1
                int c1 = tnodes.get(i).index;
                int c2 = tnodes.get(j).index;
                for (int h = 0; h < userparam.edge_Hs[c1][c2]; h++) {
                    IloLinearNumExpr con6 = cplex.linearNumExpr();
                    if (i != j && i >= 0 && j >= 1) {

                        con6.addTerm(1, a[j]);
                        con6.addTerm(-1 * (1 + userparam.time_breakpoint[c1][c2].getSlope().get(h)),
                                t[i][j][h]);
                        con6.addTerm(-(M + userparam.time_breakpoint[c1][c2].getIntercept().get(h)),
                                x[i][j][h]);
                        cplex.addGe(con6, -1 * M);
                        st_count += 1;
                    }
                }
            }
        }
        // ---constraint (7) vehicle return to depot ---//

        IloLinearNumExpr con7 = cplex.linearNumExpr();
        for (int i = 0; i < numCity - 1; i++) {
            int c1 = tnodes.get(i).index;
            for (int h = 0; h < userparam.edge_Hs[c1][userparam.nbclients + 1]; h++) {
                con7.addTerm((1 + userparam.time_breakpoint[c1][userparam.nbclients + 1].getSlope().get(h)),
                        t[i][numCity - 1][h]);
                con7.addTerm(userparam.time_breakpoint[c1][userparam.nbclients + 1].getIntercept().get(h),
                        x[i][numCity - 1][h]);
            }
        }
        con7.addTerm(-1, a[numCity - 1]);
        cplex.addEq(con7, 0);


        //initial a[i]
        IloLinearNumExpr con8 = cplex.linearNumExpr();
        con8.addTerm(1, a[0]);
        cplex.addEq(con8, this.input_startime);


        int k = 1;
        opt = false;

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-HH-mm");
        Date date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date));
        if (export) {
            cplex.exportModel("./dataset/" + prefix + "/cplexResult/cplex_model/dytsp_model" + formatter.format(date) + ".lp");
        }

        //cplex.setParam(IloCplex.Param.TimeLimit, 60);
        cplex.setParam(IloCplex.Param.TimeLimit, 120);

        System.out.println("Finish build dynamic tsp model ");
        System.out.println("variable count= " + variable_count);
        System.out.println("constraint count= " + st_count);

        boolean feasible = cplex.solve();
        obj = cplex.getObjValue();

        if (feasible) {
            currentx = new double[numCity][numCity];
            for (int i = 0; i < numCity; i++) {
                for (int j = 0; j < numCity; j++) {
                    double tmp = 0;
                    int c1 = tnodes.get(i).index;
                    int c2 = tnodes.get(j).index;
                    for (int h = 0; h < userparam.edge_Hs[c1][c2]; h++) {
                        if (i != j && j != 0) {
                            tmp += cplex.getValue(x[i][j][h]);
                        }
                    }

                    currentx[i][j] = tmp;
                    if (currentx[i][j] > 0.5) {
                        //System.err.println("x_i "+i+" j "+j+" = "+currentx[i][j]);
                    }
                }
            }

            for (int i = 0; i < numCity; i++) {
                // System.err.println(a[i].getName()+" = "+cplex.getValue(a[i]));
            }
            // part1: find all the subtours from currentx
            subtour = findSubtour1(currentx, numCity);

            //System.err.println("There are "+subtour.size()+" subtours "+subtour.get(0).toString());
            // part2: add the subtour constraints
            if (subtour.size() == 1) {
                opt = true;
                for (int i = 0; i < numCity; i++) {//TODO
                    optRoute[i] = subtour.get(0).get(i);
                }
                System.out.println("The model is optimal now!");
            } else {
                System.out.println("The  model is unfesible and it may be wrongly built up!");
            }
        }


        System.out.println("The optimal objValue is " + cplex.getObjValue());
        ArrayList<Integer> tmp_customer = new ArrayList<Integer>();
        tmp_customer = this.printsol();
        System.out.println("The optimal route is " + tmp_customer.toString());

    }

    public ArrayList<Integer> printsol() {
        ArrayList<Integer> tmp_customer = new ArrayList<Integer>();
        for (int i = 0; i < this.optRoute.length; i++) {
            int c = this.optRoute[i];
            //System.err.println(c+","+this.tnodes.get(c).index);
            tmp_customer.add(this.tnodes.get(c).index);
        }
        return tmp_customer;
    }

    public route2 setRoute() {
        route2 a = new route2();
        ArrayList<Integer> tmp_customer = new ArrayList<Integer>();
        for (int i = 0; i < this.optRoute.length; i++) {
            int c = this.optRoute[i];
            tmp_customer.add(this.tnodes.get(c).index);
        }
        //System.err.println("This initial nodes_solution: "+tmp_customer.toString());
        a.setconsumer(tmp_customer);//TODO
        a.set_completetime(this.obj);
        a.set_departime(this.input_startime);
        a.set_traveltime(this.obj - this.input_startime);
        a.set_tsptime(this.obj - this.input_startime);
        a.cal_largest_releasetime(this.userparam);
        return a;
    }

    public trip setTrip() {
        trip a = new trip(userparam);
        ArrayList<Integer> tmp_customer = new ArrayList<Integer>();
        for (int i = 0; i < this.optRoute.length; i++) {
            int c = this.optRoute[i];
            tmp_customer.add(this.tnodes.get(c).index);
        }
        //System.err.println("This initial nodes_solution: "+tmp_customer.toString());
        a.setconsumer(tmp_customer);//TODO
        a.set_completetime(this.obj);
        a.set_departime(this.input_startime);
        a.set_traveltime(this.obj - this.input_startime);
        a.set_tsptime(this.obj - this.input_startime);
        a.cal_largest_releasetime();
        return a;
    }
    /*  public void check() {
		// TODO Auto-generated method stub
    	int pre=0;
    	for (int i = 1; i < optRoute.length; i++) {
    		this.checkdistance+=this.distance[optRoute[pre]][optRoute[i]];
    		pre=i;
    	}
    	this.checkdistance+=this.distance[optRoute[pre]][optRoute[0]];
    	System.out.println("CHECk The optimal route distance is "+this.checkdistance);

	}*/

    public class MouseHandler extends MouseAdapter {
        public void mousePressed(MouseEvent event) {
            if (event.isMetaDown()) {
                mousecount = Math.max(0, mousecount - 1);
            } else {
                mousecount = Math.min(numCity, mousecount + 1);
            }

        }
    }


    public List<List<Integer>> findSubtour(double[][] x) {

        List<List<Integer>> record = new ArrayList<>();
        List<Integer> list = new ArrayList<Integer>();

        boolean[] chosen = new boolean[numCity];
        int currentCity = 0;
        int count = 1;
        chosen[0] = true;
        list.add(0);

        while (count < numCity) {
            int next = -1;
            for (int i = 0; i < numCity; i++) {
                if (x[currentCity][i] > 0.5) {
                    next = i;
                    break;
                }
            }

            if (next == list.get(0)) {
                record.add(list);
                list = new ArrayList<Integer>();

                for (int i = 0; i < numCity; i++) {
                    if (!chosen[i]) {
                        currentCity = i;
                        count++;
                        chosen[i] = true;
                        list.add(i);
                        break;
                    }
                }
            } else {
                chosen[next] = true;
                count++;
                list.add(next);
                currentCity = next;
            }
        }

        record.add(list);

        return record;

    }

    public List<List<Integer>> findSubtour1(double[][] x, int N) {
        HashSet<Integer> set1 = new HashSet<Integer>();
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                if (greater(x[i][j], 0.5)) {
                    set1.add(i);
                    set1.add(j);
                }
            }
        }
        boolean[] chosen = new boolean[N];
        for (int i = 0; i < N; i++) {
            chosen[i] = true;
        }
        for (Integer e : set1) {
            chosen[e] = false;
        }

        List<List<Integer>> record = new ArrayList<>();
        List<Integer> list = new ArrayList<Integer>();
        int count = 1;
        int currentCity = 0;

        //for (Integer i:set1) {
        //for (Integer j :set1) {
        // if (greater(x[i][j] ,0.5)  && !chosen[i]) {
        //	currentCity = i;
        //  break;}}}

        //System.err.println(currentCity);
        if (currentCity == -1) {
            return record;
        }

        chosen[currentCity] = true;
        list.add(currentCity);

        while (count < set1.size()) {//TODO

            int next = -1;
            for (int i = 0; i < N; i++) {
                if (greater(x[currentCity][i], 0.5)) {
                    next = i;
                    break;
                }
            }

            //System.err.println("Count: "+count+"Cur: "+currentCity+" Next: "+next);

            if (next == N - 1) {
                chosen[next] = true;
                count++;
                //System.err.println(list.toString());
                list.add(N - 1);
                record.add(list);
                list = new ArrayList<Integer>();

                for (Integer i : set1) {
                    for (Integer j : set1) {
                        if (greater(x[i][j], 0.5) && !chosen[i]) {
                            currentCity = i;
                            count++;
                            //System.err.println("Count: "+count+"Cur: "+currentCity+" Next: "+next);
                            chosen[i] = true;
                            list.add(i);
                            break;
                        }
                    }
                }

            } else {
                chosen[next] = true;
                count++;
                list.add(next);
                currentCity = next;
            }
        }

        //record.add(list);
        //System.err.println(list.toString());

        return record;

    }

    public boolean greater(double a, double b) {
        if (a - b > 1E-6) {
            return true;
        } else {
            return false;
        }
    }


}



