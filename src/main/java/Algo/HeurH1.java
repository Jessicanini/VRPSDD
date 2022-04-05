package Algo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import Base.Node;
import Base.paramSDD_case;
import Base.route;
import Base.trip;
import TimeNetWork.TBP;
import ilog.concert.IloException;
import util.StdRandom;
import util.aorder;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


public class HeurH1 {
    FileWriter fw1;
    BufferedWriter bw1;
    FileWriter fw2;
    BufferedWriter bw2;

    paramSDD_case userparam;
    Node depot;
    int maxrounds = 50;

    List<Integer> nodesindex = new ArrayList<>();

    public int kc = 0;
    public int nodenums = 0;

    public int phase = 0;
    public int param1 = 3;
    public int remove_trip_num = 0;
    public int remove_customer_num = 0;

    int whichlevel = -1;
    int operation_d0 = -1;
    int operation_d1 = -1;
    int operation_d2 = -1;
    int operation_r = -1;

    double level_lamda = 0;
    double d_lamda = 0;
    double r_lamda = 0;

    double[][] levels = new double[3][100];
    double[][][] removals = new double[3][3][100];//default_0
    double[][] repairs = new double[3][100];

    double level1 = 0;//whichlevel=0
    double level2 = 0;//whichlevel=1
    double level3 = 0;//whichlevel=2

    double a0_0 = 1;
    double a1_0 = 0;
    double a1_1 = 0;
    double a1_2 = 0;
    double a2_0 = 0;
    double a2_1 = 0;
    double a2_2 = 0;

    double k2_1_0 = 0;
    double k2_1_1 = 0;
    double k2_2_0 = 0;//rd
    double k2_2_1 = 0;//average distance

    double b0 = 0;
    double b1 = 0;
    double b2 = 0;

    double ls_op1 = 0;
    double ls_op2 = 0;
    double ls_op3 = 0;
    double ls_op4 = 0;
    double ls_op5 = 0;
    int whichoperator = 0;
    double ls_w = 0;

    double tempT = 200;
    double pt1 = 0.8;
    double w1 = 0;
    double w2 = 0;
    double w3 = 0;
    double w4 = 0;

    String pre_fix = "";
    public int quantity_a1 = 0;
    public int quantity_a2 = 0;
    public int quantitya1min = 1;
    public int quantitya1max = 0;
    public int quantitya2min = 1;
    public int quantitya2max = 0;

