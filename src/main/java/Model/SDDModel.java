package Model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import Base.Node;
import Base.paramSDD_case;
import Base.route2;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import util.Stopwatch;

public class SDDModel {

    FileWriter fw1;
    BufferedWriter bw1;
    HashMap<ArrayList<Integer>, ArrayList<Integer>> kroutes = new HashMap<ArrayList<Integer>, ArrayList<Integer>>();
    HashMap<Integer, ArrayList<ArrayList<Integer>>> kroutes1 = new HashMap<Integer, ArrayList<ArrayList<Integer>>>();
    HashMap<Integer, ArrayList<route2>> v1routes = new HashMap<Integer, ArrayList<route2>>();

    double obj = 0;
    double violation_con = 0;
    double finalcompletetime = 0;
    int cdis = 0;
    int ccon = 0;

    paramSDD_case userparam;
    String prefix;
    int numCity;
    int vehK;
    int rouC = 1;
    int edgeH = 0;

    public SDDModel(String result_file, String prefix, paramSDD_case userparam, int cdis, int ccon) throws IOException {
        fw1 = new FileWriter(makefile(result_file), false);
        bw1 = new BufferedWriter(fw1);
        this.userparam = userparam;
        this.cdis = cdis;
        this.ccon = ccon;
        this.prefix = prefix;
    }

