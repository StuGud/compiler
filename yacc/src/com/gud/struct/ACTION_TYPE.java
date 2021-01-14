package com.gud.struct;

/**
 * Created By Gud on 2021/1/6 8:19 下午
 */
public enum ACTION_TYPE {
    SHIFT(1),REDUCTION(2),GOTO_STATE(3),ERROR(4),ACCEPT(5);

    public final int actionValue;

    private ACTION_TYPE(int actionValue) {
        this.actionValue = actionValue;
    }
}
