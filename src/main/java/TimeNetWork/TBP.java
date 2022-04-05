package TimeNetWork;

import java.util.ArrayList;
import java.util.HashSet;
import java.io.IOException;
import java.math.BigDecimal;

public class TBP {

    public ArrayList<Integer> seg = new ArrayList<Integer>();
    public int number = 0;//
    public double[] xdomain = new double[2];// ֵ
    public ArrayList<Double> left = new ArrayList<Double>();
    public ArrayList<Double> right = new ArrayList<Double>();
    public ArrayList<Double> ub = new ArrayList<Double>();
    public ArrayList<Double> slope = new ArrayList<Double>();
    public ArrayList<Double> intercept = new ArrayList<Double>();
    public ArrayList<Double> travel_values = new ArrayList<Double>();
    public double maxrd;


    public TBP() {
    }

    public TBP(double time_horizon, double distance) {
        //if distance==0
        this.left.add(0.0);
        this.right.add(time_horizon);
        this.slope.add(0.0);
        this.intercept.add(0.0);
        number++;
    }


    public TBP(boolean flag, ArrayList<Double> cluster_speeds, double[][] speed_zones,
               int speed_zone_count, double distance, int v, int w) {//��ʼ�����ʹ��
        for (int i = 0; i < speed_zone_count; i++) {
            this.ub.add(speed_zones[i][1]);
        }
        this.ub.add(speed_zones[speed_zone_count - 1][1] * 10);
        ArrayList<ArrayList<Double>> templeft = new ArrayList<ArrayList<Double>>();

        double leftv1 = 0;
        double rightv1 = 0;
        double leftv2 = 0;
        double rightv2 = 0;
        for (int i = 0; i < speed_zone_count - 1; i++) {
            double maxdis = (speed_zones[i][1] - speed_zones[i][0]) * cluster_speeds.get(i);
            if (maxdis <= distance) {
                ArrayList<Double> temp = new ArrayList<Double>();
                temp.add(speed_zones[i][0]);
                temp.add(speed_zones[i][1]);
                templeft.add(temp);
            } else {
                leftv1 = speed_zones[i][0];
                rightv1 = speed_zones[i][0] + distance / cluster_speeds.get(i);//???
                rightv1 = speed_zones[i][1] - distance / cluster_speeds.get(i);
                leftv2 = rightv1;
                rightv2 = speed_zones[i][1];
                //System.out.println("more speed_i: " + i + "," + leftv1 + "," + rightv1 + "," + leftv2 + "," + rightv2);
                if (flag) {
                    //System.out.println("more,speed_i: " + i + "," + leftv1 + "," + rightv1 + "," + leftv2 + "," + rightv2);
                }
                if (templeft.size() > 0) {
                    ArrayList<ArrayList<Double>> merge = new ArrayList<ArrayList<Double>>();
                    ArrayList<Double> a = templeft.get(0);
                    double s1 = a.get(0);
                    double s2 = a.get(1);
                    double t1 = cal_node_traveltime(s1, distance, cluster_speeds) - s1;
                    double t2 = cal_node_traveltime(s2, distance, cluster_speeds) - s2;
                    double tan = (t2 - t1) / (s2 - s1);
                    ArrayList<Double> tmp = new ArrayList<Double>();
                    for (Double e : a) {
                        tmp.add(e);
                    }
                    merge.add(tmp);

                    for (int b = 1; b < templeft.size(); b++) {
                        ArrayList<Double> a1 = templeft.get(b);
                        double s11 = a1.get(0);
                        double s21 = a1.get(1);
                        double t11 = cal_node_traveltime(s11, distance, cluster_speeds) - s11;
                        double t21 = cal_node_traveltime(s21, distance, cluster_speeds) - s21;
                        double tan1 = (t21 - t11) / (s21 - s11);
                        if (Math.abs(tan1 - tan) < 0.00001) {//merge
                            merge.get(merge.size() - 1).set(1, a1.get(1));
                            tan = tan1;
                        } else {
                            ArrayList<Double> tmp1 = new ArrayList<Double>();
                            for (Double e : a1) {
                                tmp1.add(e);
                            }
                            merge.add(tmp1);
                            tan = tan1;
                        }
                    }
                    for (ArrayList<Double> e : merge) {
                        BigDecimal l = new BigDecimal(e.get(0));
                        double l1 = l.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        BigDecimal r = new BigDecimal(e.get(1));
                        double r1 = r.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        left.add(l1);
                        right.add(r1);
                        number++;
                    }

                }
                BigDecimal l = new BigDecimal(leftv1);
                double l1 = l.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                BigDecimal r = new BigDecimal(rightv1);
                double r1 = r.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                left.add(l1);
                right.add(r1);
                number++;
                templeft = new ArrayList<ArrayList<Double>>();
                ArrayList<Double> temp = new ArrayList<Double>();
                temp.add(leftv2);
                temp.add(rightv2);
                templeft.add(temp);
            }//for else
        }
        ArrayList<Double> temp = new ArrayList<Double>();
        temp.add(speed_zones[speed_zone_count - 1][0]);
        temp.add(speed_zones[speed_zone_count - 1][1]);
        templeft.add(temp);

        if (templeft.size() >= 0) {
            ArrayList<ArrayList<Double>> merge = new ArrayList<ArrayList<Double>>();
            ArrayList<Double> a = templeft.get(0);
            double s1 = a.get(0);
            double s2 = a.get(1);
            double t1 = cal_node_traveltime(s1, distance, cluster_speeds) - s1;
            double t2 = cal_node_traveltime(s2, distance, cluster_speeds) - s2;
            double tan = (t2 - t1) / (s2 - s1);

            ArrayList<Double> tmp = new ArrayList<Double>();
            for (Double e : a) {
                tmp.add(e);
            }
            merge.add(tmp);

            for (int b = 1; b < templeft.size(); b++) {//??
                ArrayList<Double> a1 = templeft.get(b);
                double s11 = a1.get(0);
                double s21 = a1.get(1);
                double t11 = cal_node_traveltime(s11, distance, cluster_speeds) - s11;
                double t21 = cal_node_traveltime(s21, distance, cluster_speeds) - s21;
                double tan1 = (t21 - t11) / (s21 - s11);
                if (Math.abs(tan1 - tan) < 0.00001) {//merge
                    merge.get(merge.size() - 1).set(1, a1.get(1));
                    tan = tan1;
                } else {
                    ArrayList<Double> tmp1 = new ArrayList<Double>();
                    for (Double e : a1) {
                        tmp1.add(e);
                    }
                    merge.add(tmp1);
                    tan = tan1;
                }
            }
            for (ArrayList<Double> e : merge) {
                BigDecimal l = new BigDecimal(e.get(0));
                double l1 = l.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                BigDecimal r = new BigDecimal(e.get(1));
                double r1 = r.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                left.add(l1);
                right.add(r1);
                number++;
            }
        } else {
        }
        for (int i = 0; i < left.size(); i++) {
            if (i < left.size() - 1) {
                if (left.get(i) > left.get(i + 1)) {
                    System.err.println(v + "," + w + " Break point error");
                }
            }
            double start1 = left.get(i);
            double start2 = right.get(i);
            double t1 = cal_node_traveltime(start1, distance, cluster_speeds) - start1;
            double t2 = cal_node_traveltime(start2, distance, cluster_speeds) - start2;

            double w2 = right.get(i);
            double w1 = left.get(i);

            double k0 = (t2 - t1) / (w2 - w1);
            BigDecimal k = new BigDecimal((t2 - t1) / (w2 - w1));
            double k1 = k.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            double b0 = t1 - w1 * ((t2 - t1) / (w2 - w1));
            //b0=t1-w1*k1;
            BigDecimal b = new BigDecimal(t1 - w1 * ((t2 - t1) / (w2 - w1)));
            double b1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (Math.abs(k0 - k1) <= 0.000001) {
                k0 = k1;
            }
            if (Math.abs(b0 - b1) <= 0.000001) {
                b0 = b1;
            }
            slope.add(k0);
            intercept.add(b0);
        }
        boolean flag1 = false;
        for (int i = 0; i < this.slope.size() - 1 && !flag1; i++) {
            double k0 = this.slope.get(i);
            double k1 = this.slope.get(i + 1);
            double b0 = this.intercept.get(i);
            double b1 = this.intercept.get(i + 1);
            if (Math.abs(k0 - k1) <= 0.000001 && Math.abs(b0 - b1) <= 0.000001) {
                //System.err.println("TBP Error!!!");
                flag1 = true;
            }
        }
    }