    public HeurH1(List<Integer> nodesindex, String pre_fix, String result_file1, String result_file2,
                  String readytimefile, paramSDD_case userparam2,
                  ArrayList<Integer> paramset1, ArrayList<Double> paramset2,
                  ArrayList<Double> paramset3, ArrayList<Double> paramset4,
                  ArrayList<Double> paramset5, ArrayList<Double> paramsetw,
                  ArrayList<Double> paramset6, ArrayList<Double> paramsetls,
                  double ls_w, int phase) throws IOException {
        fw1 = new FileWriter(makefile(result_file1), false);
        bw1 = new BufferedWriter(fw1);
        fw2 = new FileWriter(makefile(result_file2), false);
        bw2 = new BufferedWriter(fw2);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                levels[i][j] = 0;
                repairs[i][j] = 0;
                for (int k = 0; k < 100; k++) {
                    removals[i][j][k] = 0;
                }
            }
        }

        this.pre_fix = pre_fix;
        this.userparam = userparam2;
        this.depot = userparam2.depots.get(0);
        this.nodesindex = nodesindex;

        this.phase = phase;
        this.tempT = paramset5.get(0);
        this.pt1 = paramset5.get(1);

        this.param1 = paramset1.get(0);
        this.remove_trip_num = paramset1.get(1);
        this.remove_customer_num = paramset1.get(2);

        level1 = paramset2.get(0);//0
        level2 = paramset2.get(1);//0
        level3 = paramset2.get(2);//1
        levels[0][0] = level1;
        levels[1][0] = level2;
        levels[2][0] = level3;

        a1_0 = paramset3.get(0);//ѡtrip a1_0 random
        a1_1 = paramset3.get(1);//ѡtrip a1_1 saving
        a1_2 = paramset3.get(2);//ѡtrip a1_2 related
        a2_0 = paramset3.get(3);//ѡnode a2_0 random
        a2_1 = paramset3.get(4);//ѡnode a2_1 saving
        a2_2 = paramset3.get(5);//ѡnode a2_2 related

        b0 = paramset3.get(6);//repair random
        b1 = paramset3.get(7);//repair saving
        b2 = paramset3.get(8);//repair related
        removals[0][0][0] = a0_0;
        removals[1][0][0] = a1_0;
        removals[1][1][0] = a1_1;
        removals[1][2][0] = a1_2;
        removals[2][0][0] = a2_0;
        removals[2][1][0] = a2_1;
        removals[2][2][0] = a2_2;
        repairs[0][0] = b0;
        repairs[1][0] = b1;
        repairs[2][0] = b2;

        k2_1_0 = paramset4.get(0);
        k2_1_1 = paramset4.get(1);
        k2_2_0 = paramset4.get(2);
        k2_2_1 = paramset4.get(3);

        w1 = paramsetw.get(0);
        w2 = paramsetw.get(1);
        w3 = paramsetw.get(2);
        w4 = paramsetw.get(3);
        this.ls_op1 = paramsetls.get(0);
        this.ls_op2 = paramsetls.get(1);
        this.ls_op3 = paramsetls.get(2);
        this.ls_op4 = paramsetls.get(3);
        this.ls_op5 = paramsetls.get(4);
        this.ls_w = ls_w;
        this.level_lamda = paramset6.get(0);
        this.d_lamda = paramset6.get(1);
        this.r_lamda = paramset6.get(2);

        bw1.write("\n Initial param1 = " + this.param1);
        bw1.write("\n Initial temp = " + this.tempT + " rate= " + this.pt1);
        bw1.write("\n Initial w1 = " + this.w1 + " w2 = " + this.w2 + " w3 = " + this.w3 + " w4 = " + this.w4);
        bw1.write("\n Remove_trip num = " + this.remove_trip_num + " Remove_customer num = " + this.remove_customer_num);
        bw1.write("\n level1 = " + this.level1 + " level2 = " + this.level2 + " level3 = " + this.level3);
        bw1.write("\n select trip a1_0 random = " + this.a1_0 + " trip a1_1 saving = " + this.a1_1 +
                " trip a1_2 related = " + this.a1_2);
        bw1.write("\n select node a2_0 random = " + this.a2_0 + " node a2_1 saving = " + this.a2_1 +
                " node a2_2 related = " + this.a2_2);
        bw1.write("\n repair node b0 random = " + this.b0 + " node b1 saving = " + this.b1 +
                " node b2 related = " + this.b2);
        bw1.write("\n save time k2_1_0 = " + this.k2_1_0 + " dis k2_1_1 saving = " + this.k2_1_1 +
                " rd k2_2_0 related = " + this.k2_2_0 + " dis k2_2_1 related = " + this.k2_2_1);
        bw1.write("\n ls_operator ls_1 = " + this.ls_op1 + " ls_2 = " + this.ls_op2 +
                " ls_3 = " + this.ls_op3 + " ls_4 = " + this.ls_op4 + " ls_5 = " + this.ls_op5);
        bw1.flush();

        bw2.write("\n Initial param1 = " + this.param1);
        bw2.write("\n Initial temp = " + this.tempT + " rate= " + this.pt1);
        bw2.write("\n Initial w1 = " + this.w1 + " w2 = " + this.w2 + " w3 = " + this.w3 + " w4 = " + this.w4);
        bw2.write("\n Remove_trip num = " + this.remove_trip_num + " Remove_customer num = " + this.remove_customer_num);
        bw2.write("\n level1 = " + this.level1 + " level2 = " + this.level2 + " level3 = " + this.level3);
        bw2.write("\n select trip a1_0 random = " + this.a1_0 + " trip a1_1 saving = " + this.a1_1 +
                " trip a1_2 related = " + this.a1_2);
        bw2.write("\n select node a2_0 random = " + this.a2_0 + " node a2_1 saving = " + this.a2_1 +
                " node a2_2 related = " + this.a2_2);
        bw2.write("\n repair node b0 random = " + this.b0 + " node b1 saving = " + this.b1 +
                " node b2 related = " + this.b2);
        bw2.write("\n save time k2_1_0 = " + this.k2_1_0 + " dis k2_1_1 saving = " + this.k2_1_1 +
                " rd k2_2_0 related = " + this.k2_2_0 + " dis k2_2_1 related = " + this.k2_2_1);
        bw2.write("\n ls_operator ls_1 = " + this.ls_op1 + " ls_2 = " + this.ls_op2 +
                " ls_3 = " + this.ls_op3 + " ls_4 = " + this.ls_op4 + " ls_5 = " + this.ls_op5);
        bw2.flush();

    }

    public void ALNS(int maxcnt, int maxcnt1, int maxcntL) throws Exception {

        route initial_sol = new route();
        initial_sol = this.get_initialsolution();

        route cur_sol = new route();
        cur_sol = initial_sol.routecopy();

        route current_bestsol = new route();
        current_bestsol = initial_sol.routecopy();

        int itercount = 0;

        bw1.write("\n initial ddl= " + initial_sol.last_completetime +
                " cur_sol ddl= " + cur_sol.last_completetime + " current_bestsol ddl= "
                + current_bestsol.last_completetime + " \n");
        bw1.write("\n initial sct= " + initial_sol.cur_SCT +
                " cur_sol sct= " + cur_sol.cur_SCT + " current_bestsol sct= "
                + current_bestsol.cur_SCT + " \n");
        bw1.write("\n initial H = " + initial_sol.getCur_H() +
                " cur_sol H = " + cur_sol.getCur_H() + " current_bestsol H = "
                + current_bestsol.getCur_H() + " \n");

        while (itercount < maxcnt) {
            int cnt1 = 0;
            boolean flag1 = false;
            bw1.write("\n --- ---------- ------ ----------  ---");
            bw1.flush();
            bw1.write("\n Iteration= " + itercount + " \n");
            bw1.flush();
            bw2.write("\n Iteration= " + itercount);
            bw2.flush();

            route new_local_sol = this.local_search(maxcntL, cur_sol);
            bw1.write("\n After Local Search: ");
            bw1.flush();

            double deltals = cur_sol.total_traveltime - new_local_sol.total_traveltime;
            double deltals1 = current_bestsol.total_traveltime - new_local_sol.total_traveltime;
            double deltalsct = cur_sol.cur_SCT - new_local_sol.cur_SCT;
            double deltalsct1 = current_bestsol.cur_SCT - new_local_sol.cur_SCT;
            double deltalh = new_local_sol.cur_H - cur_sol.cur_H;
            double deltalh1 = new_local_sol.cur_H - current_bestsol.cur_H;

            if (greater(deltalh, 0)) {
                cur_sol = new_local_sol.routecopy();
                if (greater(deltalh1, 0)) {
                    current_bestsol = new_local_sol.routecopy();
                    bw1.write("\n Find more new_local_solution.fulfillment = " + new_local_sol.cur_H);
                    bw1.flush();
                    new_local_sol.display_route(bw1);
                }
            } else {
                bw1.write("\n Find new new_local_solution.fulfillment=" + new_local_sol.cur_H);
                bw1.flush();
            }
            while (cnt1 < maxcnt1 && !flag1) {
                bw1.write("\n DR iter= " + cnt1);
                bw1.flush();
                bw2.write("\n DR iter= " + cnt1);
                bw2.flush();

                boolean accept = false;
                route tmp_sol = new route();
                tmp_sol = this.destroy_and_repair(cur_sol);
                double delta = -cur_sol.cur_H + tmp_sol.cur_H;
                double delta1 = -current_bestsol.cur_H + tmp_sol.cur_H;

                bw1.write("\n temp_sol h= " + tmp_sol.cur_H + " cur_sol h= " + cur_sol.cur_H);
                bw1.write("\n temp_sol sct= " + tmp_sol.cur_SCT + " cur_sol sct= " + cur_sol.cur_SCT);

                if (greater(delta, 0)) {
                    //bw1.write("\n delta= "+delta+" Find new tmp_sol better than current one; completetime="
                    //		+tmp_sol.last_completetime);bw1.flush();
                    bw1.write("\n delta= " + delta + " Find new tmp_sol better than current one; h="
                            + tmp_sol.cur_H);
                    bw1.flush();
                    cur_sol = tmp_sol.routecopy();
                    flag1 = true;
                    if (greater(delta1, 0)) {
                        bw1.write("\n delta1= " + delta1 + " Find tmp_sol new global best; h=" + tmp_sol.cur_H);
                        bw1.flush();
                        current_bestsol = tmp_sol.routecopy();
                    }
                } else {
                    //SA select
                    double q1 = StdRandom.uniform();
                    if (Math.exp(delta / tempT) > q1) {
                        accept = true;
                        cur_sol = tmp_sol.routecopy();
                        bw1.write("\n delta/tempT= " + Math.exp(delta / tempT) + " prob= " + q1 + " Accept worse tmp_sol;"
                                + " h=" + tmp_sol.cur_H);
                        bw1.flush();
                    } else {
                        bw1.write("\n delta/tempT= " + Math.exp(delta / tempT) + " prob= " + q1 + " Discard worse tmp_sol; "
                                + "h=" + tmp_sol.cur_H);
                        bw1.flush();
                    }
                }
                bw1.write("\n Finish DR iteration= " + cnt1);
                bw1.flush();
                bw1.write("\n --- ---------- ------ ----------  ---");
                bw1.flush();
                //cur_sol.display_route(bw1);
                cnt1 += 1;
                this.adjust_dr_params(delta, delta1, accept, cnt1);
                tempT = pt1 * tempT;
            }
            //D & R
            itercount += 1;
            // LOCAL
        }
        bw1.write("\n --- ------ --- ------- ---");
        bw1.write("\n Display Final Solution");
        bw1.flush();
        bw1.write("\n Sol_Minc= " + current_bestsol.last_completetime + " minsct= "
                + current_bestsol.cur_SCT + " curH= " + current_bestsol.cur_H);

        System.out.print("\n Display Final Sol ");
        current_bestsol.display_route(bw1);
        current_bestsol.display();
        System.out.print("\n Sol_Minc= " + current_bestsol.last_completetime + " minsct= "
                + current_bestsol.cur_SCT + " curH= " + current_bestsol.cur_H);
    }


    public route get_initialsolution()
            throws Exception {

        this.nodenums = nodesindex.size() - 1;
        bw1.write("\n --- ---------- ------ ----------  ---");
        bw1.flush();
        bw1.write("\n Start Get Initial Solution split orders to " + Integer.toString(param1) + " trips");
        bw1.flush();
        ArrayList<Node> totalnodes = new ArrayList<Node>();
        ArrayList<Node> cnodes = new ArrayList<Node>();

        route initial_route = new route();
        ArrayList<trip> initial_trips = new ArrayList<trip>();

        for (Integer e1 : nodesindex) {
            totalnodes.add(userparam.nodes.get(e1));
            if (e1 != 0) {
                cnodes.add(userparam.nodes.get(e1));
            }
        }

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("xcor"));
        attributes.add(new Attribute("ycor"));
        attributes.add(new Attribute("rd"));
        //set instances
        Instances instances = new Instances("customers", attributes, 0);
        for (Node c : cnodes) {
            Instance instance = new DenseInstance(attributes.size());
            instance.setValue(0, c.XCOORD);
            instance.setValue(1, c.YCOORD);
            instance.setValue(2, c.RELEASE_DATE);
            instances.add(instance);
        }

        EM em = new EM(); // new instance of clusterer
        SimpleKMeans KM = new SimpleKMeans();
        // This is the important parameter to set
        KM.setPreserveInstancesOrder(true);
        KM.setNumClusters(param1);
        KM.buildClusterer(instances);
        int[] assignments = KM.getAssignments();
        for (int i = 0; i < KM.getClusterSizes().length; i++) {
            System.out.println(KM.getClusterSizes()[i]);
        }
        for (int i = 0; i < cnodes.size(); i++) {
            System.out.println(i + "," + assignments[i]);
        }

        ArrayList<ArrayList<Integer>> ini_nodeset = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i < param1; i++) {
            ArrayList<Integer> tmp = new ArrayList<Integer>();
            tmp.add(0);
            ini_nodeset.add(tmp);
        }
        for (int i = 0; i < cnodes.size(); i++) {
            ini_nodeset.get(assignments[i]).add(cnodes.get(i).index);
            //System.out.println(i+","+assignments[i]);
        }
        double curtime = 0;
        boolean isexport = true;
        int ctripn = 0;
        trip a = new trip(userparam);
        for (ArrayList<Integer> e : ini_nodeset) {
            System.out.println("Trip -" + e.toString());
            bw1.write("\n Trip " + Integer.toString(ctripn) + " contains nodes: " + e.toString());
            bw1.flush();
            ArrayList<Node> tmpnode = new ArrayList<Node>();
            for (Integer e1 : e) {
                tmpnode.add(userparam.nodes.get(e1));
            }
            tmpnode.add(userparam.nodes.get(userparam.nodes.size() - 1));//depot
            curtime = Math.max(maxrd(tmpnode), curtime);
            Solve_dytsp2 test = new Solve_dytsp2(isexport, this.pre_fix, userparam, tmpnode, maxrounds, curtime);
            test.TeachingPaper_Solve();// tsp

            a = new trip(userparam);
            a = test.setTrip();
            initial_trips.add(a);
            ctripn += 1;
            curtime = test.obj;
        }
        //trip last_trip=initial_route.trips.get(ctripn-1);
        for (int i = 0; i < 1; i++) {//ATTENTION
            ArrayList<Integer> tmp1 = new ArrayList<Integer>();
            tmp1.add(0);
            tmp1.add(userparam.nbclients + 1);//ATTENTION:
            trip empty_routes = new trip(userparam);
            empty_routes.setconsumer(tmp1);
            empty_routes.cal_largest_releasetime();
            empty_routes.set_completetime(0);
            empty_routes.set_departime(0);
            empty_routes.set_traveltime(0);
            initial_trips.add(empty_routes);
            //initial_route.add(empty_routes);//generate empty route
        }
        //adjust departime --- adjust sequence TODO
        ArrayList<trip> new_trips = sort_trip(initial_trips);
        double s1 = 0;
        for (trip r : new_trips) {
            r.cal_largest_releasetime();
            s1 = Math.max(s1, r.largest_releasetime);
            r.set_departime(s1);
            r.cal_traveltime(s1);//sct
            double tt1 = r.traveltime;
            double f1 = s1 + tt1;
            r.set_completetime(f1);
            s1 = f1;
        }

        for (trip r : new_trips) {
            initial_route.add(r);
        }

        initial_route.cal_time();//sct
        initial_route.update_trip_maxH(userparam.B);
        System.out.println(" All Trip H = " + initial_route.cur_H);
        initial_route.display();
        initial_route.display_route(bw1);
        bw1.write("\n All Trip Finish at " + initial_route.last_completetime);
        bw1.write("\n All Trip SCT " + initial_route.cur_SCT);
        bw1.write("\n All Trip H = " + initial_route.cur_H);
        bw1.flush();
        return initial_route;
    }

    //   level trip-level node-level
    //   whichlevel, remove_trip_num,remove_customer_num;
    //   repair
    public route destroy_and_repair(route cursol) throws IloException, IOException {
        route newsol = new route();

        ArrayList<trip> cur_trips = new ArrayList<trip>();//copy trips
        boolean preflag = false;
        //bw1.write("\n COPY Process: ");bw1.flush();
        for (trip r : cursol.trips) {
            if (r.assignedcustomer.size() >= 2) {
                preflag = true;
            }
            trip tmpr = r.tripcopy();
            cur_trips.add(tmpr);
        }//initial
        int trip_num = cur_trips.size();

        double[] Probability = new double[3];
        Probability[0] = level1 / (level1 + level2 + level3);
        Probability[1] = level2 / (level1 + level2 + level3);
        Probability[2] = level3 / (level1 + level2 + level3);
        double[] Probability0 = new double[1];
        Probability0[0] = a0_0 / (a0_0);


        double[] Probability1 = new double[3];
        Probability1[0] = a1_0 / (a1_0 + a1_1 + a1_2);
        Probability1[1] = a1_1 / (a1_0 + a1_1 + a1_2);
        Probability1[2] = a1_2 / (a1_0 + a1_1 + a1_2);

        double[] Probability2 = new double[3];
        Probability2[0] = a2_0 / (a2_0 + a2_1 + a2_2);
        Probability2[1] = a2_1 / (a2_0 + a2_1 + a2_2);
        Probability2[2] = a2_2 / (a2_0 + a2_1 + a2_2);
        if (preflag) {
            bw1.write("\n Start Destroy: ");
            bw1.flush();
            Set<Integer> removed_customer = new HashSet<Integer>();
            Set<Integer> removed_trip = new HashSet<Integer>();

            HashMap<Integer, Integer> customer_tripidx = new HashMap<Integer, Integer>();
            for (trip r : cur_trips) {
                for (Integer c : r.assignedcustomer) {
                    if (c != 0 && c != userparam.nbclients + 1) {
                        //System.err.println("idx= "+idx+" c= "+c);
                        customer_tripidx.put(c, r.index);
                    }
                }
            }

            //level destroy Roulette wheel selection
            whichlevel = Roulette_wheel_selection(Probability);
            bw1.write("\n whichlevel= " + whichlevel);
            bw1.flush();
            System.err.println("Start Destroy: whichlevel= " + whichlevel);
            if (whichlevel == 0) {
                operation_d0 = 0;
                bw1.write("\n Select Level0: ");
                bw1.flush();
                for (int i = 0; i < this.nodesindex.size(); i++) {
                    if (this.nodesindex.get(i) > 0 && this.nodesindex.get(i) != userparam.nbclients + 1) {
                        removed_customer.add(this.nodesindex.get(i));
                    }
                }
            }// level0

            if (whichlevel == 1) {
                bw1.write("\n Select Level1: ");
                bw1.flush();
                operation_d1 = Roulette_wheel_selection(Probability1);
                if (operation_d1 == 0) {//random

                    while (removed_trip.size() < remove_trip_num) {//candidate1
                        int idx = StdRandom.uniform(trip_num);
                        if (cur_trips.get(idx).assignedcustomer.size() >= 3) {
                            removed_trip.add(idx);
                        }//attention empty
                    }
                    for (Integer r : removed_trip) {
                        for (Integer e : cur_trips.get(r).assignedcustomer) {
                            if (e > 0 && e != userparam.nbclients + 1) {
                                removed_customer.add(e);
                            }
                        }
                    }
                    bw1.write("\n Randomly Remove trips: ");
                    bw1.flush();
                    bw1.write("\n remove trip: " + removed_trip.toString());
                    bw1.write("\n remove customer: " + removed_customer.toString());
                }//for if
                if (operation_d1 == 1) {//saving efficient
                    ArrayList<Double> trip_savings = new ArrayList<Double>();
                    for (trip r : cur_trips) {
                        if (r.largest_releasetime > 0) {
                            trip_savings.add(r.traveltime / r.largest_releasetime);
                        }//TODO
                        else {
                            trip_savings.add(r.traveltime);
                        }
                    }
                    int[] b = sort2idx(trip_savings);
                    while (removed_trip.size() < remove_trip_num) {
                        int idx = StdRandom.uniform(b.length);
                        if (cur_trips.get(idx).assignedcustomer.size() >= 3) {
                            removed_trip.add(idx);
                        }
                    }
                    for (Integer r : removed_trip) {
                        for (Integer e : cur_trips.get(r).assignedcustomer) {
                            if (e > 0 && e != userparam.nbclients + 1) {
                                removed_customer.add(e);
                            }
                        }
                    }
                    bw1.write("\n Remove trips by savings: ");
                    bw1.flush();
                    bw1.write("\n remove trip: " + removed_trip.toString());
                    bw1.write("\n remove customer: " + removed_customer.toString());
                }//for if
                if (operation_d1 == 2) {//related
                    double[][] related_trip = new double[trip_num][trip_num];
                    for (int i = 0; i < trip_num; i++) {//ATTENTION-
                        for (int j = 0; j < trip_num; j++) {
                            related_trip[i][j] = Math.abs(cur_trips.get(i).completetime - cur_trips.get(j).completetime) +
                                    Math.abs(cur_trips.get(i).departime - cur_trips.get(j).departime);//
                        }
                    }

                    ArrayList<Integer> RIndxList = new ArrayList<Integer>();
                    Set<Integer> RIndxList1 = new HashSet<Integer>();

                    int ridx0 = StdRandom.uniform(trip_num);
                    //RIndxList.add(ridx0);
                    while (cur_trips.get(ridx0).assignedcustomer.size() <= 2) {//empty
                        ridx0 = StdRandom.uniform(trip_num);
                    }
                    RIndxList1.add(ridx0);
                    removed_trip.add(ridx0);
                    while (removed_trip.size() < remove_trip_num) {
                        RIndxList = new ArrayList<Integer>();
                        for (Integer e : RIndxList1) {
                            RIndxList.add(e);
                        }

                        int ridx = RIndxList.get(StdRandom.uniform(RIndxList.size()));
                        int[] b1 = sort2idx1(related_trip[ridx]);
                        while (removed_trip.size() < remove_trip_num) {
                            int ridx1 = b1[StdRandom.uniform(b1.length)];
                            if (cur_trips.get(ridx1).assignedcustomer.size() >= 3) {
                                RIndxList1.add(ridx1);
                                removed_trip.add(ridx1);
                            }
                        }
                    }
                    for (Integer r : removed_trip) {
                        for (Integer e : cur_trips.get(r).assignedcustomer) {
                            if (e > 0 && e != userparam.nbclients + 1) {
                                removed_customer.add(e);
                            }
                        }
                    }
                    bw1.write("\n Remove trips by relations: ");
                    bw1.flush();
                    bw1.write("\n remove trip: " + removed_trip.toString());
                    bw1.write("\n remove customer: " + removed_customer.toString());
                }//for if
            }// level1

            if (whichlevel == 2) {
                bw1.write("\n Select Level2: ");
                bw1.flush();
                operation_d2 = Roulette_wheel_selection(Probability2);
                if (operation_d2 == 0) {//random
                    while (removed_customer.size() < remove_customer_num) {//candidate1
                        int idx = 1 + StdRandom.uniform(this.nodenums);
                        removed_customer.add(this.nodesindex.get(idx));
                    }
                    bw1.write("\n Randomly Remove Customers: ");
                    bw1.flush();
                    bw1.write("\n remove customer: " + removed_customer.toString());
                    bw1.flush();
                }//for if
                if (operation_d2 == 1) {//saving
                    ArrayList<aorder> Tconsumer_saving = new ArrayList<aorder>();
                    for (trip r : cur_trips) {
                        //System.err.println(r.assignedcustomer.toString());
                        ArrayList<aorder> consumer_saving = new ArrayList<aorder>();
                        consumer_saving = r.cal_nodesave(k2_1_0, k2_1_1);//TODO
                        for (int i = 0; i < consumer_saving.size(); i++) {
                            Tconsumer_saving.add(consumer_saving.get(i));
                        }
                    }

                    ArrayList<Integer> b1 = sort_aorder1(Tconsumer_saving);
                    int t1 = b1.size();
                    while (removed_customer.size() < remove_customer_num) {
                        int idx1 = StdRandom.uniform(t1);
                        if (b1.get(idx1) != 0 && b1.get(idx1) != userparam.nbclients + 1) {
                            removed_customer.add(b1.get(idx1));
                        }
                    }
                    bw1.write("\n Remove Customers by savings: ");
                    bw1.flush();
                    bw1.write("\n remove customer: " + removed_customer.toString());
                }//for if
                if (operation_d2 == 2) {//related
                    //k2_2_0,
                    //k2_2_1
                    double[][] savings = new double[userparam.nbclients + 1][userparam.nbclients + 1];
                    ArrayList<ArrayList<Double>> savings1 = new ArrayList<ArrayList<Double>>();
                    for (int i = 0; i < this.nodesindex.size(); i++) {
                        ArrayList<Double> tmp = new ArrayList<Double>();
                        int c1 = this.nodesindex.get(i);
                        for (int j = 0; j < this.nodesindex.size(); j++) {
                            int c2 = this.nodesindex.get(j);
                            savings[c1][c2] = k2_2_0 * userparam.pre_related_rd[c1][c2] +
                                    k2_2_1 * userparam.pre_related_dis3[c1][c2];
                            tmp.add(savings[c1][c2]);
                        }
                        savings1.add(tmp);
                    }//for ij
                    ArrayList<Integer> CIndxList = new ArrayList<Integer>();

                    int cidx0 = 1 + StdRandom.uniform(this.nodenums);// nodesindex---idx
                    CIndxList.add(cidx0);

                    int curc = this.nodesindex.get(cidx0);//real customer-idx
                    if (curc != 0 && curc != userparam.nbclients + 1) {
                        removed_customer.add(curc);
                    }

                    while (removed_customer.size() < remove_customer_num) {
                        int cidx = CIndxList.get(StdRandom.uniform(CIndxList.size()));

                        int[] b1 = sort2idx(savings1.get(cidx));
                        int cidx1 = b1[StdRandom.uniform(b1.length)];//nodesindex---idx
                        int curc1 = this.nodesindex.get(cidx1);//real customer-idx
                        if (curc1 != 0 && curc1 != userparam.nbclients + 1) {
                            CIndxList.add(cidx1);
                            removed_customer.add(curc1);
                        }//???
                    }
                    bw1.write("\n Remove Customers by relations: ");
                    bw1.flush();
                    bw1.write("\n remove customer: " + removed_customer.toString());
                }//for if
            }//for level2

            //bw1.write("\n --- ---------- split line ----------  ---");bw1.flush();
            bw1.write("\n ");
            bw1.flush();
            // Repair
            ArrayList<Integer> to_repair = new ArrayList<Integer>();
            for (Integer c : removed_customer) {
                to_repair.add(c);
            }
            bw1.write("\n Start Repair ,to be repaired customer: " + to_repair.toString());
            bw1.flush();
            System.err.println("Currently removed ,to be repaired customer: " + to_repair.toString());

            double[] Prob = new double[3];
            Prob[0] = b0 / (b0 + b1 + b2);
            Prob[1] = b1 / (b0 + b1 + b2);
            Prob[2] = b2 / (b0 + b1 + b2);

            operation_r = Roulette_wheel_selection(Prob);
            for (Integer c : to_repair) {//TODO
                int c_r_idx = customer_tripidx.get(c);
                cur_trips.get(c_r_idx).assignedcustomer.remove(c);
                bw1.write("\n Remove customer: " + c + " old_trip= " + c_r_idx);
                bw1.flush();
            }

            if (operation_r == 0) {//random
                Collections.shuffle(to_repair);
                bw1.write("\n After shuffle ,to be repaired customer: " + to_repair.toString());
                bw1.flush();
                for (Integer c : to_repair) {
                    int c_r_idx = customer_tripidx.get(c);
                    int new_c_r_idx = -1;
                    if (trip_num >= 2) {
                        new_c_r_idx = StdRandom.uniform(trip_num);
                        while (new_c_r_idx == c_r_idx) {
                            new_c_r_idx = StdRandom.uniform(trip_num);
                        }
                        int new_pos = 1 + StdRandom.uniform(cur_trips.get(new_c_r_idx).assignedcustomer.size() - 1);
                        cur_trips.get(new_c_r_idx).assignedcustomer.add(new_pos, c);
                        bw1.write("\n Randomly insert current customer: " + c + " to new_trip= " + new_c_r_idx
                                + " new_pos= " + new_pos);
                        bw1.flush();
                    } else {
                    }
                }//for each c
            }//0
            if (operation_r == 1) {//least-cost
                //calculate_savings: HashMap<Integer,ArrayList<Integer>> c_r_pos
                double[][][] c_r_pos = new double[this.userparam.nbclients + 1][trip_num][this.userparam.nbclients + 1];
                for (int i = 0; i < this.userparam.nbclients + 1; i++) {
                    for (int j = 0; j < trip_num; j++) {
                        for (int k = 0; k < this.userparam.nbclients + 1; k++) {
                            c_r_pos[i][j][k] = Double.MAX_VALUE;
                        }
                    }
                }
                for (Integer c : to_repair) {
                    int c_r_idx = customer_tripidx.get(c);
                    for (int i = 0; i < trip_num; i++) {
                        for (int j = 1; j < cur_trips.get(i).assignedcustomer.size(); j++) {//new pos
                            if (c_r_idx != i) {
                                double st1 = 0.0;
                                double st2 = 0.0;
                                ArrayList<Integer> tmp1 = new ArrayList<Integer>();
                                ArrayList<Integer> tmp2 = new ArrayList<Integer>();
                                for (Integer e : cur_trips.get(i).assignedcustomer) {
                                    tmp1.add(e);
                                    st1 = Math.max(st1, this.userparam.nodes.get(e).RELEASE_DATE);
                                    tmp2.add(e);
                                    st2 = Math.max(st2, this.userparam.nodes.get(e).RELEASE_DATE);
                                }
                                //System.out.println(st1+","+st2+","+cur_trips.get(i).assignedcustomer.toString());
                                tmp2.add(j, c);
                                st2 = Math.max(st2, this.userparam.nodes.get(c).RELEASE_DATE);
                                tmp1.remove(tmp1.size() - 1);
                                tmp2.remove(tmp2.size() - 1);//???
                                double tt1 = cal_segment_traveltime(st1, tmp1);
                                double tt2 = cal_segment_traveltime(st2, tmp2);
                                c_r_pos[c][i][j] = tt2 - tt1;
                                //System.out.println(st1+","+tt1+","+tmp1.toString());
                                //System.out.println(st2+","+tt2+","+tmp2.toString());
                                //System.out.println(tt2+","+tt1+","+(tt2-tt1));
                            } else {
                                c_r_pos[c][i][j] = Double.MAX_VALUE;
                            }//TODO
                        }//j
                    }//i
                }
                for (Integer c : to_repair) {
                    //for(int i=0;i<trip_num;i++) {
                    //for(int j=1;j<cur_trips.get(i).assignedcustomer.size();j++) {
                    //bw1.write(c_r_pos[c][i][j]+",");
                    //}bw1.write("\n");}
                    ArrayList<Integer> location = cal_trip_pos(c_r_pos[c]);
                    int trip_i = location.get(0);
                    int pos_j = location.get(1);//TODO
                    cur_trips.get(trip_i).assignedcustomer.add(pos_j, c);
                    bw1.write("\n least-cost insert current customer: " + c + " to new_trip= "
                            + trip_i + " new_pos= " + pos_j);
                    bw1.flush();
                }
            }//least-cost

            bw2.write("\n ");
            bw2.write("\n Select level= " + whichlevel);
            if (whichlevel == 1) {
                if (operation_d1 == 0) {
                    bw2.write("\n Randomly Remove trips ");
                }
                if (operation_d1 == 1) {
                    bw2.write("\n Remove trips by savings");
                }
                if (operation_d1 == 2) {
                    bw2.write("\n Remove trips by relations");
                }
            }
            if (whichlevel == 2) {
                if (operation_d2 == 0) {
                    bw2.write("\n Randomly Remove Customers ");
                }
                if (operation_d2 == 1) {
                    bw2.write("\n Remove Customers by savings");
                }
                if (operation_d2 == 2) {
                    bw2.write("\n Remove Customers by relations");
                }
            }
            //bw1.write("\n --- ---------- split line ----------  ---");bw1.flush();
            bw1.write("\n ");
            bw1.flush();
            //adjust departime --- adjust sequence TODO
            ArrayList<trip> new_trips = sort_trip(cur_trips);
            double s1 = 0;
            for (trip r : new_trips) {
                r.cal_largest_releasetime();
                s1 = Math.max(s1, r.largest_releasetime);
                r.set_departime(s1);
                r.cal_traveltime(s1);
                double tt1 = r.traveltime;
                double f1 = s1 + tt1;
                r.set_completetime(f1);
                //r.set_sct();
                s1 = f1;
            }
            newsol = new route();
            for (trip r : new_trips) {
                newsol.add(r);
                r.display_trip(bw1);
            }
            newsol.cal_time();
            newsol.update_trip_maxH(userparam.B);
        }//for if flag

        return newsol;
    }// end of function

    public void adjust_dr_params(double delta, double delta1, boolean accept, int cnt) throws IOException {
        //levels
        if (greater(delta, 0)) {
            if (greater(delta1, 0)) {
                levels[whichlevel][cnt] = w1;
            } else {
                levels[whichlevel][cnt] = w2;
            }
        } else {
            if (accept) {
                levels[whichlevel][cnt] = w3;
            } else {
                levels[whichlevel][cnt] = w4;
            }
        }

        //bw2.write("\n levels-w-c= "+levels[whichlevel][cnt]);
        //removals repair
        if (greater(delta, 0)) {
            if (greater(delta1, 0)) {
                if (whichlevel == 0) {
                    removals[0][operation_d0][cnt] = w1;
                }
                if (whichlevel == 1) {
                    removals[1][operation_d1][cnt] = w1;
                }
                if (whichlevel == 2) {
                    removals[2][operation_d2][cnt] = w1;
                }
                repairs[operation_r][cnt] = w1;
            } else {
                if (whichlevel == 0) {
                    removals[0][operation_d0][cnt] = w2;
                }
                if (whichlevel == 1) {
                    removals[1][operation_d1][cnt] = w2;
                }
                if (whichlevel == 2) {
                    removals[2][operation_d2][cnt] = w2;
                }
            }
            repairs[operation_r][cnt] = w2;
        } else {
            if (accept) {
                if (whichlevel == 0) {
                    removals[0][operation_d0][cnt] = w3;
                }
                if (whichlevel == 1) {
                    removals[1][operation_d1][cnt] = w3;
                }
                if (whichlevel == 2) {
                    removals[2][operation_d2][cnt] = w3;
                }
                repairs[operation_r][cnt] = w3;
            } else {
                if (whichlevel == 0) {
                    removals[0][operation_d0][cnt] = w4;
                }
                if (whichlevel == 1) {
                    removals[1][operation_d1][cnt] = w4;
                }
                if (whichlevel == 2) {
                    removals[2][operation_d2][cnt] = w4;
                }
                repairs[operation_r][cnt] = w4;
            }
        }
        //how to update TODO sum
        if (cnt % phase == 0 && cnt >= phase) {
            level1 = level_lamda * level1 + (1 - level_lamda) * levels[0][cnt - 1];
            //prev=cal_prev(0,cnt);
            //level1=level_lamda*level1+(1-level_lamda)*prev;
            level2 = level_lamda * level2 + (1 - level_lamda) * levels[1][cnt - 1];
            level3 = level_lamda * level3 + (1 - level_lamda) * levels[2][cnt - 1];

            a0_0 = r_lamda * a0_0 + (1 - r_lamda) * removals[0][0][cnt - 1];

            a1_0 = r_lamda * a1_0 + (1 - r_lamda) * removals[1][0][cnt - 1];
            a1_1 = r_lamda * a1_1 + (1 - r_lamda) * removals[1][1][cnt - 1];
            a1_2 = r_lamda * a1_2 + (1 - r_lamda) * removals[1][2][cnt - 1];
            a2_0 = r_lamda * a2_0 + (1 - r_lamda) * removals[2][0][cnt - 1];
            a2_1 = r_lamda * a2_1 + (1 - r_lamda) * removals[2][1][cnt - 1];
            a2_2 = r_lamda * a2_2 + (1 - r_lamda) * removals[2][2][cnt - 1];


            b0 = d_lamda * b0 + (1 - d_lamda) * repairs[0][cnt - 1];
            b1 = d_lamda * b1 + (1 - d_lamda) * repairs[1][cnt - 1];
            b2 = d_lamda * b2 + (1 - d_lamda) * repairs[2][cnt - 1];


        }

        bw2.write("\n After updating , the parameters = ");
        bw2.write("\n level= " + whichlevel + " operation_d0= " + operation_d0 + " operation_d1= " + operation_d1
                + " operation_d2= " + operation_d2);
        bw2.write("\n w1 = " + this.w1 + " w2 = " + this.w2 + " w3 = " + this.w3 + " w4 = " + this.w4);
        bw2.write("\n Remove_trip num = " + this.remove_trip_num + " Remove_customer num = " + this.remove_customer_num);
        bw2.write("\n level1 = " + this.level1 + " level2 = " + this.level2 + " level3 = " + this.level3);
        bw2.write("\n select trip a1_0 random = " + this.a1_0 + " trip a1_1 saving = " + this.a1_1 +
                " trip a1_2 related = " + this.a1_2);
        bw2.write("\n select node a2_0 random = " + this.a2_0 + " node a2_1 saving = " + this.a2_1 +
                " node a2_2 related = " + this.a2_2);
        bw2.write("\n repair node b0 random = " + this.b0 + " node b1 saving = " + this.b1 +
                " node b2 related = " + this.b2);
        bw2.write("\n save time k2_1_0 = " + this.k2_1_0 + " dis k2_1_1 saving = " + this.k2_1_1 +
                " rd k2_2_0 related = " + this.k2_2_0 + " dis k2_2_1 related = " + this.k2_2_1);
        bw2.flush();

    }

    public void adjust_ls_params(double delta_ls) {
        //
        if (greater(delta_ls, 0)) {
            if (this.whichoperator == 0) {
                this.ls_op1 += ls_w;
            }
            if (this.whichoperator == 1) {
                this.ls_op2 += ls_w;
            }
            if (this.whichoperator == 2) {
                this.ls_op3 += ls_w;
            }
            if (this.whichoperator == 3) {
                this.ls_op4 += ls_w;
            }
            if (this.whichoperator == 4) {
                this.ls_op5 += ls_w;
            }
        } else {
            if (this.whichoperator == 0) {
                this.ls_op1 -= ls_w;
            }
            if (this.whichoperator == 1) {
                this.ls_op2 -= ls_w;
            }
            if (this.whichoperator == 2) {
                this.ls_op3 -= ls_w;
            }
            if (this.whichoperator == 3) {
                this.ls_op4 -= ls_w;
            }
            if (this.whichoperator == 4) {
                this.ls_op5 -= ls_w;
            }
        }
        this.ls_op1 = Math.max(0, this.ls_op1);
        this.ls_op2 = Math.max(0, this.ls_op2);
        this.ls_op3 = Math.max(0, this.ls_op3);
        this.ls_op4 = Math.max(0, this.ls_op4);
        this.ls_op5 = Math.max(0, this.ls_op5);
    }

    public route local_search(int maxcntls, route cursol) throws IOException, IloException {
        // intra
        // inter
        // depot removal
        // depot insert
        // depot shift
        bw1.write("\n Start Local_Search: ");
        bw1.flush();
        ArrayList<trip> cur_trips = new ArrayList<trip>();//copy trips
        boolean preflag = false;
        //bw1.write("\n COPY Process: ");bw1.flush();
        for (trip r : cursol.trips) {
            if (r.assignedcustomer.size() >= 3) {
                preflag = true;
            }
            trip tmpr = r.tripcopy();
            //tmpr.display_trip(bw1);
            cur_trips.add(tmpr);
        }//initial
        int trip_num = cur_trips.size();
        route newsol = new route();
        route ls_sol = new route();
        for (trip r : cur_trips) {
            newsol.add(r);
            //r.display_trip(bw1);
        }
        newsol.cal_time();


        int itercnt = 0;
        while (itercnt < maxcntls && preflag) {//
            //LS operator --- Roulette wheel selection
            double[] Probability = new double[5];
            Probability[0] = ls_op1 / (ls_op1 + ls_op2 + ls_op3 + ls_op4 + ls_op5);
            Probability[1] = ls_op2 / (ls_op1 + ls_op2 + ls_op3 + ls_op4 + ls_op5);
            Probability[2] = ls_op3 / (ls_op1 + ls_op2 + ls_op3 + ls_op4 + ls_op5);
            Probability[3] = ls_op4 / (ls_op1 + ls_op2 + ls_op3 + ls_op4 + ls_op5);
            Probability[4] = ls_op5 / (ls_op1 + ls_op2 + ls_op3 + ls_op4 + ls_op5);
            whichoperator = Roulette_wheel_selection(Probability);
            bw1.write("\n Ls Iter= " + itercnt + " which_LS_operator= " + whichoperator);
            bw1.flush();
            if (whichoperator == 0) {//ls_op1=0;
                ls_sol = this.inter_change(newsol);
            }
            if (whichoperator == 1) {//ls_op1=0;
                ls_sol = this.intra_change(newsol);
            }
            if (whichoperator == 2) {//ls_op1=0;
                ls_sol = this.depot_insertion(newsol);
            }
            if (whichoperator == 3) {//ls_op1=0;
                ls_sol = this.depot_removal(newsol);
            }
            if (whichoperator == 4) {//ls_op1=0;
                ls_sol = this.depot_shift(newsol);
            }
            double old_ct = newsol.last_completetime;
            double cur_ct = ls_sol.last_completetime;
            double old_sct = newsol.cur_SCT;
            double cur_sct = ls_sol.cur_SCT;
            double old_h = newsol.cur_H;
            double cur_h = ls_sol.cur_H;

            double deltalsh = ls_sol.cur_H - newsol.cur_H;
            if (greater(deltalsh, 0)) {
                bw1.write("\n Find ls_route: old-ct=" + old_ct + " new-ct=" + cur_ct);
                bw1.write("\n Find better ls_route1: old-sct=" + old_sct + " new-ct=" + cur_sct);
                bw1.write("\n Find better ls_route1: old-h=" + old_h + " new-h=" + cur_h);
                newsol = ls_sol.routecopy();
            } else {
                bw1.write("\n ls_route: old-ct=" + old_ct + " new-ct=" + cur_ct);
                bw1.write("\n No improve ls_route1: old-sct=" + old_sct + " new-sct=" + cur_sct);
                bw1.write("\n No improve ls_route1: old-h=" + old_h + " new-h=" + cur_h);
            }
            this.adjust_ls_params(deltalsh);
            itercnt += 1;
        }
        bw1.write("\n Finish Local_Search: ");
        bw1.flush();
        return newsol;
    }

    public route intra_change(route cursol) throws IloException, IOException {
        //rd-departure time
        bw1.write("\n Local search to intra_change: ");
        bw1.flush();
        route newsol = new route();
        boolean preflag = false;
        boolean flag = false;
        ArrayList<trip> cur_trips = new ArrayList<trip>();//copy trips
        for (trip r : cursol.trips) {
            if (r.assignedcustomer.size() >= 3) {
                preflag = true;
            }
            trip tmpr = r.tripcopy();
            cur_trips.add(tmpr);
        }

        int trip_num = cur_trips.size();
        for (int tidx = 0; tidx < trip_num && !flag; tidx++) {

            if (cur_trips.get(tidx).assignedcustomer.size() <= 3) {
            } else {
                bw1.write("\n current pick up " + tidx + "-th trip");
                bw1.flush();
                trip picktrip = cur_trips.get(tidx);
                double st = picktrip.departime;
                double ct = picktrip.completetime;
                double sct = picktrip.sct;
                for (int i = 1; i < picktrip.assignedcustomer.size() - 1; i++) {
                    for (int j = i + 1; j < picktrip.assignedcustomer.size() - 1; j++) {
                        int element1 = picktrip.assignedcustomer.get(i);
                        int element2 = picktrip.assignedcustomer.get(j);
                        ArrayList<Integer> curc = picktrip.customercopy();
                        curc.set(i, element2);
                        curc.set(j, element1);
                        double ct1 = st + this.cal_segment_traveltime(st, curc);

                        ArrayList<Integer> curc1 = picktrip.customercopy();
                        curc1.set(i, element2);
                        curc1.set(j, element1);
                        curc1.remove(curc1.size() - 1);

                        double sct1 = st + this.cal_segment_traveltime(st, curc1);
                        //if(greater(ct-ct1,0)) {
                        if (greater(sct - sct1, 0)) {
                            bw1.write("\n find better trip seq,change node " + i + "," + j +
                                    " old-ct= " + ct + " new-ct= " + ct1);
                            bw1.flush();
                            picktrip.setconsumer(curc);
                            picktrip.set_completetime(ct1);
                            picktrip.set_traveltime(ct1 - st);
                            picktrip.set_sct(sct1);
                            sct = sct1;
                            ct = ct1;
                            flag = true;
                            break;
                        }//
                    }
                }
            }
        }

        bw1.write("\n display intra route: ");
        bw1.flush();
        newsol = new route();
        for (trip r : cur_trips) {
            newsol.add(r);
        }
        newsol.update_trip_minc();
        newsol.cal_time();
        newsol.update_trip_maxH(userparam.B);

        for (trip r : newsol.trips) {
            r.display_trip(bw1);
        }
        return newsol;

    }

    public route inter_change(route cursol) throws IloException, IOException {
        // departure time change
        double ost = cursol.cur_H;
        bw1.write("\n Local search to inter_change: ");
        bw1.flush();
        route newsol = new route();
        boolean preflag = false;
        boolean flag = false;
        ArrayList<trip> cur_trips = new ArrayList<trip>();//copy trips
        //ArrayList<trip> cur_trips1=new ArrayList<trip>();//copy trips
        for (trip r : cursol.trips) {
            if (r.assignedcustomer.size() >= 3) {
                preflag = true;
            }
            trip tmpr = r.tripcopy();
            cur_trips.add(tmpr);
        }
        //trip tmpr1=r.tripcopy();
        //cur_trips1.add(tmpr1);}

        int trip_num = cur_trips.size();
        for (int tidx1 = 0; tidx1 < trip_num && !flag; tidx1++) {
            for (int tidx2 = tidx1 + 1; tidx2 < trip_num && !flag; tidx2++) {
                bw1.write("\n current pick up " + tidx1 + "-th trip " + tidx2 + " -th trip");
                bw1.flush();
                trip picktrip1 = cur_trips.get(tidx1);
                trip picktrip2 = cur_trips.get(tidx2);
                trip npicktrip1 = picktrip1.tripcopy();
                trip npicktrip2 = picktrip2.tripcopy();
                for (int i = 1; i < picktrip1.assignedcustomer.size() - 1 && !flag; i++) {
                    for (int j = 1; j < picktrip2.assignedcustomer.size() - 1 && !flag; j++) {
                        int element1 = picktrip1.assignedcustomer.get(i);
                        int element2 = picktrip2.assignedcustomer.get(j);
                        ArrayList<Integer> curc1 = picktrip1.customercopy();
                        ArrayList<Integer> curc2 = picktrip2.customercopy();
                        curc1.set(i, element2);
                        curc2.set(j, element1);//
                        double st1 = this.maxrd1(curc1);
                        double ct1 = st1 + this.cal_segment_traveltime(st1, curc1);
                        double st2 = this.maxrd1(curc2);
                        double ct2 = st2 + this.cal_segment_traveltime(st2, curc2);
                        npicktrip1.setconsumer(curc1);
                        npicktrip1.set_completetime(ct1);
                        npicktrip1.set_traveltime(ct1 - st1);
                        npicktrip2.setconsumer(curc2);
                        npicktrip2.set_completetime(ct2);
                        npicktrip2.set_traveltime(ct2 - st2);
                        cur_trips.set(tidx1, npicktrip2);
                        cur_trips.set(tidx2, npicktrip1);
                        route nroute = new route();
                        nroute.set_trips(cur_trips);
                        nroute.update_trip_minc();
                        nroute.cal_time();
                        nroute.update_trip_maxH(userparam.B);
                        //double nct = nroute.last_completetime;
                        //double nst = nroute.cur_SCT;
                        double nst = nroute.cur_H;
                        if (greater(ost - nst, 0)) {
                            bw1.write("\n find better trip seq,change trip " + tidx1 + "," + tidx2 +
                                    " node " + element1 + "," + element2 + " old-h= " + ost + " new-h= " + nst);

                            ost = nst;
                            newsol = nroute;
                            flag = true;
                            break;
                        }
                    }
                }//for i,j


            }
        }//for t1,t2
        if (flag) {
            bw1.write("\n Imporved:display inter route: ");
            bw1.flush();
            newsol.update_trip_minc();
            newsol.cal_time();
            newsol.update_trip_maxH(userparam.B);

            for (trip r : newsol.trips) {
                r.display_trip(bw1);
            }
        } else {
            bw1.write("\n No Imporved:display inter route: ");
            bw1.flush();
            newsol = new route();
            for (trip r : cur_trips) {
                newsol.add(r);
                r.display_trip(bw1);
            }
            newsol.update_trip_minc();
            newsol.cal_time();
            newsol.update_trip_maxH(userparam.B);
        }
        return newsol;
    }

    public route depot_insertion(route cursol) throws IloException, IOException {
        // departure time
        //double oct = cursol.last_completetime;
        double ost = cursol.cur_H;
        bw1.write("\n Local search to depot_insertion: ");
        bw1.flush();
        route newsol = new route();
        boolean preflag = false;
        boolean flag = false;
        ArrayList<trip> cur_trips = new ArrayList<trip>();//copy trips
        for (trip r : cursol.trips) {
            if (r.assignedcustomer.size() >= 3) {
                preflag = true;
            }
            trip tmpr = r.tripcopy();
            cur_trips.add(tmpr);
        }

        int trip_num = cur_trips.size();
        for (int tidx1 = 0; tidx1 < trip_num && !flag; tidx1++) {
            trip picktrip1 = cur_trips.get(tidx1);//break to 2 trip
            if (picktrip1.assignedcustomer.size() >= 4) {
                for (int ci1 = 2; ci1 < picktrip1.assignedcustomer.size() - 1 && !flag; ci1++) {
                    trip t1 = picktrip1.tripcopy();
                    trip t2 = picktrip1.tripcopy();
                    ArrayList<Integer> tmp1 = new ArrayList<Integer>();
                    ArrayList<Integer> tmp2 = new ArrayList<Integer>();
                    tmp2.add(0);
                    for (int i1 = 0; i1 < ci1; i1++) {
                        tmp1.add(picktrip1.assignedcustomer.get(i1));
                    }
                    tmp1.add(this.userparam.nbclients + 1);
                    for (int i1 = ci1; i1 < picktrip1.assignedcustomer.size(); i1++) {
                        tmp2.add(picktrip1.assignedcustomer.get(i1));
                    }
                    t1.setconsumer(tmp1);//
                    t2.setconsumer(tmp2);


                    ArrayList<trip> cur_trips1 = new ArrayList<trip>();//copy trips
                    for (trip tr : cur_trips) {
                        cur_trips1.add(tr.tripcopy());
                    }
                    cur_trips1.add(tidx1, t1);
                    cur_trips1.set(tidx1 + 1, t2);

                    route nroute = new route();
                    nroute.set_trips(cur_trips1);
                    nroute.update_trip_minc();
                    nroute.update_trip_maxH(userparam.H);
                    nroute.cal_time();
                    //double nct = nroute.last_completetime;
                    double nst = nroute.cur_H;

                    bw1.write("\n depot_insert route: ");
                    bw1.flush();
                    bw1.write("\n current split trip " + tidx1 + " to 2 trips"
                            + " old-h= " + ost + " new-h= " + nst);
                    if (greater(ost - nst, 0)) {
                        bw1.write("\n find better trip,split trip " + tidx1 + " to 2 trips"
                                + " old-h= " + ost + " new-h= " + nst);
                        ost = nst;
                        newsol = nroute;
                        flag = true;
                        break;
                    }

                }
            }
        }

        if (flag) {
            bw1.write("\n Imporved:display depot_insert route: ");
            bw1.flush();
            newsol.update_trip_minc();
            newsol.cal_time();
            newsol.update_trip_maxH(userparam.H);
            for (trip r : newsol.trips) {
                r.display_trip(bw1);
            }
        } else {
            bw1.write("\n No Imporved:display depot_insert route: ");
            bw1.flush();
            newsol = new route();
            for (trip r : cur_trips) {
                newsol.add(r);
                r.display_trip(bw1);
            }
            newsol.update_trip_minc();
            newsol.cal_time();
            newsol.update_trip_maxH(userparam.H);
        }
        return newsol;
    }

    public route depot_removal(route cursol) throws IloException, IOException {
        //departure time 2
        double ost = cursol.cur_H;
        bw1.write("\n Local search to depot_removal: ");
        bw1.flush();
        route newsol = new route();
        boolean preflag = false;
        boolean flag = false;
        ArrayList<trip> cur_trips = new ArrayList<trip>();//copy trips
        for (trip r : cursol.trips) {
            if (r.assignedcustomer.size() >= 3) {
                preflag = true;
            }
            trip tmpr = r.tripcopy();
            cur_trips.add(tmpr);
        }

        int trip_num = cur_trips.size();
        for (int tidx1 = 0; tidx1 < trip_num && !flag; tidx1++) {
            for (int tidx2 = tidx1 + 1; tidx2 < trip_num && !flag; tidx2++) {
                bw1.write("\n current pick up " + tidx1 + "-th trip " + tidx2 + "-th trip");
                bw1.flush();
                trip picktrip1 = cur_trips.get(tidx1);
                trip picktrip2 = cur_trips.get(tidx2);
                trip npicktrip1 = picktrip1.tripcopy();
                trip npicktrip2 = picktrip2.tripcopy();

                ArrayList<Integer> tmp1 = new ArrayList<Integer>();
                ArrayList<Integer> tmp2 = new ArrayList<Integer>();
                tmp2.add(0);
                tmp2.add(this.userparam.nbclients + 1);
                for (int i1 = 0; i1 < picktrip1.assignedcustomer.size() - 1; i1++) {
                    tmp1.add(picktrip1.assignedcustomer.get(i1));
                }
                for (int i2 = 1; i2 < picktrip2.assignedcustomer.size(); i2++) {
                    tmp1.add(picktrip2.assignedcustomer.get(i2));
                }
                npicktrip1.setconsumer(tmp1);
                npicktrip2.setconsumer(tmp2);
                ArrayList<trip> cur_trips1 = new ArrayList<trip>();//copy trips
                for (trip tr : cur_trips) {
                    cur_trips1.add(tr.tripcopy());
                }
                cur_trips1.set(tidx1, npicktrip1);
                cur_trips1.set(tidx2, npicktrip2);

                route nroute = new route();//
                ArrayList<trip> new_trips = sort_trip(cur_trips1);
                double s1 = 0;
                for (trip r : new_trips) {
                    r.cal_largest_releasetime();
                    s1 = Math.max(s1, r.largest_releasetime);
                    r.set_departime(s1);
                    r.cal_traveltime(s1);
                    double tt1 = r.traveltime;
                    double f1 = s1 + tt1;
                    r.set_completetime(f1);
                    s1 = f1;
                }
                nroute = new route();
                for (trip r : new_trips) {
                    nroute.add(r);
                    r.display_trip(bw1);
                }
                nroute.cal_time();
                nroute.update_trip_maxH(userparam.B);
                double nst = nroute.cur_H;
                bw1.write("\n depot_removal route: ");
                bw1.flush();

                if (greater(ost - nst, 0)) {
                    //if(greater(oct-nct,0)) {
                    bw1.write("\n find better trips,merge trip " + tidx1 + "," + tidx2 + " to 1 trip"
                            + " old-h= " + ost + " new-h= " + nst);
                    ost = nst;
                    newsol = nroute;
                    flag = true;
                    break;
                }


            }
        }
        if (flag) {
            bw1.write("\n Imporved:display depot_removal route: ");
            bw1.flush();
            newsol.update_trip_minc();
            newsol.cal_time();
            newsol.update_trip_maxH(userparam.B);

            for (trip r : newsol.trips) {
                r.display_trip(bw1);
            }
        } else {
            bw1.write("\n No Imporved:display depot_removal route: ");
            bw1.flush();
            newsol = new route();
            for (trip r : cur_trips) {
                newsol.add(r);
                r.display_trip(bw1);
            }
            newsol.update_trip_minc();
            newsol.cal_time();
            newsol.update_trip_maxH(userparam.B);
        }
        return newsol;//
    }

    public route depot_shift(route cursol) throws IloException, IOException {
        double ost = cursol.cur_H;
        bw1.write("\n Local search to depot_shift: ");
        bw1.flush();
        route newsol = new route();
        boolean preflag = false;
        boolean flag = false;
        ArrayList<trip> cur_trips = new ArrayList<trip>();//copy trips
        for (trip r : cursol.trips) {
            if (r.assignedcustomer.size() >= 3) {
                preflag = true;
            }
            trip tmpr = r.tripcopy();
            cur_trips.add(tmpr);
        }

        int trip_num = cur_trips.size();
        for (int tidx1 = 0; tidx1 < trip_num && !flag; tidx1++) {
            for (int tidx2 = tidx1 + 1; tidx2 < trip_num && !flag; tidx2++) {
                bw1.write("\n first pick up " + tidx1 + "-th trip " + tidx2 + " -th trip");
                bw1.flush();
                trip picktrip1 = cur_trips.get(tidx1);
                trip picktrip2 = cur_trips.get(tidx2);
                trip npicktrip1 = picktrip1.tripcopy();
                trip npicktrip2 = picktrip2.tripcopy();
                int len1 = picktrip1.assignedcustomer.size() - 2;
                int len2 = picktrip2.assignedcustomer.size() - 2;

                ArrayList<Integer> tmp0 = new ArrayList<Integer>();
                for (int i1 = 1; i1 < picktrip1.assignedcustomer.size() - 1; i1++) {
                    tmp0.add(picktrip1.assignedcustomer.get(i1));
                }
                for (int i2 = 1; i2 < picktrip2.assignedcustomer.size() - 1; i2++) {
                    tmp0.add(picktrip2.assignedcustomer.get(i2));
                }
                //Collections.shuffle(tmp0);//
                for (int ci1 = 1; ci1 < tmp0.size() && !flag; ci1++) {
                    ArrayList<Integer> tmp1 = new ArrayList<Integer>();
                    ArrayList<Integer> tmp2 = new ArrayList<Integer>();
                    tmp1.add(0);
                    tmp2.add(0);
                    for (int i3 = 0; i3 < ci1; i3++) {
                        tmp1.add(tmp0.get(i3));
                    }
                    for (int i3 = ci1; i3 < tmp0.size(); i3++) {
                        tmp2.add(tmp0.get(i3));
                    }
                    tmp1.add(this.userparam.nbclients + 1);
                    tmp2.add(this.userparam.nbclients + 1);

                    npicktrip1.setconsumer(tmp1);
                    npicktrip2.setconsumer(tmp2);
                    ArrayList<trip> cur_trips1 = new ArrayList<trip>();//copy trips
                    for (trip tr : cur_trips) {
                        cur_trips1.add(tr.tripcopy());
                    }
                    cur_trips1.set(tidx1, npicktrip1);
                    cur_trips1.set(tidx2, npicktrip2);

                    route nroute = new route();
                    ArrayList<trip> new_trips = sort_trip(cur_trips1);
                    double s1 = 0;
                    for (trip r : new_trips) {
                        r.cal_largest_releasetime();
                        s1 = Math.max(s1, r.largest_releasetime);
                        r.set_departime(s1);
                        r.cal_traveltime(s1);
                        double tt1 = r.traveltime;
                        double f1 = s1 + tt1;
                        r.set_completetime(f1);
                        s1 = f1;
                    }
                    nroute = new route();
                    for (trip r : new_trips) {
                        nroute.add(r);
                        r.display_trip(bw1);
                    }
                    nroute.cal_time();
                    nroute.update_trip_maxH(userparam.B);
                    double nst = nroute.cur_SCT;
                    bw1.write("\n depot_shift route: ");
                    bw1.flush();
                    bw1.write("\n current shift trip " + tidx1 + "," + tidx2 + " to 1 trip"
                            + " old-h= " + ost + " new-h= " + nst);
                    if (greater(ost - nst, 0)) {
                        //if(greater(oct-nct,0)) {
                        bw1.write("\n find better trips,shift trip " + tidx1 + "," + tidx2 + " to 1 trip"
                                + " old-h= " + ost + " new-h= " + nst);
                        ost = nst;
                        newsol = nroute;
                        flag = true;
                        break;
                    }
                }
            }
        }
        if (flag) {
            bw1.write("\n Imporved:display depot_shift route: ");
            bw1.flush();
            newsol.update_trip_minc();
            newsol.cal_time();
            newsol.update_trip_maxH(userparam.B);
            for (trip r : newsol.trips) {
                r.display_trip(bw1);
            }
        } else {
            bw1.write("\n No Imporved:display depot_shift route: ");
            bw1.flush();
            newsol = new route();
            for (trip r : cur_trips) {
                newsol.add(r);
                r.display_trip(bw1);
            }
            newsol.update_trip_minc();
            newsol.cal_time();
            newsol.update_trip_maxH(userparam.B);
        }
        return newsol;//

    }

    //--- helper function ---//
    public double cal_node_traveltime(int v, int w, double st) {
        //System.out.println("start= "+st+" seg = "+d_ij);
        //assert false;
        double tt = -1;
        int cnt = 0;
        TBP tmp = userparam.time_breakpoint[v][w];
        if (tmp.right.size() == 0) {
            return 1000000;
        }
        if (st > tmp.right.get(tmp.right.size() - 1)) {
            cnt = tmp.right.size() - 1;
        } else {
            for (int i = 0; i < tmp.left.size(); i++) {
                if (st >= tmp.left.get(i) && st <= tmp.right.get(i)) {
                    cnt = i;
                    break;
                }
            }
        }
        //System.out.println("v= "+v+" w= "+w+" inter= "+cnt+" k= "+tmp.intercept.get(cnt)+" b= "+tmp.slope.get(cnt));
        tt = tmp.slope.get(cnt) * st + tmp.intercept.get(cnt);
        return tt;
    }

    public double cal_segment_traveltime(double st, ArrayList<Integer> seg) {
        //System.out.println("start= "+st+" seg = "+seg.toString());
        //assert false;
        double st1 = st;
        double tt = 0;
        double tt1 = 0;
        for (int i = 0; i < seg.size() - 1; i++) {
            int v = seg.get(i);
            int w = seg.get(i + 1);
            tt = cal_node_traveltime(v, w, st1);
            tt1 += tt;
            st1 += tt;
        }
        return tt1;
    }

    public int Roulette_wheel_selection(double[] Prob) {
        double m = StdRandom.uniform();
        double Probability_Total = 0;
        int ans = 0;
        for (int i = 0; i < Prob.length; i++) {
            Probability_Total += Prob[i];
            if (Probability_Total >= m) {
                ans = i;
                break;
            }
        }
        return ans;
    }

    public ArrayList<Integer> sort_aorder1(ArrayList<aorder> a1) {
        ArrayList<Integer> b = new ArrayList<Integer>();
        ArrayList<aorder> a = new ArrayList<aorder>();
        for (aorder e : a1) {
            a.add(e);
        }
        int N = a.size();
        for (int j = 0; j < N; j++) {
            for (int i = 0; i < N - j - 1; i++) {
                if (a.get(i).value1 < a.get(i + 1).value1) {
                    double t1 = a.get(i).value1;
                    a.get(i).value1 = a.get(i + 1).value1;
                    a.get(i + 1).value1 = t1;
                    int t2 = a.get(i).subscript;
                    a.get(i).subscript = a.get(i + 1).subscript;
                    a.get(i + 1).subscript = t2;
                }
            }
        }

        for (int i = 0; i < N; i++) {
            b.add(a.get(i).subscript);
        }
        return b;
    }

    public int[] sort2idx(ArrayList<Double> arrayList) {
        int k = arrayList.size();
        double[] arr = new double[k];
        for (int i = 0; i < k; i++) {
            arr[i] = arrayList.get(i);
        }
        double temp;
        int index;

        int[] Index = new int[k];
        for (int i = 0; i < k; i++) {
            Index[i] = i;
        }

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j] < arr[j + 1]) {
                    temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;

                    index = Index[j];
                    Index[j] = Index[j + 1];
                    Index[j + 1] = index;
                }
            }
        }
        return Index;
    }

    public int[] sort2idx1(double[] related_trip) {
        int k = related_trip.length;
        double[] arr = new double[k];
        for (int i = 0; i < k; i++) {
            arr[i] = related_trip[i];
        }
        double temp;
        int index;

        int[] Index = new int[k];
        for (int i = 0; i < k; i++) {
            Index[i] = i;
        }

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;

                    index = Index[j];
                    Index[j] = Index[j + 1];
                    Index[j + 1] = index;
                }
            }
        }
        return Index;
    }

    public ArrayList<trip> sort_trip(ArrayList<trip> atrips) throws IOException {
        ArrayList<trip> tmp_trips = new ArrayList<trip>();
        for (trip r : atrips) {
            tmp_trips.add(r);
        }

        ArrayList<trip> btrips = new ArrayList<trip>();

        double[] minrd = new double[tmp_trips.size()];
        int i = 0;
        for (trip r : tmp_trips) {
            r.cal_largest_releasetime();
            if (r.assignedcustomer.size() >= 3) {
                minrd[i] = r.largest_releasetime;
            } else {
                minrd[i] = Double.MAX_VALUE;
            }
            i += 1;
        }
        bw1.write("\n minrd= ");
        for (int i1 = 0; i1 < minrd.length; i1++) {
            bw1.write(minrd[i1] + ",");
            bw1.flush();
        }

        int[] k = sort2idx1(minrd);
        bw1.write("\n sequence= ");
        for (int i1 = 0; i1 < k.length; i1++) {
            bw1.write(k[i1] + ",");
            bw1.flush();
        }
        for (int j = 0; j < k.length; j++) {
            btrips.add(tmp_trips.get(k[j]));
        }
        return btrips;
    }

    public ArrayList<Integer> cal_trip_pos(double[][] saving1) {
        int a = -1;
        int b = -1;
        double mincost = Double.MAX_VALUE;
        for (int i = 0; i < saving1.length; i++) {
            for (int j = 0; j < saving1[0].length; j++) {
                if (saving1[i][j] < mincost) {
                    mincost = saving1[i][j];
                    a = i;
                    b = j;
                    //System.out.println(a+","+b+","+mincost);
                }
            }
        }
        ArrayList<Integer> ans = new ArrayList<Integer>();
        ans.add(a);
        ans.add(b);
        return ans;
        //c_r_pos[c]
    }

    public boolean greater(double a, double b) {
        if (a - b > 1E-6) {
            return true;
        } else {
            return false;
        }
    }

    public double maxrd(ArrayList<Node> tmpnode) {
        double maxrd = 0;
        for (Node n : tmpnode) {
            maxrd = Math.max(n.RELEASE_DATE, maxrd);
        }
        return maxrd;
    }

    public double maxrd1(ArrayList<Integer> curc) {
        ArrayList<Node> tmpnode = new ArrayList<Node>();
        for (Integer e : curc) {
            tmpnode.add(userparam.nodes.get(e));
        }
        double maxrd = 0;
        for (Node n : tmpnode) {
            maxrd = Math.max(n.RELEASE_DATE, maxrd);
        }
        return maxrd;
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


    public static void main(String[] args) throws Exception {

        String pre_fix = "STUDYCASE";
        String c = " ";
        String c1 = " ";
        String egdefile = "";
        boolean isdynamic = false;
        boolean isexport = true;

        c = "A20/CaseData20_5_600.txt";
        c1 = "CaseData20_5_600.txt";
        egdefile = "./dataset/" + pre_fix + "/inputdata/A20/edge_cat.txt";

        String objf = "minc";
        objf = "maxorderfulliment";

        double speed_Origin = 100;//m/min
        int region_num = 3;
        double time_horizon = 960;//更改整个规划时间 3
        int K = 1;
        int kc = 3;
        int didx = 2;//区域 4

        int DDL = 600;
        int SCT = 400;//更改DDL,SCT	 5
        int cnum = 21;//更改顾客数目 6

        isdynamic = true;
        boolean servet = false;

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
        String vfile = "./dataset/" + pre_fix + "/cplexResult/timeinformation/real_ArriveTime_" + Integer.toString(DDL) + "_" + Integer.toString(kc) + "_" + c.substring(4);//arrive time
        String vfile1 = "./dataset/" + pre_fix + "/cplexResult/timeinformation/real_TravelTime_" + Integer.toString(DDL) + "_" + Integer.toString(kc) + "_" + c.substring(4);//travel time
        String bpfile = "./dataset/" + pre_fix + "/cplexResult/timeinformation/breakpoints_" + Integer.toString(DDL) + "_" + Integer.toString(kc) + "_" + c.substring(4);//travel time

        paramSDD_case sd = new paramSDD_case(speed_Origin);
        sd.initParams(input_datafile, infor_file,
                K, time_horizon, DDL, SCT, isdynamic, region_num, partation, servet);

        if (isdynamic) {
            System.out.println("Cal BP");
            sd.cal_BreakPoint(speed, bpfile, vfile, vfile1, edge_cat, 1);
        }
        sd.nodeset.forEach(a -> System.out.println(a.toString()));

        //ALNS
        int maxcnt = 1;//total iteration
        int maxcnt1 = 0;//destroy and repair
        int maxcntL = 1;//local
        int intt = 3;//split to 3 trips

        String file1 = "./dataset/" + pre_fix + "/H1Result/result1_region" + Integer.toString(didx) + "_" + objf + "_" + c1;
        String file2 = "./dataset/" + pre_fix + "/H1Result/result2_region" + Integer.toString(didx) + "_" + objf + "_" + c1;

        ArrayList<Integer> paramset1 = new ArrayList<Integer>();
        ArrayList<Double> paramset2 = new ArrayList<Double>();
        ArrayList<Double> paramset3 = new ArrayList<Double>();
        ArrayList<Double> paramset4 = new ArrayList<Double>();
        ArrayList<Double> paramset5 = new ArrayList<Double>();
        ArrayList<Double> paramsetw = new ArrayList<Double>();
        ArrayList<Double> paramset6 = new ArrayList<Double>();
        ArrayList<Double> paramsetls = new ArrayList<Double>();

        paramset1.add(intt);//split to  trips
        paramset1.add(1);//remove_trip_num done
        paramset1.add(2);//remove_customer_num done

        paramset2.add(0.0);//level1=paramset2.get(0);// regions
        paramset2.add(0.3);//level2=paramset2.get(1);// trip_num done
        paramset2.add(0.7);//level3=paramset2.get(2);// customer_num done

        paramset3.add(0.2);//ѡtrip a1_0 random done
        paramset3.add(0.5);//ѡtrip a1_1 saving done
        paramset3.add(0.3);//ѡtrip a1_2 related done

        paramset3.add(0.1);//ѡnode a2_0 random done
        paramset3.add(0.5);//ѡnode a2_1 saving done
        paramset3.add(0.4);//ѡnode a2_2 related done

        paramset3.add(0.65);//b0 done
        paramset3.add(0.35);//b1 done
        paramset3.add(0.0);//b2 todo

        paramset4.add(0.5);//k2_1_0=paramset4.get(1);
        paramset4.add(0.5);//k2_1_1=paramset4.get(2);

        paramset4.add(0.5);//k2_2_0=paramset4.get(3);
        paramset4.add(0.5);//k2_2_1=paramset4.get(4);

        paramset5.add(120.0);
        paramset5.add(0.9);

        paramsetw.add(0.7);
        paramsetw.add(0.5);
        paramsetw.add(0.3);
        paramsetw.add(0.1);

        paramset6.add(0.4);
        paramset6.add(0.4);
        paramset6.add(0.4);

        paramsetls.add(0.5);//inter
        paramsetls.add(0.3);//intra
        paramsetls.add(0.0);//insert
        paramsetls.add(0.2);//remove
        paramsetls.add(0.0);//shift

        int phase = 1;
        double ls_w = 0.1;
        String readytimefile = "./dataset/" + pre_fix + "/validation/timeinformation/readytime.txt";

        List<Integer> cnode = sd.nodeset.get(didx);
        HeurH1 heu1 = new HeurH1(cnode, pre_fix, file1, file2, readytimefile, sd,
                paramset1, paramset2, paramset3, paramset4,
                paramset5, paramsetw, paramset6, paramsetls, ls_w, phase);

        heu1.ALNS(maxcnt, maxcnt1, maxcntL);


    }


}



