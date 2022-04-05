package Base;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import TimeNetWork.TBP;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import util.StdDraw;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class paramSDD_case {

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

    FileWriter fw1;
    BufferedWriter bw1;
    FileWriter fw2;
    BufferedWriter bw2;
    FileWriter fw3;
    BufferedWriter bw3;
    FileWriter fwv;
    BufferedWriter bwv;
    FileWriter fwv1;
    BufferedWriter bwv1;
    FileWriter fwv2;
    BufferedWriter bwv2;

    FileWriter fwbp;
    BufferedWriter bwbp;
    private final static double EARTH_RADIUS = 6378.137;//

    public int capacity;
    public double speed;

    public int Nd = 1;//depot number
    public int Nk = 0;
    public int Nt;
    public int Nc = 0;
    public int H = 0;
    public int DDL = -1;
    public int SCT = -1;
    public int region_num = 0;

    public double maxreleasetime = 0;
    public ArrayList<Double> releasetime = new ArrayList<Double>();

    public int nbclients;//customer number Nc
    public ArrayList<Node> nodes = new ArrayList<Node>();//all nodes include depot0
    public ArrayList<Node> depots = new ArrayList<Node>();
    public ArrayList<Node> customers = new ArrayList<Node>();
    public List<List<Integer>> nodeset = new ArrayList<>();

    public TBP[][] time_breakpoint;//=new TBP[][];
    public double[][] speed_zones;//=new ArrayList<Double>();
    public ArrayList<Double> ub = new ArrayList<Double>();
    public double time_horizon = 0;
    public int[][] edge_Hs;
    public int maxEH = 0;
    public double[][][] tmp_traveltime;
    public double maxspeed = 0;

    public double[][] pre_related_rd;
    public double[][] pre_related_dis1;
    public double[][] pre_related_dis2;
    public double[][] pre_related_dis3;

    public double startime = 0;
    public ArrayList<Double> maprealtimes = new ArrayList<Double>();
    public HashMap<String, Integer> stop_idx = new HashMap<String, Integer>();

    public HashMap<ArrayList<Integer>, ArrayList<Double>> MapSpeed = new HashMap<ArrayList<Integer>, ArrayList<Double>>();
    public double[][][] link_traveltime;


    public double[][] cost; // for the SPPRC subproblem
    public double[][] distBase; // original distances for the Branch and Bound
    public double[][] dist; // distances that will be updated during the B&B before being used in the CG & SPPRC
    public double[][] ttime;
    public double[][] distance;//for one vehicle
    public double[][] edges; // weight of each edge during branch and bound
    public double[] posx, posy, d, wval;
    public int[][] shrink;
    public int[] a; // time windows: a=early, b=late, s=service
    public int[] b;
    public int[] s;
    public double[] load;
    public int[] rdtime;
    public double[][] nodedis;
    public double xmin = Double.MAX_VALUE;
    public double ymin = Double.MAX_VALUE;
    public double xmax = Double.MIN_VALUE;
    public double ymax = Double.MIN_VALUE;

    public double maxtime, verybig, gap, maxlength, maxdis;
    public boolean serviceInTW, debug = true;
    String[] citieslab, citiesloc;
    public double B;
    public double speed_Origin;

    public paramSDD_case(double speed_Origin) throws IOException {
        this.speed_Origin = speed_Origin;
        this.gap = 0.00000000001;
        this.serviceInTW = false;
        this.verybig = 1E10;
    }

    public void resetDDL(int newddl) {
        this.DDL = newddl;
    }

    public void initParams(String inputPath, String outdisfile,
                           int k, double time_horizon, int DDL, int SCT, boolean isdynamic,
                           int region_num, ArrayList<Double> partation, boolean servet)
            throws IOException {
        this.B = SCT;
        this.region_num = region_num;
        this.time_horizon = time_horizon;
        this.SCT = SCT;
        this.DDL = DDL;
        this.Nd = 1;
        this.Nk = k;
        System.out.println("Prepare Data");
        Scanner in1 = new Scanner(Paths.get(inputPath));

        for (int i = 0; i < 1; i++) {
            String[] temp = in1.nextLine().split(" ");
            for (int j = 0; j < temp.length; j++) {
            }
        }

        Node depot = null;
        Node depot1 = null;
        //ID lng lat region rd Tw H
        while (in1.hasNextLine()) {
            String[] temp = in1.nextLine().split("\\s+");
            int Index = Integer.parseInt(temp[0]);
            double XCOORD = Double.parseDouble(temp[1]);
            double YCOORD = Double.parseDouble(temp[2]);
            int regionc = Integer.parseInt(temp[3]);
            double RELEASE_DATE = Double.parseDouble(temp[4]);
            double TW = Double.parseDouble(temp[5]);
            int H = Integer.parseInt(temp[6]);

            this.releasetime.add(RELEASE_DATE);
            this.maxreleasetime = Math.max(this.maxreleasetime, RELEASE_DATE);
            if (Index < this.Nd) {
                depot = new Node(Index, XCOORD, YCOORD, 0, 0, 0, 0, RELEASE_DATE);
                depot1 = new Node(Index, XCOORD, YCOORD, 0, 0, 0, 0, RELEASE_DATE);
                depot.setRegionc(regionc);
                depot1.setRegionc(regionc);
                this.depots.add(depot);
                this.nodes.add(depot);
            } else {
                Node consumer = new Node(Index, XCOORD, YCOORD, 0, 0, 0, 0, RELEASE_DATE);
                consumer.setRegionc(regionc);
                this.customers.add(consumer);
                this.nodes.add(consumer);
                this.nbclients += 1;
            }
        }
        this.customers.stream()
                .collect(Collectors.groupingBy(Node::getRegionc, Collectors.toList()))
                .forEach((regionc, CListByRc) -> {
                    this.nodeset.add(CListByRc.stream().map(e -> e.getIndex()).collect(Collectors.toList()));
                });
        this.nodeset.stream().forEach(k1 -> k1.add(0, 0));

        System.err.println("this.NC= " + this.nbclients);
        this.Nc = this.nbclients;
        this.releasetime.add(this.releasetime.get(0));
        depot1.set_idx(this.nbclients + 1);
        this.nodes.add(depot1);

        this.setParams(servet, outdisfile);
        System.out.println("Finish distance set");
        this.set_speed_zones(partation);
        System.out.println("Finish speed_zones set");

    }

    public double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public double GetDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS * 1000;
        s = new BigDecimal(s).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
        return s;
    }

    public void setParams(boolean servet, String outdisfile) throws IOException {
        FileWriter fwd = new FileWriter(makefile(outdisfile), false);
        BufferedWriter bwd = new BufferedWriter(fwd);
        distBase = new double[nbclients + 2][nbclients + 2];
        distance = new double[nbclients + 2][nbclients + 2];
        citieslab = new String[nbclients + 2];
        d = new double[nbclients + 2];
        a = new int[nbclients + 2];
        b = new int[nbclients + 2];
        s = new int[nbclients + 2];
        load = new double[nbclients + 2];
        posx = new double[nbclients + 2];
        posy = new double[nbclients + 2];

        cost = new double[nbclients + 2][nbclients + 2];
        dist = new double[nbclients + 2][nbclients + 2];
        ttime = new double[nbclients + 2][nbclients + 2];
        // copy nodes
        int idx = 0;
        for (Node n : this.nodes) {
            d[idx] = n.demand;
            a[idx] = (int) n.OPENING_TW;
            b[idx] = (int) n.CLOSING_TW;//TODO
            if (servet) {
                s[idx] = (int) n.SERVICE_TIME;
                load[idx] = 0.2 * n.SERVICE_TIME;
            } else {
                s[idx] = 0;
                load[idx] = 0;
            }
            posx[idx] = n.XCOORD;//lat
            posy[idx] = n.YCOORD;
            idx += 1;
        }
        // second depot : copy of the first one for arrival depot0-depot n+1
        citieslab[nbclients + 1] = citieslab[0];
        d[nbclients + 1] = 0.0;
        a[nbclients + 1] = a[0];
        b[nbclients + 1] = b[0];
        s[nbclients + 1] = 0;
        load[nbclients + 1] = 0.0;
        posx[nbclients + 1] = posx[0];
        posy[nbclients + 1] = posy[0];

        // ---- distances
        fwd.write("\n Node i -> j, meters , minutes ");
        fwd.flush();
        double max;
        maxlength = 0.0;
        maxdis = 0;
        for (int i = 0; i < nbclients + 2; i++) {
            max = 0.0;
            for (int j = 0; j < nbclients + 2; j++) {
                double tmp = GetDistance(posx[i], posy[i], posx[j], posy[j]) / 2;
                fwd.write("\n " + i + "," + j + "," + tmp + " m," + tmp / this.speed_Origin + " minute");
                fwd.flush();
                distBase[i][j] = tmp;
                distance[i][j] = tmp;

                if (max < distBase[i][j])
                    max = distBase[i][j];
                if (maxdis < distBase[i][j])
                    maxdis = distBase[i][j];
            }
            maxlength += max;
        }
        for (int i = 0; i < nbclients + 2; i++) {
            distBase[nbclients + 1][i] = this.verybig;
            distance[nbclients + 1][i] = this.verybig;
        }
    }


    public void set_speed_zones(ArrayList<Double> partation) {
        for (int i = 1; i < 6; i++) {
            this.ub.add(partation.get(i) * this.time_horizon);
        }
        this.ub.add(10 * this.time_horizon);
        // 0 0.1 0.3 0.7 0.9 1.0
        speed_zones = new double[5][2];//speed-zone count TODO
        for (int i = 0; i < 5; i++) {
            speed_zones[i][0] = partation.get(i) * time_horizon;
            speed_zones[i][1] = partation.get(i + 1) * time_horizon;
        }
        for (int i = 0; i < 5; i++) {
            //System.out.println("left: " + speed_zones[i][0] + " right: " + speed_zones[i][1]);
        }
    }

    public double cal_node_traveltime(double start1, ArrayList<Double> slope, double d_ij) {

        ArrayList<Double> slope1 = new ArrayList<Double>();
        for (Double e : slope) {
            slope1.add(e);
        }
        if (start1 >= 1000000000 || d_ij >= 1000000) {
            return 0;
        }
        int cnt = 0;
        boolean flag = false;
        double te = -1;

        double vlast = slope.get(slope.size() - 1);
        for (int i = 0; i < 100; i++) {
            slope1.add(vlast);
        }

        double t = start1;
        int period = 0;
        while (t >= this.ub.get(period)) {
            period += 1;
        }
        double d1 = 0;
        double d2 = (this.ub.get(period) - t) * slope1.get(period);

        while (d2 <= d_ij) {
            t = this.ub.get(period);
            d1 = d2;
            period += 1;
            d2 = d1 + (this.ub.get(period) - t) * slope1.get(period);
        }

        if (period >= this.ub.size()) {
            period = this.ub.size() - 1;
        }

        double ans = t + (d_ij - d1) / slope1.get(period);
        return ans;
    }

    public void UpdateLinkTime_ij(int v, int w, ArrayList<Double> newspeed,
                                  String updatefile) throws IOException {
        //[[1, 0.333333, 0.666667, 0.5, 0.833333], [1.16667, 0.666667, 1.33333, 0.833333, 1],
        // [1.5, 1, 1.66667, 1.16667, 1.33333]]
        //edge v,w
        fwv2 = new FileWriter(makefile(updatefile), true);
        bwv2 = new BufferedWriter(fwv2);
        int speed_zone_count = newspeed.size();
        ArrayList<Double> speed_ij = new ArrayList<Double>();
        for (Double e : newspeed) {
            speed_ij.add(e);
        }
        double distance = this.distance[v][w];
        boolean flag = false;
        TBP tbp = new TBP(speed_ij, speed_zones, speed_zone_count, distance, v, w);
        this.maxEH = Math.max(this.maxEH, tbp.number);
        this.time_breakpoint[v][w] = tbp;
        edge_Hs[v][w] = tbp.number;
        bwv2.write(v + "," + w + "," + edge_Hs[v][w] + "\n");
        bwv2.write(tbp.getLeft().toString() + "\n");
        bwv2.write(tbp.getRight().toString() + "\n");
        bwv2.write(tbp.getSlope().toString() + "\n");
        bwv2.write(tbp.getIntercept().toString() + "\n");
        bwv2.flush();
    }

    public void cal_BreakPoint(ArrayList<ArrayList<Double>> speed, String bpfile,
                               String vfile, String vfile1, int[][] edge_cat, int sinterval) throws IOException {
        //[[1, 0.333333, 0.666667, 0.5, 0.833333], [1.16667, 0.666667, 1.33333, 0.833333, 1],
        // [1.5, 1, 1.66667, 1.16667, 1.33333]]
        for (ArrayList<Double> e1 : speed) {
            for (Double e2 : e1) {
                maxspeed = Math.max(e2, maxspeed);
            }
        }

        edge_Hs = new int[nbclients + 2][nbclients + 2];
        fwbp = new FileWriter(makefile(bpfile), false);
        bwbp = new BufferedWriter(fwbp);
        fwv = new FileWriter(makefile(vfile), false);
        bwv = new BufferedWriter(fwv);
        fwv1 = new FileWriter(makefile(vfile1), false);
        bwv1 = new BufferedWriter(fwv1);

        int[][] speed_idx = new int[nbclients + 2][nbclients + 2];
        for (int i = 0; i < nbclients + 1; i++) {
            for (int j = 0; j < nbclients + 1; j++) {
                speed_idx[i][j] = edge_cat[i][j];
            }
        }
        for (int i = 1; i < nbclients + 1; i++) {
            speed_idx[i][nbclients + 1] = speed_idx[i][0];
        }
        this.time_breakpoint = new TBP[nbclients + 2][nbclients + 2];
        int speed_zone_count = speed.get(0).size();
        for (int i = 0; i < nbclients + 2; i++) {
            for (int j = 0; j < nbclients + 2; j++) {
                ArrayList<Double> speed_ij = new ArrayList<Double>();
                int idx = speed_idx[i][j];
                speed_ij = speed.get(idx);

                double distance = this.distBase[i][j];
                if (i != j && distance != verybig) {
                    if (i == 0 && j == nbclients + 1) {
                        TBP tbp = new TBP(this.time_horizon, 0);
                        tbp.check();
                        this.time_breakpoint[i][j] = tbp;
                    } else {

                        boolean flag = false;
                        if (i == 0 && j == 1) {
                            System.err.println("i,j,dis= " + i + "," + j + "," + distance);
                        }
                        TBP tbp = new TBP(speed_ij, speed_zones, speed_zone_count, distance, i, j);
                        tbp.check();
                        this.time_breakpoint[i][j] = tbp;
                    }
                } else {
                    TBP tbp = new TBP();
                    tbp.check();
                    this.time_breakpoint[i][j] = tbp;
                }
            }
        }
        System.out.println("Valiation");
        for (int i = 0; i < nbclients + 2; i++) {
            for (int j = 0; j < nbclients + 2; j++) {
                //System.err.println("s"+s);
                if (i != j) {
                    TBP tbp = time_breakpoint[i][j];
                    edge_Hs[i][j] = tbp.number;
                    this.maxEH = Math.max(this.maxEH, tbp.number);
                    bwbp.write(i + "," + j + "," + edge_Hs[i][j] + "\n");
                    bwbp.write(tbp.getLeft().toString() + "\n");
                    bwbp.write(tbp.getRight().toString() + "\n");
                    bwbp.write(tbp.getSlope().toString() + "\n");
                    bwbp.write(tbp.getIntercept().toString() + "\n");
                    bwbp.flush();
                }
            }
        }

        pre_related_rd = new double[this.nbclients + 2][this.nbclients + 2];
        pre_related_dis1 = new double[this.nbclients + 2][this.nbclients + 2];
        pre_related_dis2 = new double[this.nbclients + 2][this.nbclients + 2];
        pre_related_dis3 = new double[this.nbclients + 2][this.nbclients + 2];
        for (int i = 0; i < nbclients + 2; i++) {
            for (int j = 0; j < nbclients + 2; j++) {
                pre_related_rd[i][j] = Math.abs(this.nodes.get(i).RELEASE_DATE - this.nodes.get(j).RELEASE_DATE);
                //pre_related_dis1[i][j]=max1(this.tmp_traveltime[i][j]);
                //pre_related_dis2[i][j]=min1(this.tmp_traveltime[i][j]);
                //pre_related_dis3[i][j]=average1(this.tmp_traveltime[i][j]);
            }
        }


    }


    public void setNodeDistrict_cluster(String dfile) throws Exception {
        fw3 = new FileWriter(makefile(dfile), false);
        bw3 = new BufferedWriter(fw3);
        int vk = this.region_num;
        ArrayList<Attribute> attributes = new ArrayList<>();
        //attributes.add(new Attribute("index"));
        attributes.add(new Attribute("xcor"));
        attributes.add(new Attribute("ycor"));
        //set instances
        Instances instances = new Instances("customers", attributes, 0);
        //instances.setClassIndex(instances.numAttributes() - 1);
        //add instance
        //int cn=1;
        for (Node c : this.customers) {
            Instance instance = new DenseInstance(attributes.size());
            //instance.setValue(0,c.index);
            instance.setValue(0, c.XCOORD);
            instance.setValue(1, c.YCOORD);
            instances.add(instance);
        }
        EM em = new EM(); // new instance of clusterer
        SimpleKMeans KM = new SimpleKMeans();
        // This is the important parameter to set
        KM.setPreserveInstancesOrder(true);
        KM.setNumClusters(vk);
        KM.buildClusterer(instances);
        System.out.println(KM.preserveInstancesOrderTipText());
        System.out.println(KM.toString());
        //this.nodeset
        ArrayList<ArrayList<Integer>> districts = new ArrayList<ArrayList<Integer>>();
        int[] assignments = KM.getAssignments();
        for (int i = 0; i < KM.getClusterSizes().length; i++) {
            System.out.println(KM.getClusterSizes()[i]);
        }
        for (int i = 0; i < this.customers.size(); i++) {
            System.out.println(i + "," + assignments[i]);
        }

        this.nodeset = new ArrayList<>();
        for (int i = 0; i < vk; i++) {
            ArrayList<Integer> tmp = new ArrayList<Integer>();
            tmp.add(0);
            this.nodeset.add(tmp);
        }
        for (int i = 0; i < this.customers.size(); i++) {
            this.nodeset.get(assignments[i]).add(i + 1);
            //System.out.println(i+","+assignments[i]);
        }
        int cnti = 0;
        for (List<Integer> t : this.nodeset) {
            for (Integer e : t) {
                this.nodes.get(e).set_disidx(cnti);
                this.nodes.get(e).set_pre_disidx(cnti);
            }
            bw1.write("\n Vehicle " + cnti + " contains " + t.size() + " customernodes:");
            bw1.flush();
            bw1.write(t.toString());
            bw1.flush();
            for (Integer e : t) {
                bw3.write(e + ",");
            }
            bw3.write("\n");
            bw3.flush();
            cnti += 1;
        }

    }


    public boolean greater(double a, double b) {
        if (a - b > 1E-6 || a == b) {
            return true;
        } else {
            return false;
        }
    }

    public double min1(double[] a1) {
        double ans = Double.MAX_VALUE;
        for (int i = 0; i < a1.length; i++) {
            ans = Math.min(ans, a1[i]);
        }
        return ans;
    }

    public double max1(double[] a1) {
        double ans = 0;
        for (int i = 0; i < a1.length; i++) {
            ans = Math.max(ans, a1[i]);
        }
        return ans;
    }

    public double average1(double[] a1) {
        double ans = 0;
        for (int i = 0; i < a1.length; i++) {
            ans += a1[i];
        }
        return ans / this.time_horizon;
    }

    public static void main(String[] args) throws IOException, IloException {

    }


}