    public TBP(ArrayList<Double> cluster_speeds, double[][] speed_zones,
               int speed_zone_count, double distance, int v, int w) {
        this.seg.add(v);
        this.seg.add(w);
        for (int i = 0; i < speed_zone_count; i++) {
            this.ub.add(speed_zones[i][1]);
        }
        this.ub.add(speed_zones[speed_zone_count - 1][1] * 10);
        ArrayList<ArrayList<Double>> temp1 = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> tmpk = new ArrayList<Double>();
        ArrayList<Double> tmpb = new ArrayList<Double>();

        double leftv1 = 0;
        double rightv1 = 0;
        double leftv2 = 0;
        double rightv2 = 0;
        for (int i = 0; i < speed_zone_count; i++) {


            double maxdis = (speed_zones[i][1] - speed_zones[i][0]) * cluster_speeds.get(i);
            if (v == 0 && w == 1) {
                System.err.println("i,j,dis= " + v + "," + w + "," + distance + "," + maxdis + "," + speed_zones[i][1] + "," +
                        speed_zones[i][0] + "," + cluster_speeds.get(i));
            }
            if (greatereq(distance, maxdis)) {
                ArrayList<Double> temp = new ArrayList<Double>();
                temp.add(speed_zones[i][0]);
                temp.add(speed_zones[i][1]);
                temp1.add(temp);
                double s1 = speed_zones[i][0];
                double s2 = speed_zones[i][1];
                double t1 = cal_node_traveltime(s1, distance, cluster_speeds) - s1;
                double t2 = cal_node_traveltime(s2, distance, cluster_speeds) - s2;
                double k0 = (t2 - t1) / (s2 - s1);
                double b0 = t1 - s1 * ((t2 - t1) / (s2 - s1));
                tmpk.add(k0);
                tmpb.add(b0);
            } else {
                leftv1 = speed_zones[i][0];
                rightv1 = speed_zones[i][1] - distance / cluster_speeds.get(i);
                leftv2 = rightv1;
                rightv2 = speed_zones[i][1];
                ArrayList<Double> temp = new ArrayList<Double>();
                temp.add(leftv1);
                temp.add(rightv1);
                temp1.add(temp);
                double s1 = leftv1;
                double s2 = rightv1;
                double t1 = cal_node_traveltime(s1, distance, cluster_speeds) - s1;
                double t2 = cal_node_traveltime(s2, distance, cluster_speeds) - s2;
                double k0 = (t2 - t1) / (s2 - s1);
                double b0 = t1 - s1 * ((t2 - t1) / (s2 - s1));
                tmpk.add(k0);
                tmpb.add(b0);

                temp = new ArrayList<Double>();
                temp.add(leftv2);
                temp.add(rightv2);
                temp1.add(temp);
                s1 = leftv2;
                s2 = rightv2;
                t1 = cal_node_traveltime(s1, distance, cluster_speeds) - s1;
                t2 = cal_node_traveltime(s2, distance, cluster_speeds) - s2;
                k0 = (t2 - t1) / (s2 - s1);
                b0 = t1 - s1 * ((t2 - t1) / (s2 - s1));
                tmpk.add(k0);
                tmpb.add(b0);
            }
        }
        ArrayList<ArrayList<Double>> merge = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> a = temp1.get(0);
        ArrayList<Double> tmp = new ArrayList<Double>();
        for (Double e : a) {
            tmp.add(e);
        }
        merge.add(tmp);

        double k0 = tmpk.get(0);
        double b0 = tmpb.get(0);
        for (int b = 1; b < temp1.size(); b++) {
            ArrayList<Double> a1 = temp1.get(b);
            double k1 = tmpk.get(b);
            double b1 = tmpb.get(b);
            if (Math.abs(k0 - k1) <= 0.000001 && Math.abs(b0 - b1) <= 0.000001) {
                merge.get(merge.size() - 1).set(1, a1.get(1));
                k0 = k1;
                b0 = b1;
            } else {
                ArrayList<Double> tmp1 = new ArrayList<Double>();
                for (Double e : a1) {
                    tmp1.add(e);
                }
                merge.add(tmp1);
                k0 = k1;
                b0 = b1;
            }
        }
        for (ArrayList<Double> e : merge) {
            BigDecimal l = new BigDecimal(e.get(0));
            double l1 = l.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            BigDecimal r = new BigDecimal(e.get(1));
            double r1 = r.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            left.add(l1);
            right.add(r1);
            number++;
        }
        for (int i = 0; i < left.size(); i++) {

            if (i < left.size() - 1) {
                if (left.get(i) >= left.get(i + 1)) {
                    System.err.println(v + "," + w + " Break point error");
                }
            }
            double start1 = left.get(i);
            double start2 = right.get(i);
            double t1 = cal_node_traveltime(start1, distance, cluster_speeds) - start1;
            double t2 = cal_node_traveltime(start2, distance, cluster_speeds) - start2;

            double w2 = right.get(i);
            double w1 = left.get(i);

            double k01 = (w2 == w1) ? (0.0) : (t2 - t1) / (w2 - w1);
            BigDecimal k = (w2 == w1) ? new BigDecimal(0.0) : (new BigDecimal((t2 - t1) / (w2 - w1)));
            double k1 = k.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

            double b01 = t1 - w1 * (k01);
            //b0=t1-w1*k1;
            BigDecimal b = new BigDecimal(t1 - w1 * (k01));
            double b1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            if (Math.abs(k01 - k1) <= 0.000001) {
                k01 = k1;
            }
            if (Math.abs(b01 - b1) <= 0.000001) {
                b01 = b1;
            }
            slope.add(k01);
            intercept.add(b01);
        }
        for (int i1 = 0; i1 < left.size(); i1++) {
            this.travel_values.add(this.slope.get(i1) * this.left.get(i1) + this.intercept.get(i1));
        }
    }

