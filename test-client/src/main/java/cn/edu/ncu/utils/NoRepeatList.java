package cn.edu.ncu.utils;

import cn.edu.ncu.exception.EmptyListException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class  NoRepeatList<T> extends HashSet<T> {

    private List<T> list = new LinkedList<>();

    @Override
    public boolean add(T t) {
        boolean result = super.add(t);
        if (result){
            list.add(t);
        }
        return result;
    }

    public T pop() throws EmptyListException {
        if (list.isEmpty()){
            throw new EmptyListException();
        }
        T t = list.get(0);
        remove(t);
        return t;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = super.remove(o);
        if (result){
            list.remove(o);
        }
        return result;
    }

    @Override
    public void clear() {
        super.clear();
        list.clear();
    }
}
