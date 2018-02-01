package cn.edu.ncu.model;

import cn.edu.ncu.controller.RootController;
import javafx.scene.paint.Color;

public class SettingMessage {
    private double lineWidth;
    private RootController.Tool tool;
    private double eraserSize;

    private double storkRed;
    private double storkBlue;
    private double storkGreen;
    private double storkOpacity;

    public SettingMessage(double storkBlue, double storkGreen,
                          double storkRed, double storkOpacity, double lineWidth,
                          double eraserSize, RootController.Tool tool) {
        this.lineWidth = lineWidth;
        this.eraserSize = eraserSize;
        this.tool = tool;

        this.storkBlue = storkBlue;
        this.storkGreen = storkGreen;
        this.storkRed = storkRed;
        this.storkOpacity = storkOpacity;
    }

    public Color storkColor(){
        return new Color(storkRed, storkGreen, storkBlue, storkOpacity);
    }

    public void changeStorkColor(Color storkColor){
        this.storkBlue = storkColor.getBlue();
        this.storkGreen = storkColor.getGreen();
        this.storkRed = storkColor.getRed();
        this.storkOpacity = storkColor.getOpacity();
    }

    public double getStorkRed() {
        return storkRed;
    }

    public void setStorkRed(double storkRed) {
        this.storkRed = storkRed;
    }

    public double getStorkBlue() {
        return storkBlue;
    }

    public void setStorkBlue(double storkBlue) {
        this.storkBlue = storkBlue;
    }

    public double getStorkGreen() {
        return storkGreen;
    }

    public void setStorkGreen(double storkGreen) {
        this.storkGreen = storkGreen;
    }

    public double getStorkOpacity() {
        return storkOpacity;
    }

    public void setStorkOpacity(double storkOpacity) {
        this.storkOpacity = storkOpacity;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(double lineWidth) {
        this.lineWidth = lineWidth;
    }

    public RootController.Tool getTool() {
        return tool;
    }

    public void setTool(RootController.Tool tool) {
        this.tool = tool;
    }

    public double getEraserSize() {
        return eraserSize;
    }

    public void setEraserSize(double eraserSize) {
        this.eraserSize = eraserSize;
    }
}
