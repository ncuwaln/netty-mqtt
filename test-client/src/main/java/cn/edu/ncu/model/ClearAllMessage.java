package cn.edu.ncu.model;

public class ClearAllMessage {
    private boolean isClearAll = true;

    public ClearAllMessage() {
        this.isClearAll = true;
    }

    public boolean isClearAll() {
        return isClearAll;
    }

    public void setClearAll(boolean clearAll) {
        isClearAll = clearAll;
    }
}
