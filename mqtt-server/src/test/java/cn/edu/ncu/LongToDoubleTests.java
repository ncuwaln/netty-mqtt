package cn.edu.ncu;

import org.junit.Test;

public class LongToDoubleTests {

    @Test
    public void testLongToDouble(){
        Long l = Long.MAX_VALUE;
        Long l2 = l - 1;

        double d = l.doubleValue();
        double d2 = l2.doubleValue();
        assert d == l;
        assert l2 == d2;
        System.out.println(l);
        System.out.println(l2);
        System.out.println(d);
        System.out.println(d2);
    }
}
