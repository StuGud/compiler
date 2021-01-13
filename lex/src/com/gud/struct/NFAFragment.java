package com.gud.struct;

/**
 * 防止连接符添加出错
 * <p>
 * Created By Gud
 */
public class NFAFragment {
    public NFAState startState;
    public NFAState endState;

    public NFAFragment() {
    }

    public NFAFragment(NFAState startState, NFAState endState) {
        this.startState = startState;
        this.endState = endState;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (startState != null) {
            sb.append("startStateID:" + startState.getId());
            if (startState.getC() < 256) {
                sb.append(" c:" + startState.getC() + " out1:" + startState.getOut1().getId());
            } else if (startState.getC() == 256) {
                sb.append(" match");
            } else {
                System.out.println(" out1:" + startState.getOut1().getId() + " out2:" + startState.getOut2().getId());
            }
        }
        return sb.toString();
    }
}
