package com.uangel.training;


import org.junit.Test;

class MultifyCalculator extends SubstractCalculator {

    public void multify() {
        System.out.println(this.left / this.rigth);
    }
}

public class DemoInherit1 {
    @Test
    public void testInherit1() {
        MultifyCalculator ci = new MultifyCalculator();
        ci.setOperands(10, 20);
        ci.sum();
        ci.avg();
        ci.substract();
        ci.multify();
        String abc = "A" + "B" + "C" + 'D';
        System.out.println(abc);
    }
}
