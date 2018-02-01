package cn.edu.ncu.model;

import java.util.LinkedList;
import java.util.List;

public class LineMessages {
    private List<LineMessage> lineMessages = new LinkedList<>();

    public void add(LineMessage lineMessage){
        lineMessages.add(lineMessage);
    }

    public void add(double x1, double y1, double x2, double y2){
        lineMessages.add(new LineMessage(x1, y1, x2, y2));
    }

    public List<LineMessage> getLineMessages() {
        return lineMessages;
    }
}
