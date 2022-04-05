package Base;

import lombok.Data;

import java.util.List;

import ilog.concert.IloException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@Data
public class route {
    public int index = -1;
    public int vehindex = -1;

    public ArrayList<trip> trips = new ArrayList<trip>();
    public ArrayList<Integer> allcustomers = new ArrayList<Integer>();
    public int tripnum = 0;

    public double first_departime = -1;
    public double total_traveltime = -1;
    public double last_completetime = -1;
    public double cur_SCT = -1;
    public double cur_H = 0.0;//履约率

    public route() {
    }

    public route(int index) {
        this.index = index;
    }

    public route(int index, int vehindex) {
        this.index = index;
        this.vehindex = vehindex;
    }

    public void setvidx(int vidx) {
        this.vehindex = vidx;
    }

    public void setidx(int idx) {
        this.index = idx;
    }

    public void set_completetime(double newcompletetime) {
        this.last_completetime = newcompletetime;
    }

    public void set_departime(double a1) {
        this.first_departime = a1;
    }

    public void set_traveltime(double a1) {
        this.total_traveltime = a1;
    }

    public void set_trips(ArrayList<trip> trips) {
        this.tripnum = 0;
        this.trips = trips;
        for (int i = 0; i < this.trips.size(); i++) {
            this.trips.get(i).setidx(i);
        }
        this.tripnum = trips.size();
    }

    public void add(trip nr) {
        nr.setidx(this.tripnum);
        this.trips.add(nr);
        this.tripnum += 1;

    }

    public void update_trip_minc() {//complete time---DDL  sct??? TODO
        double s1 = 0;
        for (trip r : this.trips) {
            r.cal_largest_releasetime();
            s1 = Math.max(s1, r.largest_releasetime);
            r.set_departime(s1);
            r.cal_traveltime(s1);
            double tt1 = r.traveltime;
            double f1 = s1 + tt1;
            r.set_completetime(f1);
            s1 = f1;
        }
    }

    public void cal_time() {
        this.first_departime = Double.MAX_VALUE;
        this.last_completetime = Double.MIN_VALUE;
        this.total_traveltime = 0;
        for (trip r : this.trips) {
            this.first_departime = Math.min(this.first_departime, r.departime);
            this.last_completetime = Math.max(this.last_completetime, r.completetime);//DDL
            this.total_traveltime += r.traveltime;
            if (r.assignedcustomer.size() >= 3) {//这里已经把空路径的SCT排除了
                this.cur_SCT = Math.max(r.sct, this.cur_SCT);
            }
        }
        //最后出发的trip的到最后一个点的完成时间-注意有空路径
        //each node b
    }

    public void update_trip_maxH(double B) {
        double total_number = 0;
        double number_in_time = 0;
        for (trip r : this.trips) {
            number_in_time += r.arrivalTime.subList(1, r.arrivalTime.size()).stream().filter(c -> c < B).count();
            total_number += r.arrivalTime.size() - 1;
        }
        double H = number_in_time / total_number;
        System.out.println(number_in_time + " , " + total_number + " , H = " + H);
        this.cur_H = H;
    }


    public void display_route(BufferedWriter bw1) throws IOException {
        for (trip e : this.trips) {
            e.display_trip(bw1);
        }
    }

    public void display() {
        for (trip e : this.trips) {
            e.display();
        }
    }

    public route routecopy() {
        route routecopy = new route();
        routecopy.setidx(this.index);
        routecopy.setvidx(this.vehindex);

        ArrayList<trip> atrips = new ArrayList<trip>();
        for (trip t : this.trips) {
            trip another = t.tripcopy();
            atrips.add(another);
        }
        routecopy.set_trips(atrips);

        routecopy.set_completetime(this.last_completetime);
        routecopy.set_departime(this.first_departime);
        routecopy.set_traveltime(this.total_traveltime);
        routecopy.setCur_SCT(this.cur_SCT);
        routecopy.setCur_H(this.cur_H);
        return routecopy;
    }


}
