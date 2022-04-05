package Base;
import lombok.Data;

import java.util.Collections;
import java.util.Comparator;
@Data
public class Node implements Comparable<Node> {
    public int index;
    public String STOPNAME = "";
    public double XCOORD;
    public double YCOORD;
    public double demand;
    public double OPENING_TW;
    public double CLOSING_TW;
    public double SERVICE_TIME;
    public double RELEASE_DATE;
    public double save_time = 0;
    public double save_detour = 0;
    public int routeidx = -1;
    public int disidx = -1;
    public int pre_disidx = -1;
    public String REGIONCODE = "";
    public int regionc = -1;

    public Node(int index, double corx, double cory, double RELEASE_DATE) {
        this.index = index;
        this.XCOORD = corx;
        this.YCOORD = cory;
        this.RELEASE_DATE = RELEASE_DATE;
    }

    public Node(int index, double corx, double cory, double demand, double OPENING_TW, double CLOSING_TW, double SERVICE_TIME, double RELEASE_DATE) {
        this.index = index;
        this.XCOORD = corx;
        this.YCOORD = cory;
        this.demand = demand;
        this.OPENING_TW = OPENING_TW;
        this.CLOSING_TW = CLOSING_TW;
        this.SERVICE_TIME = SERVICE_TIME;
        this.RELEASE_DATE = RELEASE_DATE;
    }

    public void set_region(String rcode) {
        this.REGIONCODE = rcode;
    }

    public void set_name(String name) {
        this.STOPNAME = name;
    }

    public void set_idx(int idx) {
        this.index = idx;
    }

    public void set_savetime(double st) {
        this.save_time = st;
    }

    public void set_savedis(double sd) {
        this.save_detour = sd;
    }

    public void set_routeidx(int routeidx) {
        this.routeidx = routeidx;
    }

    public void set_disidx(int didx) {
        this.disidx = didx;
    }

    public void set_pre_disidx(int didx) {
        this.pre_disidx = didx;
    }

    public void update_disidx(int didx) {
        this.disidx = didx;
    }

    public void update_pre_disidx(int didx) {
        this.pre_disidx = didx;
    }

    public double cal_dis(Node n1) {
        double dis1 = Math.sqrt((n1.XCOORD - this.XCOORD) * (n1.XCOORD - this.XCOORD) + (n1.YCOORD - this.YCOORD) * (n1.YCOORD - this.YCOORD));
        //return Math.round(dis1);
        return dis1;
    }

    public void diplay_node() {

    }

    public int compareTo(Node n2) {
        // TODO Auto-generated method stub
        double diff = (-this.save_time + n2.save_time);
        if (diff == 0) {
            diff = (-this.save_detour + n2.save_detour);
        }
        //System.err.println(n1.index+":"+n2.index+":"+diff);
        if (diff < 0) {
            return -1;
        } else if (diff > 0) {
            return 1;
        } else {
            return 0;
        }
    }

}
