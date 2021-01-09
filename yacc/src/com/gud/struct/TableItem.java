package com.gud.struct;

/**
 * Created By Gud on 2021/1/6 8:21 下午
 */
public class TableItem {
    ACTION_TYPE action=ACTION_TYPE.ERROR;
    int index;


    public TableItem() {
    }

    public TableItem(ACTION_TYPE action, int index) {
        this.action = action;
        this.index = index;
    }

    public ACTION_TYPE getAction() {
        return action;
    }

    public void setAction(ACTION_TYPE action) {
        this.action = action;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
