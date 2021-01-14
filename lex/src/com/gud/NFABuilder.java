package com.gud;

import com.gud.struct.NFA;
import com.gud.struct.NFAFragment;
import com.gud.struct.NFAState;
import com.gud.util.EscapeUtil;

import java.util.*;

/**
 * 构建NFA 非确定有限自动机
 * <p>
 * Created By Gud
 */
public class NFABuilder {

    /**
     * 中缀转后缀,消除括号
     *
     * @param reg
     * @return
     */
    public String[] transInfixToSuffix(String[] reg) {

        Stack<String> opStack = new Stack<>();
        Deque<String> outputDeque = new ArrayDeque<>();

        for (int pointer = 0; pointer < reg.length; pointer++) {
            //运算符和左括号
            if ("|".equals(reg[pointer]) || "•".equals(reg[pointer])) {
                String top="";
                if(!opStack.isEmpty()){
                    top = opStack.pop();
                }
                while ((!"(".equals(top))&&(!"".equals(top))) {
                    outputDeque.push(top);
                    if(!opStack.isEmpty()){
                        top = opStack.pop();
                    }else{
                        break;
                    }
                }
                opStack.push(top);
                opStack.push(reg[pointer]);
            } else if ("*".equals(reg[pointer])) {
                outputDeque.push(reg[pointer]);
            } else if ("(".equals(reg[pointer])) {
                opStack.push(reg[pointer]);
            } else if (")".equals(reg[pointer])) {
                //右括号
                String top = opStack.pop();
                while ((!"(".equals(top))) {
                    outputDeque.push(top);
                    top = opStack.pop();
                }
            } else if (!"".equals(reg[pointer])) {
                //普通符号
                outputDeque.push(reg[pointer]);
            }
        }

        while (!opStack.isEmpty()) {
            outputDeque.push(opStack.pop());
        }

        //stack转Str[]

        List<String> res = new LinkedList<>();
        while (!outputDeque.isEmpty()) {
            String s = outputDeque.removeLast();
            if (!"".equals(s)) {
                res.add(s);
            }
        }
        return res.toArray(new String[res.size()]);
    }

    /*
    E.g.
    0•(0|1|2|3)*((a|b|c)|(a|b|c))•((((a|b|c)|(a|b|c))*)|ø)
    00123|||abc||abc|||abc||abc|||*ø|•*•
     */

    /**
     * 这三个符号存在转义问题 * | •
     *
     * @param regSplit 转义处理后的;          确保连接符正确添加
     * @return
     */
    public NFAFragment buildNFAFragment(String[] regSplit, NFA nfa) {

        String keyCharStr = "*|•ø";
        Stack<NFAFragment> fragmentStack = new Stack<>();

        for (int i = 0; i < regSplit.length; i++) {
            String target = regSplit[i];
            if (keyCharStr.contains(target)) {
                //运算符
                switch (target) {
                    case "•": {
                        NFAFragment f2 = fragmentStack.pop();
                        NFAFragment f1 = fragmentStack.pop();
                        if (f1 != null && f2 != null) {
                            f1.endState.addEpsilonOut(f2.startState);
                            f1.endState = f2.endState;
                            fragmentStack.push(f1);
                        } else {
                            System.out.println("wrong");
                        }
                        break;
                    }
                    case "|": {
                        NFAFragment f2 = fragmentStack.pop();
                        NFAFragment f1 = fragmentStack.pop();
                        if (f1 != null && f2 != null) {
                            NFAState sState = new NFAState(nfa, f1.startState, f2.startState);
                            NFAState eState = new NFAState(nfa);
                            f1.endState.addEpsilonOut(eState);
                            f2.endState.addEpsilonOut(eState);
                            fragmentStack.push(new NFAFragment(sState, eState));
                        } else {
                            System.out.println("wrong");
                        }
                        break;

                    }
                    case "*": {
                        NFAFragment f1 = fragmentStack.pop();
                        if (f1 != null) {
                            NFAState eState = new NFAState(nfa);
                            NFAState sState = new NFAState(nfa, f1.startState, eState);
                            f1.endState.addEpsilonOut(f1.startState, eState);
                            fragmentStack.push(new NFAFragment(sState, eState));
                        } else {
                            System.out.println("wrong");
                        }
                        break;
                    }
                    case "ø": {
                        NFAState eState = new NFAState(nfa);
                        NFAState sState = new NFAState(nfa, eState);
                        fragmentStack.push(new NFAFragment(sState, eState));
                        break;
                    }
                    default: {
                    }
                }
                continue;
            } else if (target.length() == 2) {
                target = String.valueOf(regSplit[i].toCharArray()[1]);
            }
            //target现在是 非运算符
            if (target.length() == 1) {
                char targetChar = target.toCharArray()[0];
                NFAState eState = new NFAState(nfa);
                NFAState sState = new NFAState(nfa, targetChar, eState);
                fragmentStack.push(new NFAFragment(sState, eState));
            }
        }

        NFAFragment resF = fragmentStack.pop();
        //让栈空
        if (resF != null) {
            while (!fragmentStack.isEmpty()) {
                NFAFragment preF = fragmentStack.pop();
                preF.endState.addEpsilonOut(resF.startState);
                resF.startState = preF.startState;
            }
        }
        if (resF.endState.getC() != 256) {
            System.out.println("出大问题");
        }

        return resF;
    }

    public NFA buildNFA(NFA nfa, Map<String[], String> regMap) {
        //NFA nfa=new NFA();
        NFAState startState = new NFAState(nfa);
        EscapeUtil escapeUtil = new EscapeUtil();

        boolean flag = true;
        for (Map.Entry<String[], String> entry : regMap.entrySet()) {

            System.out.print("建立NFAFragment:");
            for (String s : entry.getKey()) {
                System.out.print(s);
            }
            System.out.println();


            NFAFragment nfaFragment = buildNFAFragment(transInfixToSuffix(entry.getKey()), nfa);


            if (flag) {
//                startState= nfaFragment.startState;
                startState.addEpsilonOut(nfaFragment.startState);
                flag = false;
            } else {
                startState.addEpsilonOut(nfaFragment.startState);
                NFAState temp = new NFAState(nfa, startState);
                startState = temp;
                nfa.setStartStateId(startState.getId());
            }

            nfa.addEndFunc(nfaFragment.endState, entry.getValue());

        }
        return nfa;
    }
}
