package com.gud;

import com.gud.struct.NFA;
import com.gud.struct.NFAFragment;
import com.gud.struct.NFAState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Stack;

/**
 * 构建NFA 非确定有限自动机
 * Created By Gud on 2020/12/30 10:13 下午
 */
public class NFABuilder {

    /**
     * 中缀转后缀,消除括号
     *
     * @param reg
     * @return
     */
    public String postifx(String reg) {
        String[] split = reg.split("");
        Stack<String> stack1 = new Stack<>();
        Deque<String> outputDeque = new ArrayDeque<>();

        for (int pointer = 0; pointer < split.length; pointer++) {
            //运算符和左括号
            if ("*".equals(split[pointer]) || "|".equals(split[pointer]) || "(".equals(split[pointer]) || "•".equals(split[pointer])) {
                if (pointer > 0 && "\\".equals(split[pointer - 1])) {
                    outputDeque.push(split[pointer]);
                } else {
                    stack1.push(split[pointer]);
                }
            } else if (")".equals(split[pointer]) && (!"\\".equals(split[pointer]))) {
                //右括号

                String top = stack1.pop();
                while ((!"(".equals(top)) && (!"\\".equals(stack1.peek()))) {
                    outputDeque.push(top);
                    top = stack1.pop();
                }
            } else {
                outputDeque.push(split[pointer]);
            }
        }

        while (!stack1.isEmpty()) {
            outputDeque.push(stack1.pop());
        }

        //stack转Str[]

        StringBuilder sb = new StringBuilder();
        while (!outputDeque.isEmpty()) {
            sb.append(outputDeque.removeLast());
        }
        return sb.toString();
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

        String keyCharStr = "*|•";
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
                            f1.endState.setOut1(f2.startState);
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
                            f1.endState.setOut1(eState);
                            f2.endState.setOut1(eState);
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
                            f1.endState.setOut(f1.startState, eState);
                            fragmentStack.push(new NFAFragment(sState, eState));
                        } else {
                            System.out.println("wrong");
                        }
                        break;
                    }
                    default: {
                        System.out.println("非法字符");
                    }
                }

                continue;
            } else if (target.length() == 2) {
                target = String.valueOf(regSplit[i].toCharArray()[1]);
                if (!keyCharStr.contains(target)) {
                    System.out.println("Sth wrong");
                }
            }
            //target现在是 非运算符
            //symbolStack.push(target);
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
                preF.endState.setOut1(resF.startState);
                resF.startState = preF.startState;
            }
        }
        if (resF.endState.getC() != 256) {
            System.out.println("出大问题");
        }

        return resF;
    }

    private NFA buildNFA(NFA nfa,Map<String, String> regMap) {
        //NFA nfa=new NFA();
        NFAState startState=new NFAState(nfa);
        EscapeUtil escapeUtil = new EscapeUtil();

        boolean flag=true;
        for (Map.Entry<String, String> entry : regMap.entrySet()) {
            NFAFragment nfaFragment = buildNFAFragment(escapeUtil.escapeFormalizedStr(entry.getKey()), nfa);
            if(flag){
//                startState= nfaFragment.startState;
                startState.setOut1(nfaFragment.startState);
                flag=false;
                continue;
            }
            startState.setOut2(nfaFragment.startState);
            NFAState temp=new NFAState(nfa,248,startState);
            startState=temp;
            nfa.addEndFunc(nfaFragment.endState, entry.getValue());
            nfa.setStartStateId(startState.getId());
        }
        return nfa;
    }
}
