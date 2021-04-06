package com.uangel.training;

import com.uangel.trainingmain.Say;
import org.junit.Test;
import java.util.List;
import java.util.Arrays;

import static org.junit.Assert.*;

public class HelloWorld {
    @Test
    public void testHello() {
        Say h = new Say();
        System.out.println(h.Say());
        assertEquals("hello", h.Say());
    }

    @Test
    public void testHello1() {

        int num1;
        num1 = 10;

        int num2 = 20;
        int num3 = num1 + num2;
        System.out.println(num1 + "+" + num2 + "=" + num3);
        char c = 'A';
        String aa = "i am tom";
        System.out.println(c); // A
        System.out.println((int) c); // 65
        System.out.println(aa);

        String abc = "A".concat("B").concat("C");
        System.out.println(abc);
    }

    @Test
    public void testType() {


        String abc = "A".concat("B").concat("C");
        System.out.println(abc);

        abc = "A" + "B" + "C" + 'D';
        System.out.println(abc);
    }

    @Test
    public void testType1() {

        List<String> list = Arrays.asList("foo", "bar", "baz", "qux");
        List<Integer> list1 = Arrays.asList(1, 2, 3, 4);

        System.out.println(list);
        System.out.println(list1);
        StringBuilder sb = new StringBuilder();
        String sb1 = "";
        for (String s : list) {
            System.out.println(s);
            sb.append(s);
            sb1 += s;
        }
        System.out.println(sb);
        System.out.println(sb1);
    }
}