    public void check() {
        boolean flag = false;
        for (int i = 0; i < this.slope.size() - 1 && !flag; i++) {
            double k0 = this.slope.get(i);
            double k1 = this.slope.get(i + 1);
            double b0 = this.intercept.get(i);
            double b1 = this.intercept.get(i + 1);
            if (Math.abs(k0 - k1) <= 0.000001 && Math.abs(b0 - b1) <= 0.000001) {
                //System.err.println("TBP Error!!!");
                flag = true;
            }
        }
    }

    public double cal_node_traveltime(double start1, double d_ij, ArrayList<Double> cluster_speeds1) {
        ArrayList<Double> cluster_speeds = new ArrayList<Double>();
        for (Double e : cluster_speeds1) {
            cluster_speeds.add(e);
        }
        if (start1 >= 1000000000){//|| d_ij >= 10000) {
            return 0;
        }
        int cnt = 0;
        boolean flag = false;
        double te = -1;
        double vlast = cluster_speeds1.get(cluster_speeds1.size() - 1);
        for (int i = 0; i < 10; i++) {
            cluster_speeds.add(vlast);
        }

        double t = start1;
        int period = 0;
        while (t >= this.ub.get(period)) {
            period += 1;
        }

        double d1 = 0;
        double d2 = (this.ub.get(period) - t) * cluster_speeds.get(period);

        while (d2 <= d_ij) {
            t = this.ub.get(period);
            d1 = d2;
            period += 1;
            d2 = d1 + (this.ub.get(period) - t) * cluster_speeds.get(period);

        }
        if (period >= this.ub.size()) {
            period = this.ub.size() - 1;
        }

        double ans = t + (d_ij - d1) / cluster_speeds.get(period);
        return ans;
    }

