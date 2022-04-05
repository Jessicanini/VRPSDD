package util;

import lombok.Data;

@Data
public class aorder {
    public int subscript;
    public double value1;
    public double value2;

    public aorder(int s, double v1, double v2) {
        this.subscript = s;
        this.value1 = v1;
        this.value2 = v2;
    }
}
