package com.uangel.training;

import org.junit.Test;

class Calulator {
    int left, rigth;

    public void setOperands(int left, int right) {
        this.left = left;
        this.rigth = right;
    }

    public void sum() {
        System.out.println(this.left + this.rigth);
    }

    public void avg() {
        System.out.println((this.left + this.rigth) / 2);
    }
}

class SubstractCalculator extends Calulator {

    public void substract() {
        System.out.println(this.left - this.rigth);
    }
}

public class DemoInherit {
    public static void main(String[] args) {
        SubstractCalculator ci = new SubstractCalculator();
        ci.setOperands(10, 20);
        ci.sum();
        ci.avg();
        ci.substract();
    }

    @Test
    public void testInherit() {
        SubstractCalculator ci = new SubstractCalculator();
        ci.setOperands(10, 20);
        ci.sum();
        ci.avg();
        ci.substract();
        String abc = "A" + "B" + "C" + 'D';
        System.out.println(abc);
    }
}