    public void cal_bp(ArrayList<Double> travel_time) {
        HashSet<Double> lefinterval = new HashSet<Double>();
        HashSet<Double> rightinterval = new HashSet<Double>();
        int prel = 0;
        double pv = travel_time.get(prel);
        int prer = 0;
        int cnt = 0;
        for (int i = 1; i < travel_time.size(); i++) {
            if (travel_time.get(i) == pv) {
                cnt += 1;
                if (cnt == 1) {
                    lefinterval.add((double) prel);
                    left.add((double) prel);
                }
                prer = i;
            } else {
                if (cnt >= 2) {
                    rightinterval.add((double) prer);
                    right.add((double) prer);
                }
                prel = i;
                prer = i;
                pv = travel_time.get(prel);
                cnt = 0;

            }
        }
        if (cnt >= 2) {
            rightinterval.add((double) prer);
            right.add((double) prer);
        }
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setXdomain(double[] xdomain) {
        this.xdomain = xdomain;
    }

    public double[] getXdomain() {
        return this.xdomain;
    }

    public void setY(ArrayList<Double> y) {
        this.travel_values = y;
    }

    public ArrayList<Double> getY() {
        return this.travel_values;
    }

    public void setSeg(ArrayList<Integer> seg) {
        this.seg = seg;
    }

    public ArrayList<Integer> getSeg() {
        return this.seg;
    }

    public void setLeft(ArrayList<Double> left) {
        this.left = left;
    }

    public ArrayList<Double> getLeft() {
        return left;
    }

    public void setRight(ArrayList<Double> right) {
        this.right = right;
    }

    public ArrayList<Double> getRight() {
        return right;
    }

    public void setSlope(ArrayList<Double> slope) {
        this.slope = slope;
    }

    public ArrayList<Double> getSlope() {
        return slope;
    }

    public void setIntercept(ArrayList<Double> intercept) {
        this.intercept = intercept;
    }

    public ArrayList<Double> getIntercept() {
        return intercept;
    }

    public double getMaxrd() {
        return maxrd;
    }

    public void setMaxrd(double largest_releasetime) {
        this.maxrd = largest_releasetime;
    }


    public boolean greatereq(double a, double b) {
        if (a - b >= 1E-6 || Math.abs(a - b) <= 1E-6) {
            return true;
        } else {
            return false;
        }
    }

    public boolean greater(double a, double b) {
        if (a - b >= 1E-6) {
            return true;
        } else {
            return false;
        }
    }

}