    public void build_mincomplete_model(List<Integer> cnode, int cc, boolean export,
                                        String exportname, boolean isrd, boolean isduration, double duration, double Q,
                                        boolean istw, boolean isload, boolean isQ, boolean isa, boolean isb, double timelimit,
                                        boolean isminc, boolean ismintcost, boolean ismindcost, int[][] help,
                                        boolean inforce_helpvalue, boolean inforce_quantity, int[][] z1value) throws IloException, IOException {
        Stopwatch timer = new Stopwatch();
        double time = timer.elapsedTime();
        System.out.println("Now the time                    = " + time);
        bw1.write("\n Now the time                    = " + time);
        bw1.flush();

        cnode.add(userparam.nbclients + 1);//TODO

        int N = cnode.size();
        int K = userparam.Nk;

        Node depot = userparam.depots.get(0);
        System.err.println("N_depot+customer+artifical_depot= " + N + " Vehicle_num= " + K + " c= " + cnode.toString());
        int cnti = 0;
        for (List<Integer> t : userparam.nodeset) {
            bw1.write("\n Vehicle " + cnti + " contains " + t.size() + " customernodes:");
            bw1.flush();
            bw1.write(t.toString());
            bw1.flush();
            cnti += 1;
        }
        numCity = N;
        vehK = K;
        edgeH = userparam.maxEH;
        System.err.println("vek: " + vehK);

        ArrayList<Integer> rouCs = new ArrayList<Integer>();
        for (int i = 0; i < vehK; i++) {
            int c = cc;
            bw1.write("\n The vehicle " + i + " most contains " + c + " routes ");
            rouCs.add(c);
            rouC = Math.max(rouC, c);
        }
        System.err.println("rouC: " + rouC + " ");
        int[][] edge_Hs = userparam.edge_Hs;

        int variable_count = 0;
        int st_count = 0;
        int M = 2 * userparam.DDL;

        IloCplex cplex = new IloCplex();
        IloIntVar[][][][][] x = new IloIntVar[numCity][numCity][vehK][rouC][edgeH];
        IloIntVar[][][] y = new IloIntVar[numCity][vehK][rouC];

        IloNumVar[][][] a = new IloNumVar[numCity][vehK][rouC];
        IloNumVar[][][][][] t = new IloNumVar[numCity][numCity][vehK][rouC][edgeH];
        IloIntVar[][] z = new IloIntVar[vehK][vehK];
        IloIntVar[][] z1 = new IloIntVar[vehK][vehK];

        IloNumVar[][] travel_cost = new IloNumVar[vehK][rouC];
        IloNumVar completion = cplex.numVar(0, Double.MAX_VALUE, "completion");
        IloNumVar tcost = cplex.numVar(0, Double.MAX_VALUE, "travel_time_cost");
        IloIntVar[] fulliment = new IloIntVar[numCity];

        variable_count += 1;

        // ---build model---//
        IloLinearNumExpr exprObj = cplex.linearNumExpr();
        for (int i = 0; i < numCity; i++) {
            for (int j = 0; j < numCity; j++) {
                int c1 = userparam.nodes.get(cnode.get(i)).index;
                int c2 = userparam.nodes.get(cnode.get(j)).index;
                for (int k = 0; k < vehK; k++) {
                    for (int c = 0; c < rouCs.get(k); c++) {
                        for (int h = 0; h < edge_Hs[c1][c2]; h++) {
                            if (i != j) {
                                x[i][j][k][c][h] = cplex.intVar(0, 1, "x" + i + "," + j + "," + k + "," + c + "," + h);
                                t[i][j][k][c][h] = cplex.numVar(0, Double.MAX_VALUE, "t" + i + "," + j + "," + k + "," + c + "," + h);
                            } else {
                                x[i][j][k][c][h] = cplex.intVar(0, 0, "x" + i + "," + j + "," + k + "," + c + "," + h);
                                t[i][j][k][c][h] = cplex.numVar(0, 0, "t" + i + "," + j + "," + k + "," + c + "," + h);
                            }
                            variable_count += 2;
                        }
                    }
                }
            }
        }
        //assert false;

        for (int k1 = 0; k1 < vehK; k1++) {
            for (int k2 = 0; k2 < vehK; k2++) {
                if (k1 != k2) {
                    z[k1][k2] = cplex.intVar(0, 1, "z" + k1 + "," + k2);
                    z1[k1][k2] = cplex.intVar(0, Integer.MAX_VALUE, "q" + k1 + "," + k2);
                    exprObj.addTerm(ccon, z1[k1][k2]);
                } else {
                    z[k1][k2] = cplex.intVar(0, 0, "z" + k1 + "," + k2);
                    z1[k1][k2] = cplex.intVar(0, 0, "q" + k1 + "," + k2);
                }
                variable_count += 2;
            }
        }//2region

        for (int i = 0; i < numCity; i++) {
            for (int k = 0; k < vehK; k++) {
                for (int c = 0; c < rouCs.get(k); c++) {
                    y[i][k][c] = cplex.intVar(0, 1, "y" + i + "," + k + "," + c);
                    a[i][k][c] = cplex.numVar(0, Double.MAX_VALUE, "a" + i + "," + k + "," + c);
                    variable_count += 2;
                }
            }
        }

        for (int k = 0; k < vehK; k++) {
            for (int c = 0; c < rouCs.get(k); c++) {
                travel_cost[k][c] = cplex.numVar(0, Double.MAX_VALUE, "travel_cost" + k + "," + c);
                variable_count += 1;
            }
        }

        for (int i = 0; i < numCity; i++) {
            fulliment[i] = cplex.intVar(0, 1, "H" + i);
        }

        for (int i = 1; i < numCity - 1; i++) {
            exprObj.addTerm(1, fulliment[i]);
        }
        cplex.addMaximize(exprObj);

        if (ismindcost) {
            for (int i = 0; i < numCity - 1; i++) {//i in 0,vc
                for (int j = 1; j < numCity; j++) {//j vc.n+1
                    int c1 = userparam.nodes.get(cnode.get(i)).index;
                    int c2 = userparam.nodes.get(cnode.get(j)).index;
                    for (int k = 0; k < vehK; k++) {
                        for (int c = 0; c < rouCs.get(k); c++) {
                            for (int h = 0; h < edge_Hs[c1][c2]; h++) {
                                exprObj.addTerm(userparam.distBase[i][j], x[i][j][k][c][h]);
                            }
                        }
                    }
                }
            }
        }

        if (ismintcost) {//objective
            for (int k = 0; k < vehK; k++) {
                for (int c = 0; c < rouCs.get(k); c++) {
                    IloLinearIntExpr con1 = cplex.linearIntExpr();
                    IloLinearNumExpr con2 = cplex.linearNumExpr();

                    con2.addTerm(1, a[numCity - 1][k][c]);
                    con2.addTerm(-1, a[0][k][c]);
                    con2.addTerm(-1, travel_cost[k][c]);
                    cplex.addEq(con2, 0);
                    exprObj.addTerm(1, travel_cost[k][c]);
                }
            }
        }

        if (isminc) {
            exprObj.addTerm(cdis, completion);
        }
        //cplex.addMinimize(exprObj);

        double cur_sct = 0;
        int idxj = -1;
        ArrayList<Double> tmp_sct = new ArrayList<Double>();

        for (int i = 1; i < numCity - 1; i++) {
            IloLinearNumExpr con1 = cplex.linearNumExpr();
            for (int k = 0; k < vehK; k++) {
                for (int c = 0; c < rouCs.get(k); c++) {
                    con1.addTerm(a[i][k][c], 1);
                }
            }
            con1.addTerm(M, fulliment[i]);
            cplex.addLe(con1, M + userparam.B);
        }

        // ---constraint 1  nbclients+1//
        for (int k = 0; k < vehK; k++) {
            for (int c = 0; c < rouCs.get(k); c++) {
                IloLinearIntExpr con1 = cplex.linearIntExpr();
                IloLinearIntExpr con1_1 = cplex.linearIntExpr();
                for (int j = 1; j < numCity; j++) {//j in 0-(vc n+1)
                    int c2 = userparam.nodes.get(cnode.get(j)).index;
                    for (int h = 0; h < edge_Hs[0][c2]; h++) {
                        con1.addTerm(x[0][j][k][c][h], 1);
                        //con1_1.addTerm(x[0][j][k][c][h],1);
                    }
                }
                con1.addTerm(y[0][k][c], -1);
                con1_1.addTerm(y[0][k][c], 1);
                cplex.addEq(con1, 0);
                st_count += 1;
                cplex.addEq(con1_1, 1);
                st_count += 1;
            }
        }
        // ---constraint 1//
        for (int k = 0; k < vehK; k++) {
            for (int c = 0; c < rouCs.get(k); c++) {
                IloLinearIntExpr con1 = cplex.linearIntExpr();
                IloLinearIntExpr con1_1 = cplex.linearIntExpr();
                for (int i = 0; i < numCity - 1; i++) {//i in 0,vc
                    //int c1=userparam.nodes.get(cnode.get(i)).index;
                    int c1 = userparam.nodes.get(cnode.get(i)).index;
                    for (int h = 0; h < edge_Hs[c1][userparam.nbclients + 1]; h++) {
                        con1.addTerm(x[i][numCity - 1][k][c][h], 1);
                        //con1_1.addTerm(x[0][j][k][c][h],1);
                    }
                }
                con1.addTerm(y[numCity - 1][k][c], -1);
                con1_1.addTerm(y[numCity - 1][k][c], 1);
                cplex.addEq(con1, 0);
                st_count += 1;
                cplex.addEq(con1_1, 1);
                st_count += 1;
            }
        }
        // ---constraint 2 --- flow //
        for (int i = 1; i < numCity - 1; i++) {//i in vc
            for (int k = 0; k < vehK; k++) {
                for (int c = 0; c < rouCs.get(k); c++) {
                    IloLinearIntExpr con2_1 = cplex.linearIntExpr();
                    IloLinearIntExpr con2_2 = cplex.linearIntExpr();
                    for (int j = 1; j < numCity; j++) {//i-j vc.n+1
                        int c1 = userparam.nodes.get(cnode.get(i)).index;
                        int c2 = userparam.nodes.get(cnode.get(j)).index;
                        for (int h = 0; h < edge_Hs[c1][c2]; h++) {//j={0}Ucustomer\{i} i-j j-i
                            con2_1.addTerm(1, x[i][j][k][c][h]);
                            //con2_2.addTerm(1, x[j][i][k][c][h]);
                        }
                    }
                    for (int j = 0; j < numCity - 1; j++) {//j-i 0,vc
                        int c1 = userparam.nodes.get(cnode.get(i)).index;
                        int c2 = userparam.nodes.get(cnode.get(j)).index;
                        for (int h = 0; h < edge_Hs[c2][c1]; h++) {//j={0}Ucustomer\{i} i-j j-i
                            con2_2.addTerm(1, x[j][i][k][c][h]);
                        }
                    }

                    con2_1.addTerm(-1, y[i][k][c]);
                    con2_2.addTerm(-1, y[i][k][c]);
                    cplex.addEq(con2_1, 0);
                    st_count += 1;// c2
                    //System.err.println("i: "+i+"con2_2: "+con2_2);
                    cplex.addEq(con2_2, 0);
                    st_count += 1;// c2
                }
            }
        }

        //---constraint 3 //
        for (int i = 1; i < numCity - 1; i++) {//i=1 start from customer
            IloLinearIntExpr con2 = cplex.linearIntExpr();
            for (int k = 0; k < vehK; k++) {
                for (int c = 0; c < rouCs.get(k); c++) {
                    con2.addTerm(y[i][k][c], 1);
                }
            }
            cplex.addEq(con2, 1);
            st_count += 1;//c2
        }

        // ---constraint (4) vehicle k trip r �ӵ�i depart ��ʱ�� û��nbclients+1 ---//
        for (int k = 0; k < vehK; k++) {
            for (int c = 0; c < rouCs.get(k); c++) {
                for (int i = 0; i < numCity - 1; i++) {
                    IloLinearNumExpr con4 = cplex.linearNumExpr();
                    for (int j = 1; j < numCity; j++) {//j in vc n+1
                        int c1 = userparam.nodes.get(cnode.get(i)).index;
                        int c2 = userparam.nodes.get(cnode.get(j)).index;
                        for (int h = 0; h < edge_Hs[c1][c2]; h++) {//
                            con4.addTerm(1, t[i][j][k][c][h]);
                        }
                    }
                    con4.addTerm(-1, a[i][k][c]);
                    cplex.addEq(con4, 0);
                    st_count += 1;
                }
            }
        }

        //---constraint (5)
        double r1 = 0.0;
        for (int i = 0; i < numCity - 1; i++) {//i in 0,vc
            for (int j = 1; j < numCity; j++) {// j in vc n+1
                int c1 = userparam.nodes.get(cnode.get(i)).index;
                int c2 = userparam.nodes.get(cnode.get(j)).index;
                for (int k = 0; k < vehK; k++) {
                    for (int c = 0; c < rouCs.get(k); c++) {
                        for (int h = 0; h < edge_Hs[c1][c2]; h++) {
                            IloLinearNumExpr con5_1 = cplex.linearNumExpr();
                            IloLinearNumExpr con5_2 = cplex.linearNumExpr();
                            //int c1=userparam.nodes.get(cnode.get(i)).index;
                            //int c2=userparam.nodes.get(cnode.get(j)).index;
                            con5_1.addTerm(1, t[i][j][k][c][h]);//TODO
                            con5_1.addTerm(-1 * userparam.time_breakpoint[c1][c2].getLeft().get(h), x[i][j][k][c][h]);
                            cplex.addGe(con5_1, 0);
                            con5_2.addTerm(-1, t[i][j][k][c][h]);
                            con5_2.addTerm(userparam.time_breakpoint[c1][c2].getRight().get(h) - r1, x[i][j][k][c][h]);
                            cplex.addGe(con5_2, 0);


                        }
                    }
                }
            }
        }
        //---constraint (6)  vehicle travel time ---//
        for (int i = 0; i < numCity - 1; i++) {// i in 0,vc
            for (int j = 1; j < numCity; j++) {// j in vc,n+1
                int c1 = userparam.nodes.get(cnode.get(i)).index;
                int c2 = userparam.nodes.get(cnode.get(j)).index;
                for (int k = 0; k < vehK; k++) {
                    for (int c = 0; c < rouCs.get(k); c++) {
                        for (int h = 0; h < edge_Hs[c1][c2]; h++) {
                            IloLinearNumExpr con6 = cplex.linearNumExpr();
                            if (i != j && i >= 0 && j >= 1) {
                                //int c1=userparam.nodes.get(cnode.get(i)).index;
                                //int c2=userparam.nodes.get(cnode.get(j)).index;
                                con6.addTerm(1, a[j][k][c]);
                                con6.addTerm(-1 * (1 + userparam.time_breakpoint[c1][c2].getSlope().get(h)),
                                        t[i][j][k][c][h]);
                                con6.addTerm(-(M + userparam.time_breakpoint[c1][c2].getIntercept().get(h)),
                                        x[i][j][k][c][h]);
                                cplex.addGe(con6, -1 * M + userparam.s[c2]);
                                st_count += 1;
                                //System.err.println(userparam.s[c2]+","+(userparam.s[c2]-M));
                            }
                        }
                    }
                }
            }
        }

        // ---constraint (7) vehicle return to depot ---//
        for (int k = 0; k < vehK; k++) {
            for (int c = 0; c < rouCs.get(k); c++) {
                IloLinearNumExpr con7 = cplex.linearNumExpr();
                for (int i = 0; i < numCity - 1; i++) {
                    int c1 = userparam.nodes.get(cnode.get(i)).index;
                    //int c2=userparam.nodes.get(cnode.get(j)).index;
                    for (int h = 0; h < edge_Hs[c1][userparam.nbclients + 1]; h++) {
                        //int c1=userparam.nodes.get(cnode.get(i)).index;
                        con7.addTerm((1 + userparam.time_breakpoint[c1][userparam.nbclients + 1].getSlope().get(h)),
                                t[i][numCity - 1][k][c][h]);
                        con7.addTerm(userparam.time_breakpoint[c1][userparam.nbclients + 1].getIntercept().get(h),
                                x[i][numCity - 1][k][c][h]);
                    }
                }
                con7.addTerm(-1, a[numCity - 1][k][c]);
                cplex.addEq(con7, 0);
            }
        }

        // ---constraint(10)  ---//
        for (int k = 0; k < vehK; k++) {
            for (int c = 0; c < rouCs.get(k) - 1; c++) {//
                IloLinearNumExpr con10 = cplex.linearNumExpr();
                con10.addTerm(1, a[0][k][c + 1]);//c+1 start time
                con10.addTerm(-1, a[numCity - 1][k][c]);//c endtime
                cplex.addGe(con10, 0);
                st_count += 1;
            }
        }

        for (int k = 0; k < vehK; k++) {
            for (int c = 0; c < rouCs.get(k) - 1; c++) {
                IloLinearNumExpr con10 = cplex.linearNumExpr();
                con10.addTerm(1, a[numCity - 1][k][c + 1]);//c+1 end time
                con10.addTerm(-1, a[numCity - 1][k][c]);//c end time
                cplex.addGe(con10, 0);
                st_count += 1;
            }
        }

        // ---constraint (8) start time > release date ---//
        if (isrd) {
            for (int k = 0; k < vehK; k++) {
                for (int c = 0; c < rouCs.get(k); c++) {
                    for (int i = 1; i < numCity; i++) {//i=1 start from customer
                        IloLinearNumExpr con8 = cplex.linearNumExpr();
                        int c1 = userparam.nodes.get(cnode.get(i)).index;
                        con8.addTerm(-1 * userparam.releasetime.get(c1), y[i][k][c]);
                        con8.addTerm(1, a[0][k][c]);
                        cplex.addGe(con8, 0);
                        st_count += 1;
                    }
                }
            }
        }
        // ---constraint (8) start time > sum(load_time)---//
        if (isduration) {//duration
            for (int k = 0; k < vehK; k++) {
                for (int c = 0; c < rouCs.get(k); c++) {

                    IloLinearNumExpr con8 = cplex.linearNumExpr();
                    con8.addTerm(-1, a[0][k][c]);
                    con8.addTerm(1, a[numCity - 1][k][c]);
                    cplex.addLe(con8, duration);
                    st_count += 1;
                }
            }
        }
        // ---constraint(9)  end time < DDL  ---//
       /* for (int k = 0; k < vehK; k++) {
            for (int c = 0; c < rouCs.get(k); c++) {
                //if(rouCs.get(k)>=1) {
                IloLinearNumExpr con9 = cplex.linearNumExpr();
                con9.addTerm(1, a[numCity - 1][k][c]);
                cplex.addLe(con9, this.userparam.DDL);//DDL
                st_count += 1;//}
            }
        }*/
        // ---constraint(9)  end time < Service Time SCT  ---//

       /* for (int k = 0; k < vehK; k++) {
            for (int c = 0; c < rouCs.get(k); c++) {
                for (int i = 0; i < numCity - 1; i++) {//i=1 start from customer
                    int c1 = userparam.nodes.get(cnode.get(i)).index;
                    //System.err.println("c1="+c1);
                    //IloLinearNumExpr con9 = cplex.linearNumExpr();
                    IloLinearNumExpr con10 = cplex.linearNumExpr();
                    //con9.addTerm(1, a[i][k][c]);
                    //cplex.addLe(con9, this.userparam.SCT);//SCT
                    con10.addTerm(1, completion);
                    con10.addTerm(-1, a[i][k][c]);//这里除去了回来的点
                    cplex.addGe(con10, 0);//completion_time
                    st_count += 2;
                }
            }
        }*/

        // ---constraint(12) time window ---//
        if (istw) {
            for (int k = 0; k < vehK; k++) {//a,b timewindow
                for (int c = 0; c < rouCs.get(k); c++) {
                    for (int i = 0; i < numCity - 1; i++) {
                        int c1 = userparam.nodes.get(cnode.get(i)).index;
                        if (isa) {
                            IloLinearNumExpr con12 = cplex.linearNumExpr();
                            con12.addTerm(1, a[i][k][c]);
                            con12.addTerm(-(userparam.a[c1] + userparam.s[c1]), y[i][k][c]);
                            cplex.addGe(con12, 0);
                            st_count += 1;
                        }
                        if (isb) {
                            IloLinearNumExpr con12_1 = cplex.linearNumExpr();
                            con12_1.addTerm(1, a[i][k][c]);
                            con12_1.addTerm(-(userparam.b[c1] + userparam.s[c1]), y[i][k][c]);
                            cplex.addLe(con12_1, 0);
                            st_count += 1;
                        }
                    }
                }
            }
        }
        // ---constraint(13) load time window ---//
        if (isload) {
            for (int k = 0; k < vehK; k++) {//load==0
                IloLinearNumExpr con12 = cplex.linearNumExpr();
                for (int i = 0; i < numCity - 1; i++) {
                    int c1 = userparam.nodes.get(cnode.get(i)).index;
                    con12.addTerm(-1 * userparam.load[c1], y[i][k][0]);
                }
                con12.addTerm(1, a[0][k][0]);
                cplex.addGe(con12, 0);
                st_count += 1;
            }

            for (int k = 0; k < vehK; k++) {//load==0
                for (int c = 1; c < rouCs.get(k); c++) {
                    IloLinearNumExpr con12 = cplex.linearNumExpr();
                    for (int i = 0; i < numCity - 1; i++) {
                        int c1 = userparam.nodes.get(cnode.get(i)).index;
                        con12.addTerm(-1 * userparam.load[c1], y[i][k][c]);
                    }
                    con12.addTerm(1, a[0][k][c]);
                    con12.addTerm(-1, a[numCity - 1][k][c - 1]);
                    cplex.addGe(con12, 0);
                    st_count += 1;

                }
            }
        }
        //capacity demand=0
        if (isQ) {
            for (int k = 0; k < vehK; k++) {
                for (int c = 0; c < rouCs.get(k); c++) {
                    IloLinearNumExpr con12 = cplex.linearNumExpr();
                    for (int i = 0; i < numCity - 1; i++) {
                        int c1 = userparam.nodes.get(cnode.get(i)).index;
                        con12.addTerm(userparam.d[c1], y[i][k][c]);
                    }
                    cplex.addLe(con12, Q);
                    st_count += 1;

                }
            }
        }


        // ---constraint vehicle travel route consistency ---//
        //z >= y
        for (int k1 = 0; k1 < vehK; k1++) {
            for (int k2 = 0; k2 < vehK; k2++) {
                if (k1 != k2) {
                    for (int c = 0; c < rouCs.get(k1); c++) {
                        for (int i = 1; i < numCity - 1; i++) {//i k2
                            if (userparam.nodeset.get(k2).contains(i)) {
                                IloLinearNumExpr con12 = cplex.linearNumExpr();
                                con12.addTerm(-1, y[i][k1][c]);
                                con12.addTerm(1, z[k1][k2]);
                                cplex.addGe(con12, 0);
                                st_count += 1;
                            }
                        }
                    }
                }
            }
        }
        if (inforce_quantity) {
            for (int k1 = 0; k1 < vehK; k1++) {
                for (int k2 = 0; k2 < vehK; k2++) {
                    if (k1 != k2) {
                        IloLinearNumExpr con12 = cplex.linearNumExpr();
                        con12.addTerm(1, z1[k1][k2]);
                        for (int c = 0; c < rouCs.get(k1); c++) {
                            for (int i = 1; i < numCity - 1; i++) {//i k2
                                if (userparam.nodeset.get(k2).contains(i)) {
                                    con12.addTerm(-1, y[i][k1][c]);
                                }
                            }
                        }
                        cplex.addEq(con12, 0);
                        st_count += 1;
                    }
                }
            }
            for (int k1 = 0; k1 < vehK; k1++) {
                for (int k2 = 0; k2 < vehK; k2++) {
                    if (k1 != k2) {
                        IloLinearNumExpr con14 = cplex.linearNumExpr();
                        con14.addTerm(1, z1[k1][k2]);
                        //con13.addTerm(1, z[k2][k1]);
                        cplex.addEq(con14, z1value[k1][k2]);
                        st_count += 1;
                    }
                }
            }//
        }

        for (int k1 = 0; k1 < vehK; k1++) {
            for (int k2 = 0; k2 < vehK; k2++) {
                if (k1 != k2) {
                    IloLinearNumExpr con13 = cplex.linearNumExpr();
                    con13.addTerm(1, z[k1][k2]);
                    con13.addTerm(1, z[k2][k1]);
                    cplex.addLe(con13, 1);
                    //System.err.println("con13: " + con13);
                    st_count += 1;
                }
            }
        }
        if (inforce_helpvalue) {
            for (int k1 = 0; k1 < vehK; k1++) {
                for (int k2 = 0; k2 < vehK; k2++) {
                    if (k1 != k2) {
                        IloLinearNumExpr con14 = cplex.linearNumExpr();
                        con14.addTerm(1, z[k1][k2]);
                        //con13.addTerm(1, z[k2][k1]);
                        cplex.addEq(con14, help[k1][k2]);
                        st_count += 1;
                    }
                }
            }
        }
        // ---constraint speedup //
        for (int k = 0; k < vehK; k++) {
            for (int c = 0; c < rouCs.get(k); c++) {
                for (int i = 1; i < numCity - 1; i++) {//i=1 start from customer
                    IloLinearNumExpr con14 = cplex.linearNumExpr();
                    int c1 = userparam.nodes.get(cnode.get(i)).index;
                    double minttime = 0;
                    double mindis = 100000;

                    mindis = userparam.distBase[0][c1];
                    minttime = mindis / userparam.maxspeed;
                    con14.addTerm(-1 * (userparam.releasetime.get(c1) + minttime), y[i][k][c]);
                    con14.addTerm(1, a[i][k][c]);
                    cplex.addGe(con14, 0);
                    st_count += 1;
                }
            }
        }

        for (int k = 0; k < vehK; k++) {
            for (int c = 0; c < rouCs.get(k); c++) {
                for (int i = 1; i < numCity - 1; i++) {//i=1 start from customer
                    IloLinearNumExpr con14 = cplex.linearNumExpr();
                    int c1 = userparam.nodes.get(cnode.get(i)).index;
                    double minttime = 100000;
                    double dis_i_0 = userparam.distBase[c1][userparam.nbclients + 1];
                    minttime = dis_i_0 / userparam.maxspeed;
                    con14.addTerm(1, a[numCity - 1][k][c]);
                    con14.addTerm(-1, a[i][k][c]);
                    con14.addTerm(-1 * M, y[i][k][c]);
                    cplex.addGe(con14, minttime - 1 * M);
                    st_count += 1;
                    //System.out.println("Current contraints= "+st_count);
                }
            }
        }
        for (int k = 0; k < vehK; k++) {
            for (int c = 0; c < rouCs.get(k); c++) {
                for (int i = 1; i < numCity - 1; i++) {//i=1 start from customer
                    IloLinearNumExpr con14 = cplex.linearNumExpr();
                    int c1 = userparam.nodes.get(cnode.get(i)).index;
                    double minttime = 100000;
                    double dis_0_i = userparam.distBase[c1][userparam.nbclients + 1];
                    minttime = dis_0_i / userparam.maxspeed;
                    con14.addTerm(1, a[i][k][c]);
                    con14.addTerm(-1, a[0][k][c]);
                    con14.addTerm(-1 * M, y[i][k][c]);
                    cplex.addGe(con14, minttime - 1 * M);
                    st_count += 1;
                    //System.out.println("Current contraints= "+st_count);
                }
            }
        }

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-HH-mm");
        Date date = new Date(System.currentTimeMillis());
        System.out.println(formatter.format(date));
        if (export) {
            cplex.exportModel("./dataset/" + this.prefix + "/cplexResult/cplex_model/" + exportname);
        }
        //singleregion_model"+formatter.format(date)+".lp");}
        System.out.println("./dataset/" + this.prefix + "/cplex_model/" + exportname);
        //cplex.setParam(cplex.Param.TimeLimit, 60);
        cplex.setParam(IloCplex.Param.TimeLimit, timelimit);
        //cplex.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, timelimit);
        System.out.println("Finish build model ");
        System.out.println("variable count= " + variable_count);
        System.out.println("constraint count= " + st_count);
        bw1.write("\n variable count= " + variable_count);
        bw1.flush();
        bw1.write("\n constraint count= " + st_count);
        bw1.flush();
        bw1.write("\n Finish build model ");
        bw1.flush();

        boolean feasible = cplex.solve();
        obj = cplex.getObjValue();
        violation_con = 0;
        /*for(int k1=0;k1<vehK;k1++) {
        	for(int k2=0;k2<vehK;k2++) {
        		if(k1!=k2) {
        		if(cplex.getValue(z[k1][k2])>0.5) {
        			violation_con+=1;
        		}}
        	}}*/
        finalcompletetime = (obj - violation_con * ccon) / cdis;

        double[][][][][] currentx_kt = new double[numCity][numCity][vehK][rouC][edgeH];
        double[][][] currenty_kt = new double[numCity][vehK][rouC];
        double time1 = timer.elapsedTime();
        System.out.println("Now the time                    = " + time1);
        bw1.write("\n Now the time                    = " + time1);
        bw1.flush();

        //TODO get_route_solution
        if (feasible) {
            for (int i = 0; i < numCity; i++) {
                for (int j = 0; j < numCity; j++) {
                    int c1 = userparam.nodes.get(cnode.get(i)).index;
                    int c2 = userparam.nodes.get(cnode.get(j)).index;
                    for (int k = 0; k < vehK; k++) {
                        for (int c = 0; c < rouCs.get(k); c++) {
                            for (int h = 0; h < edge_Hs[c1][c2]; h++) {
                                if (i != j && j != 0) {
                                    currentx_kt[i][j][k][c][h] = cplex.getValue(x[i][j][k][c][h]);
                                }
                                if (greater(currentx_kt[i][j][k][c][h], 0.5)) {
                                    System.err.println(x[i][j][k][c][h].getName() + " = " + currentx_kt[i][j][k][c][h]);
                                }
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < numCity; i++) {
                int c1 = userparam.nodes.get(cnode.get(i)).index;
                for (int k = 0; k < vehK; k++) {
                    for (int c = 0; c < rouCs.get(k); c++) {
                        currenty_kt[i][k][c] = cplex.getValue(y[i][k][c]);
                        if (greater(currenty_kt[i][k][c], 0.5)) {
                            System.err.println(y[i][k][c].getName() + " = " + currenty_kt[i][k][c]);
                        }
                    }
                }
            }

            for (int k = 0; k < vehK; k++) {
                ArrayList<ArrayList<Integer>> tmpc = new ArrayList<ArrayList<Integer>>();
                ArrayList<route2> tmpr = new ArrayList<route2>();
                for (int c = 0; c < rouCs.get(k); c++) {
                    double[][] currentx = new double[numCity][numCity];
                    for (int i = 0; i < numCity; i++) {
                        for (int j = 0; j < numCity; j++) {
                            double tmp = 0;
                            int c1 = userparam.nodes.get(cnode.get(i)).index;
                            int c2 = userparam.nodes.get(cnode.get(j)).index;
                            for (int h = 0; h < edge_Hs[c1][c2]; h++) {
                                tmp += currentx_kt[i][j][k][c][h];
                            }
                            currentx[i][j] = tmp;
                        }
                    }
                    for (int i = 0; i < numCity; i++) {
                        for (int j = 0; j < numCity; j++) {
                            if (greater(currentx[i][j], 0.5)) {
                                System.err.println(a[i][k][c].getName() + " = " + cplex.getValue(a[i][k][c]));
                                tmp_sct.add(cplex.getValue(a[i][k][c]));
                            }
                        }
                    }

                    List<List<Integer>> subtour = new ArrayList<List<Integer>>();
                    subtour = findSubtour(currentx, N);
                    ArrayList<Integer> k1 = new ArrayList<Integer>();
                    k1.add(k);
                    k1.add(c);
                    ArrayList<Integer> tmp = new ArrayList<Integer>();
                    route2 tmproute = new route2();
                    double kc_a = -1;
                    double kc_f = -1;
                    //System.err.println("There are "+subtour.size()+" subtours");
                    if (subtour.size() <= 1) {
                        if (subtour.size() == 0) {
                            tmp = new ArrayList<Integer>();
                        } else {
                            kc_a = cplex.getValue(a[0][k][c]);
                            kc_f = cplex.getValue(a[numCity - 1][k][c]);
                            int[] optRoute = new int[subtour.get(0).size()];
                            for (int i1 = 0; i1 < subtour.get(0).size(); i1++) {
                                optRoute[i1] = subtour.get(0).get(i1);
                                int c1 = userparam.nodes.get(cnode.get(subtour.get(0).get(i1))).index;
                                tmp.add(c1);
                                //tmp.add(subtour.get(0).get(i1));
                            }
                        }
                        kroutes.put(k1, tmp);
                        tmproute.set_departime(kc_a);
                        tmproute.setconsumer(tmp);
                        tmproute.setvidx(k);
                        //tmproute.setcidx(c);
                        tmproute.set_completetime(kc_f);
                        tmproute.set_traveltime(kc_f - kc_a);
                        tmproute.cal_largest_releasetime(userparam);

                        tmpr.add(tmproute);
                        //tmpc.add(tmp);
                        bw1.write("\n The Vehicle " + k + " Route Index " + c + " for model has no subtour now! ");
                        bw1.flush();
                        System.out.println("The Vehicle " + k + " Route Index " + c + " for model has no subtour now! ");
                        System.out.println("The Vehicle " + k + " Route Index " + c + " depart at= "
                                + cplex.getValue(a[0][k][c]) + " finish at= " + cplex.getValue(a[numCity - 1][k][c]));

                        for (int i1 = 0; i1 < subtour.size(); i1++) {
                            System.out.println("Subtour: " + i1 + " " + subtour.get(i1).toString());
                            bw1.write("Subtour: " + i1 + " " + subtour.get(i1).toString());
                            bw1.flush();
                        }
                    }//ÿ��K,C��ֻ��һ��·
                    else {
                        System.err.println("!!! There are " + subtour.size() + " subtours");
                        bw1.write("\n !!! The Vehicle " + k + " Route Index " + c + " for model has subtours");
                        bw1.write("\n !!! There are " + subtour.size() + " subtours");
                        bw1.flush();
                    }
                }//c
                this.v1routes.put(k, tmpr);
            }//k

        }//feasible
        double time2 = timer.elapsedTime();
        System.out.println("Total time= " + time2 + " DDL= " + userparam.DDL + " CPLEX OBJ= " + obj);
        //System.out.println("Cur SCT= " + cur_sct +" a= "+idxj);
        Collections.sort(tmp_sct);
        System.out.println("Cur Sct= " + tmp_sct.toString());
        bw1.write("\n Total time                   = " + time2);
        bw1.flush();
    }

    public List<List<Integer>> findSubtour(double[][] x, int N) {
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

        while (count < set1.size() - 1) {//TODO
            int next = -1;
            for (int i = 0; i < N; i++) {
                if (greater(x[currentCity][i], 0.5)) {
                    next = i;
                    break;
                }
            }
            //System.err.println("Count: "+count+"Cur: "+currentCity+" Next: "+next);

            if (next == N - 1) {
                System.err.println(list.toString());
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

        record.add(list);
        //System.err.println(list.toString());

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
                System.err.println(list.toString());
                //list.add(e);
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

    // TODO :::
    public void reportResult(HashMap<Integer, ArrayList<route2>> vroutes) throws IOException {

        for (Integer k : vroutes.keySet()) {
            ArrayList<route2> a = vroutes.get(k);
            for (route2 r : a) {

                r.display_route1();
            }
            //bw1.write("\n The Vehicle "+k+" Route Index "+ i);bw1.flush();
            //bw1.write("\n The Vehicle "+k+" Start at "+startime+" travel time "+tsptime+
            //" End at "+endtime);bw1.flush();
            //bw1.write("\n Served Customer: "+a.get(i).toString());
        }

    }

    public void reportResult_d() throws IOException {
        for (Integer k : this.v1routes.keySet()) {
            ArrayList<route2> a = this.v1routes.get(k);
            bw1.write("The Vehicle " + k + " \n");
            ;
            bw1.flush();
            for (route2 r : a) {
                r.display_route1();
                r.display_route(bw1);
            }
        }
    }

    public void reportResult_s() throws IOException {

        for (Integer k : this.kroutes1.keySet()) {
            ArrayList<ArrayList<Integer>> a = this.kroutes1.get(k);
            double startime = 0;
            double tsptime = 0;
            double endtime = 0;
            for (int i = 0; i < a.size(); i++) {
                bw1.write("\n The Vehicle " + k + " Route Index " + i);
                bw1.flush();
                startime = Math.max(checkRd(a.get(i)), endtime);
                tsptime = check(a.get(i));
                endtime = startime + tsptime;
                bw1.write("\n The Vehicle " + k + " Start at " + startime + " travel time " + tsptime +
                        " End at " + endtime);
                bw1.flush();
                bw1.write("\n Served Customer: " + a.get(i).toString());

            }

        }

        bw1.write("\n CPLEX ObjectiveValue: " + obj);
        bw1.write("\n Final completetime: " + finalcompletetime);
        bw1.write("\n consistency violation times: " + violation_con);
        bw1.flush();
        System.err.print("\n CPLEX ObjectiveValue: " + obj);
        System.err.print("\n Final completetime: " + finalcompletetime);
        System.err.print("\n consistency violation times: " + violation_con);
    }

    public File makefile(String fp) {
        File file = null;
        try {
            file = new File(fp);
            if (file.exists()) {
                file.delete();
            }//delete file!!!
            if (!file.getParentFile().exists()) {
                boolean mkdir = file.getParentFile().mkdirs();
                if (!mkdir) {
                    throw new RuntimeException("Fail");
                }
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
        }
        return file;
    }

    public File makefile1(String fp) {
        File file = null;
        try {
            file = new File(fp);
            //if (file.exists()) {file.delete();}//delete file!!!
            if (!file.getParentFile().exists()) {
                boolean mkdir = file.getParentFile().mkdirs();
                if (!mkdir) {
                    throw new RuntimeException("Fail");
                }
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
        }
        return file;
    }

    public double check(ArrayList<Integer> route) {
        // TODO Auto-generated method stub
        double checkdistance = 0;
        if (route.size() == 0) {
            return checkdistance;
        }
        int pre = 0;

        for (int i = 1; i < route.size(); i++) {
            checkdistance += userparam.distance[route.get(pre)][route.get(i)];
            pre = i;
        }
        checkdistance += userparam.distance[route.get(pre)][route.get(0)];
        //System.out.println("CHECk The optimal route distance is "+this.checkdistance);
        return checkdistance;
    }

    public double checkRd(ArrayList<Integer> route) {
        // TODO Auto-generated method stub
        double checkrd = 0;
        for (int i = 0; i < route.size(); i++) {
            //System.err.println()
            checkrd = Math.max(userparam.releasetime.get(route.get(i)), checkrd);

        }
        return checkrd;
    }

    public boolean greater(double a, double b) {
        if (a - b > 1E-6) {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {

        int maxrounds = 30;
        String pre_fix = "STUDYCASE";
        String c = " ";
        String egdefile = "";
        boolean isdynamic = false;
        boolean isexport = true;

        c = "A65/CaseData1_65_4_16.txt";
        egdefile = "./dataset/" + pre_fix + "/inputdata/A65/RouteData1_65_4_16.txt";

        boolean isminc = false;//更改目标函数 2
        boolean ismintcost = false;//
        boolean ismindcost = false;
        boolean isrd = true;

        String objf = "minc";
        objf = "maxorderfulliment";

        double speed_Origin = 800;//m/min 48km/h
        int region_num = 4;
        double time_horizon = 1200;//20*60 更改整个规划时间 3
        int K = 1;
        int kc = 3;
        int didx = -1;//更改车辆数目 出车次数 区域 4

        int DDL = 1200;
        int SCT = 960;//更改DDL,SCT	 5
        int cnum = 66;//更改顾客数目 6

        int cdis = 1;
        int ccon = 100000;
        double timelimit = 4800;

        int[][] help_value = new int[2][2];
        help_value[0][0] = 0;
        help_value[1][1] = 0;
        help_value[0][1] = 0;//0helps1
        help_value[1][0] = 0;
        boolean inforce_helpvalue = true;//true  强制输入值ֵ false  更改7
        boolean inforce_quantity = true;
        int[][] z1value = new int[2][2];
        z1value[0][0] = 0;
        z1value[1][1] = 0;
        z1value[0][1] = 0;//
        z1value[1][0] = 0;


        isdynamic = true;
        boolean isduration = false;
        boolean isload = false;
        boolean servet = false;
        boolean istw = false;

        boolean isa = false;
        boolean isb = false;
        boolean isQ = false;
        double duration = 550;
        double Q = 700.0;

        String portname = c.substring(4) + "_K" + Integer.toString(K) + "_R" + Integer.toString(kc) + "_T" +
                Double.toString(time_horizon) + "_DDL" + Integer.toString(DDL);
        String exportname = c.substring(4).split("\\.")[0] + "_region" + Integer.toString(didx) + "_DDL" + Integer.toString(DDL) + "_" + objf + ".lp";
        //0.2	0.1	0.4	0.1	0.2
        ArrayList<Double> partation = new ArrayList<Double>();
        partation.add(0.0);
        partation.add(0.2);
        partation.add(0.3);
        partation.add(0.7);
        partation.add(0.8);
        partation.add(1.0);

        int[][] edge_cat = new int[cnum][cnum];

        Scanner in1 = new Scanner(Paths.get(egdefile));//

        int i = 0;
        while (in1.hasNextLine()) {
            String[] temp = in1.nextLine().split("\\s+");
            for (int j = 0; j < cnum; j++) {
                int ec = Integer.parseInt(temp[j]);
                edge_cat[i][j] = ec;
            }
            i += 1;
        }

        ArrayList<ArrayList<Double>> speed = new ArrayList<ArrayList<Double>>();
        //[[1, 0.333333, 0.666667, 0.5, 0.833333], [1.16667, 0.666667, 1.33333, 0.833333, 1],
        // [1.5, 1, 1.66667, 1.16667, 1.33333]
        ArrayList<Double> t1 = new ArrayList<Double>();
        ArrayList<Double> t2 = new ArrayList<Double>();
        ArrayList<Double> t3 = new ArrayList<Double>();
        t1.add(1.0 * speed_Origin);
        t1.add(1.0 / 3.0 * speed_Origin);
        t1.add(2.0 / 3.0 * speed_Origin);
        t1.add(1.0 / 2.0 * speed_Origin);
        t1.add(5.0 / 6.0 * speed_Origin);
        t2.add(7.0 / 6.0 * speed_Origin);
        t2.add(2.0 / 3.0 * speed_Origin);
        t2.add(4.0 / 3.0 * speed_Origin);
        t2.add(5.0 / 6.0 * speed_Origin);
        t2.add(1.0 * speed_Origin);
        t3.add(1.5 * speed_Origin);
        t3.add(1.0 * speed_Origin);
        t3.add(5.0 / 3.0 * speed_Origin);
        t3.add(7.0 / 6.0 * speed_Origin);
        t3.add(4.0 / 3.0 * speed_Origin);
        speed.add(t1);
        speed.add(t2);
        speed.add(t3);


        String input_datafile = "./dataset/" + pre_fix + "/inputdata/" + c;
        String infor_file = "./dataset/" + pre_fix + "/cplexResult/information/infor_" + c.substring(4);
        String rfile = "./dataset/" + pre_fix + "/cplexResult/timeinformation/setTime_" + Integer.toString(DDL) + "_" + Integer.toString(kc) + "_" + c.substring(4);
        String vfile = "./dataset/" + pre_fix + "/cplexResult/timeinformation/real_ArriveTime_" + Integer.toString(DDL) + "_" + Integer.toString(kc) + "_" + c.substring(4);//arrive time
        String vfile1 = "./dataset/" + pre_fix + "/cplexResult/timeinformation/real_TravelTime_" + Integer.toString(DDL) + "_" + Integer.toString(kc) + "_" + c.substring(4);//travel time
        String bpfile = "./dataset/" + pre_fix + "/cplexResult/timeinformation/breakpoints_" + Integer.toString(DDL) + "_" + Integer.toString(kc) + "_" + c.substring(4);//travel time
        String dfile = "./dataset/" + pre_fix + "/cplexResult/districtinformation/district_" + Integer.toString(DDL) + "_" + c.substring(4);
        String updatefile = "./dataset/" + pre_fix + "/cplexResult/timeinformation/updatetime_" + Integer.toString(DDL) + "_" + c.substring(4);

        paramSDD_case sd = new paramSDD_case(speed_Origin);
        sd.initParams(input_datafile, infor_file,
                K, time_horizon, DDL, SCT, isdynamic, region_num, partation, servet);

        if (isdynamic) {
            System.out.println("Cal BP");
            sd.cal_BreakPoint(speed, bpfile, vfile, vfile1, edge_cat, 1);
            //System.out.println("Cal ND");
            //sd.setNodeDistrict_cluster(dfile);
        }


        String result_file = "./dataset/" + pre_fix + "/cplexResult/cplex_result/dynamic_result_" + c.substring(4);
        System.err.println("RESULT_FILE= " + result_file);

        //更改某段的速度：
        //public void UpdateLinkTime_ij(int v,int w,ArrayList<Double> newspeed,
        //String updatefile) throws IOException {

        ArrayList<Double> newspeed = new ArrayList<Double>();
        ArrayList<Double> newspeed1 = new ArrayList<Double>();
        newspeed.add(0.3);
        newspeed.add(0.3);
        newspeed.add(0.3);
        newspeed.add(0.3);
        newspeed.add(0.3);
        newspeed1.add(3.0);
        newspeed1.add(3.0);
        newspeed1.add(3.0);
        newspeed1.add(3.0);
        newspeed1.add(3.0);

        // 这个点路况 差一; 区域点多的 要慢

        SDDModel test = new SDDModel(result_file, pre_fix, sd, cdis, ccon);
        sd.nodeset.forEach(a -> System.err.println(a.toString()));

        didx = 2;// 0 1 2 3
        List<Integer> cnode0 = sd.nodeset.get(didx);
        //cnode0.add(38);

        cnode0.remove(12);



        List<Integer> cnode1 = sd.nodeset.get(0);//worse
        List<Integer> cnode2 = sd.nodeset.get(1);
        List<Integer> cnode3 = sd.nodeset.get(2);
        List<Integer> cnode4 = sd.nodeset.get(3);

        boolean kk = false;
        if (kk) {
            ArrayList<Integer> candi = new ArrayList<Integer>();
            candi.add(22);
            for (Integer a1 : candi) {
                for (Integer e : cnode1) {
                    if (e != a1) {
                        sd.UpdateLinkTime_ij(e, a1, newspeed, updatefile);
                    }
                    if (e != 0 && e != a1) {
                        sd.UpdateLinkTime_ij(a1, e, newspeed, updatefile);
                    }
                }
            }

            for (Integer a1 : candi) {
                for (Integer e : cnode2) {
                    if (e != a1) {
                        sd.UpdateLinkTime_ij(e, a1, newspeed1, updatefile);
                    }
                    if (e != 0 && e != a1) {
                        sd.UpdateLinkTime_ij(a1, e, newspeed1, updatefile);
                    }
                }
            }
        }

        System.err.println("Current Customer in districts  = " + cnode0.toString());

        test.build_mincomplete_model(cnode0, kc, isexport, exportname, isrd, isduration,
                duration, Q, istw, isload, isQ, isa, isb, timelimit, isminc, ismintcost, ismindcost, help_value,
                inforce_helpvalue, inforce_quantity, z1value);
        //1region--cnode0
        //2region-cnode
        test.reportResult_d();


    }


}
