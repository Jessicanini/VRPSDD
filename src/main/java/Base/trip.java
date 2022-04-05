package Base;

import java.util.List;

import TimeNetWork.TBP;
import ilog.concert.IloException;
import util.aorder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class trip {
    public int index = -1;

    public int vehindex = -1;

    public double departime = -1;
    public double traveltime = -1;
    public double completetime = -1;

    public double largest_releasetime = -1;
    public double tspdis = 0;
    public double sct = -1;

    public ArrayList<Integer> assignedcustomer = new ArrayList<Integer>();
    public int cnum = 0;
    public paramSDD_case up;
    public ArrayList<Double> arrivalTime = new ArrayList<>();


    public trip(paramSDD_case up) {
        this.up = up;
    }

    public trip(int index, paramSDD_case up) {
        this.index = index;
        this.up = up;
    }

    public trip(int index, int vehindex, paramSDD_case up) {
        this.index = index;
        this.vehindex = vehindex;
        this.up = up;
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
        for (Integer e : this.assignedcustomer) {
            if (e > 0 && e != up.nbclients + 1) {
                cnum += 1;
            }
        }
    }

    public void cal_largest_releasetime() {
        this.largest_releasetime = -1;
        for (Integer e1 : this.assignedcustomer) {
            if (up.releasetime.get(e1) > this.largest_releasetime) {
                this.largest_releasetime = up.releasetime.get(e1);
            }
        }
    }

    public void cal_traveltime(double st) {// dynamic
        ArrayList<Integer> seg = new ArrayList<Integer>();
        for (Integer e : this.assignedcustomer) {
            seg.add(e);
        }
        this.traveltime = cal_segment_traveltime(st, seg);

        ArrayList<Integer> seg1 = new ArrayList<Integer>();
        for (Integer e : this.assignedcustomer) {
            if (e < up.nbclients + 1) {//customer
                seg1.add(e);
            }
        }
        this.sct = st + cal_segment_traveltime(st, seg1);
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

    public double cal_node_traveltime(int v, int w, double st) {
        double tt = -1;

        double st1 = st;
        double ft = -1;
        int cnt = 0;
        TBP tmp = up.time_breakpoint[v][w];
        if (tmp.right.size() == 0) {
            //System.err.println(v+","+w);
            assert false;
        }
        if (tmp.right.size() == 0) {
            return 1000000;
        }
        if (st1 >= tmp.right.get(tmp.right.size() - 1)) {
            cnt = tmp.right.size() - 1;
        } else {
            for (int i = 0; i < tmp.left.size(); i++) {
                if (st1 >= tmp.left.get(i) && st1 <= tmp.right.get(i)) {
                    cnt = i;
                    break;
                }
            }
        }
        tt = tmp.slope.get(cnt) * st1 + tmp.intercept.get(cnt);
        ft = tt + st1;
        return tt;
    }

    public double cal_segment_traveltime(double st, ArrayList<Integer> seg) {
        this.arrivalTime = new ArrayList<>();
        double st1 = st;
        double tt = 0;
        double tt1 = 0;
        this.arrivalTime.add(st1);
        for (int i = 0; i < seg.size() - 1; i++) {//
            int v = seg.get(i);
            int w = seg.get(i + 1);
            tt = cal_node_traveltime(v, w, st1);
            //System.err.println("v= "+v+" w= "+w+" st1= "+st1+" tt= "+tt);
            tt1 += tt;
            st1 += tt;
            this.arrivalTime.add(st1);
        }
        return tt1;
    }

    public ArrayList<aorder> cal_nodesave(double k1, double k2) {
        //HashMap<Integer,ArrayList<Double>> consumer_saving=new HashMap<Integer,ArrayList<Double>>();

        ArrayList<aorder> consumer_saving1 = new ArrayList<aorder>();
        if (this.assignedcustomer.size() >= 3) {
            double[] curstartime = new double[this.assignedcustomer.size()];
            int cidx = 0;
            for (Integer e1 : this.assignedcustomer) {
                curstartime[cidx] = up.nodes.get(e1).RELEASE_DATE;
                cidx += 1;
            }
            Arrays.sort(curstartime);
            double first = curstartime[cidx - 1];
            double second = curstartime[cidx - 2];
            ArrayList<Integer> ac = customercopy();
            //ac.add(up.nbclients+1);0,11,11
            double totaltime = this.cal_segment_traveltime(first, ac);

            for (int i = 1; i < this.assignedcustomer.size() - 1; i++) {//�ų���β
                ArrayList<Integer> ac1 = customercopy();
                ac1.remove(i);

                double savetime = 0;
                double savedis = 0;
                double curtime = 0;
                int a = this.assignedcustomer.get(i);
                if (up.releasetime.get(a) >= first) {
                    savetime = Math.max(0, up.releasetime.get(a) - second);
                }
                //taking into account changes in the starting time of the route.
                //�������ܸĽ������ʱ��
                if (savetime == 0) {
                    curtime = this.cal_segment_traveltime(first, ac1);
                    savedis = totaltime - curtime;
                } else {
                    curtime = this.cal_segment_traveltime(first - savetime, ac1);
                    savedis = totaltime - curtime;
                }

                aorder t = new aorder(a, savedis, savetime);
                consumer_saving1.add(t);


            }
        }
        return consumer_saving1;
        //return consumer_saving;

    }

    public void display_trip(BufferedWriter bw1) throws IOException {
        //System.out.println("Route: "+this.index+" Rd= "+this.largest_releasetime+" Departime: "+this.departime+" traveltime: "+
        //this.traveltime+" Complete at: "+(this.completetime));
        //System.out.println("Served Customers: "+this.assignedcustomer.toString());
        bw1.write("\n Trip: " + this.index + " vidx: " + this.vehindex
                + " Max_Rd= " + this.largest_releasetime + " Departime: " + this.departime + " traveltime: " +
                this.traveltime + " Complete at: " + (this.completetime));
        bw1.write("\n ArriveTime = " + this.arrivalTime.toString());
        bw1.flush();
        bw1.write("\n Served Customers: " + this.assignedcustomer.toString());
        bw1.flush();
    }

    public void display() {
        System.out.println(" Trip: " + this.index + " vidx: " + this.vehindex
                + " Max_Rd= " + this.largest_releasetime + " Departime: " + this.departime + " traveltime: " +
                this.traveltime + " Complete at: " + (this.completetime));
        System.out.println(" ArriveTime = " + this.arrivalTime.toString());
        System.out.println(" Served Customers: " + this.assignedcustomer.toString());
    }


    public ArrayList<Integer> customercopy() {//TODO
        ArrayList<Integer> ac = new ArrayList<Integer>();
        for (Integer e : this.assignedcustomer) {
            ac.add(e);
        }
        return ac;
    }
    public ArrayList<Double> arrivalTimecopy() {//TODO
        ArrayList<Double> ac = new ArrayList<Double>();
        for (Double e : this.arrivalTime) {
            ac.add(e);
        }
        return ac;
    }

    public trip tripcopy() {
        trip another = new trip(this.up);
        another.set_completetime(this.completetime);
        another.set_departime(this.departime);
        another.set_traveltime(this.traveltime);
        another.set_sct(this.sct);
        another.setidx(this.index);
        another.setvidx(this.vehindex);
        another.assignedcustomer = this.customercopy();
        another.largest_releasetime = this.largest_releasetime;
        another.cnum = this.cnum;
        another.arrivalTime = this.arrivalTimecopy();
        return another;

    }

    public void cal_readytime_BPS() {
        int pre1 = this.assignedcustomer.get(0);
        for (int i = 1; i < this.assignedcustomer.size() - 1; i++) {
            int cur1 = this.assignedcustomer.get(i);
            int pre2 = cur1;
            int cur2 = this.assignedcustomer.get(i + 1);


        }
    }

    public void set_sct(double sct1) {
        this.sct = sct1;

    }
}
