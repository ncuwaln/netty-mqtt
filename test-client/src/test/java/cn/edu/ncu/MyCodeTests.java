package cn.edu.ncu;

import cn.edu.ncu.exception.EmptyListException;
import cn.edu.ncu.utils.NoRepeatList;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class MyCodeTests {
    @Test
    public void test(){
        List<String> list = new LinkedList<>();
        list.add("hello");
        list.add("world");
        String data = list.get(0);
        list.remove(data);
        assert data.equals("hello");
    }

    @Test
    public void testNoRepeatList(){
        NoRepeatList<String> list = new NoRepeatList<>();
        list.add("12345");
        list.add("54321");
        list.add("13579");
        list.add("246810");
        assert !list.add("12345");
        list.remove("12345");
        assert list.add("12345");
        try {
            list.pop();
        } catch (EmptyListException e) {
            e.printStackTrace();
        }
        assert list.add("54321");
    }
}
