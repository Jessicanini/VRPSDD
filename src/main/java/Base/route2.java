package Base;


import java.util.List;

import ilog.concert.IloException;
import util.StdDraw;
import util.aorder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class route2 {
    public int index = -1;

    public int vehindex = -1;
    //public int cindex= -1;

    public double departime = -1;
    public double traveltime = -1;
    public double completetime = -1;

    public double largest_releasetime = -1;
    public double tspdis = 0;

    public ArrayList<Integer> assignedcustomer = new ArrayList<Integer>();

    public route2() {
    }

    public route2(int index) {
        this.index = index;
    }

    public route2(int index, int vehindex) {
        this.index = index;
        this.vehindex = vehindex;
    }

    public void setvidx(int vidx) {
        this.vehindex = vidx;
    }

    public void setidx(int idx) {
        this.index = idx;
    }

    public void setconsumer(ArrayList<Integer> customer) {
        int n = -1;
        //System.err.println(customer.toString());
        ArrayList<Integer> ctmp = new ArrayList<Integer>();
        for (int i = 0; i < customer.size(); i++) {
            if (customer.get(i) == 0) {
                n = i;
                break;
            }
        }
        if (n >= 0) {
            for (int i = n; i < customer.size(); i++) {
                ctmp.add(customer.get(i));
            }
            for (int i = 0; i < n; i++) {
                ctmp.add(customer.get(i));
            }
        } else {
            ctmp = customer;
        }
        this.assignedcustomer = ctmp;
    }

    public void cal_largest_releasetime(paramSDD_case userparam) {
        for (Integer e1 : this.assignedcustomer) {
            if (userparam.releasetime.get(e1) > this.largest_releasetime) {
                this.largest_releasetime = userparam.releasetime.get(e1);
            }
        }
    }

    public void cal_tsptime(paramSDD_case up) {// static
        tspdis = 0;
        if (this.assignedcustomer.size() >= 2) {//???
            int pre = this.assignedcustomer.get(0);
            int cur = 0;
            for (int i = 1; i < this.assignedcustomer.size(); i++) {
                cur = this.assignedcustomer.get(i);
                tspdis += up.distance[pre][cur];
                //System.err.print("\n "+pre+" "+cur+" = "+up.distance[pre][cur]+" Tdis= "+tspdis);

                pre = cur;
            }
            cur = this.assignedcustomer.get(this.assignedcustomer.size() - 1);
            int depot = this.assignedcustomer.get(0);
            tspdis += up.distance[depot][cur];
            //System.err.print("\n "+depot+" "+cur+" = "+up.distance[depot][cur]+" Tdis= "+tspdis);

        }
        //System.err.print("\ncal_tspdis = "+tspdis);
    }


    public void set_completetime(double newcompletetime) {
        this.completetime = newcompletetime;
    }

    public void set_departime(double a1) {
        this.departime = a1;
    }

    public void set_tsptime(double newtspdis) {
        this.tspdis = newtspdis;
    }

    public void set_traveltime(double a1) {
        this.traveltime = a1;
    }


    public ArrayList<aorder> cal_nodesave(paramSDD_case userparam) {
        HashMap<Integer, ArrayList<Double>> consumer_saving = new HashMap<Integer, ArrayList<Double>>();

        ArrayList<aorder> consumer_saving1 = new ArrayList<aorder>();
        if (this.assignedcustomer.size() >= 2) {
            double[] curstartime = new double[this.assignedcustomer.size()];
            int cidx = 0;
            for (Integer e1 : this.assignedcustomer) {
                curstartime[cidx] = userparam.nodes.get(e1).RELEASE_DATE;
                cidx += 1;
            }
            Arrays.sort(curstartime);
            double first = curstartime[cidx - 1];
            double second = curstartime[cidx - 2];
            int pre = 0;
            for (int i = 1; i < this.assignedcustomer.size() - 1; i++) {
                double savetime = 0;
                int a = this.assignedcustomer.get(i);
                int b = this.assignedcustomer.get(pre);
                if (userparam.releasetime.get(a) >= first) {
                    savetime = Math.max(0, userparam.releasetime.get(a) - second);
                }
                int nxt = i + 1;
                int c = this.assignedcustomer.get(nxt);
                //System.err.println(this.assignedcustomer.get(pre).index+","+this.assignedcustomer.get(i).index+
                //","+this.assignedcustomer.get(nxt).index);
                double savedis = userparam.distance[a][b] + userparam.distance[b][c] - userparam.distance[a][c];
                pre = i;
                aorder t = new aorder(a, savetime, savedis);
                consumer_saving1.add(t);

                ArrayList<Double> tmp1 = new ArrayList<Double>();
                tmp1.add(savetime);
                tmp1.add(savedis);
                consumer_saving.put(a, tmp1);
            }
            ArrayList<Double> tmp1 = new ArrayList<Double>();
            tmp1.add(0.0);
            tmp1.add(0.0);
            consumer_saving.put(this.assignedcustomer.get(0), tmp1);
            //aorder t=new aorder(a,savetime,savedis);
            //consumer_saving1.add(t);

            int last = this.assignedcustomer.size() - 1;
            int a = this.assignedcustomer.get(last);
            int b = this.assignedcustomer.get(0);//depot
            double savetime = 0;
            if (userparam.releasetime.get(a) >= first) {
                savetime = Math.max(0, userparam.releasetime.get(a) - second);
            }
            double savedis = userparam.distance[a][pre] + userparam.distance[a][b] - userparam.distance[pre][b];
            ArrayList<Double> tmp11 = new ArrayList<Double>();
            tmp11.add(savetime);
            tmp11.add(savedis);
            consumer_saving.put(a, tmp11);
            aorder t = new aorder(a, savetime, savedis);
            consumer_saving1.add(t);
        }
        return consumer_saving1;
        //return consumer_saving;

    }

    public void display_route(BufferedWriter bw1) throws IOException {
        //System.out.println("Route: "+this.index+" Rd= "+this.largest_releasetime+" Departime: "+this.departime+" traveltime: "+
        //this.traveltime+" Complete at: "+(this.completetime));
        //System.out.println("Served Customers: "+this.assignedcustomer.toString());
        bw1.write("\n Route: " + this.index + " vidx: " + this.vehindex + " cidx: "
                + " Max_Rd= " + this.largest_releasetime + " Departime: " + this.departime + " traveltime: " +
                this.traveltime + " Complete at: " + (this.completetime));
        bw1.flush();
        bw1.write("\n Served Customers: " + this.assignedcustomer.toString());
        bw1.flush();
    }

    public void display_route1() throws IOException {
        System.out.println("Route: " + this.index + " vidx: " + this.vehindex +
                " Max_Rd= " + this.largest_releasetime + " Departime: " + this.departime + " traveltime: " +
                this.traveltime + " Complete at: " + (this.completetime));
        System.out.println("Served Customers: " + this.assignedcustomer.toString());
    }

    public void draw_route(paramSDD_case userparam) {
        StdDraw.setXscale(-10, 500);
        StdDraw.setYscale(-10, 500);
        int pre = this.assignedcustomer.get(0);
        for (int i = 1; i < this.assignedcustomer.size(); i++) {
            int cur = this.assignedcustomer.get(i);
            double x0 = userparam.nodes.get(pre).XCOORD;
        }
    }

}
