package com.gud;

import com.gud.struct.DFA;
import com.gud.struct.DFAState;
import com.gud.struct.NFA;
import com.gud.struct.NFAFragment;
import com.gud.util.EscapeUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created By Gud on 2020/12/29 2:50 上午
 */
public class Test {
    public static void main(String[] args) throws IOException {
        System.out.println((char) 248);
        System.out.println((char) 100);

        //new Test().testNFABuilder();

        //new EscapeUtil().escapeFormalizedStr("\\+abc\\*");

        //new Test().readFile();

        new Test().testDFA();

    }

    private void testDFA() throws IOException {
        LexFileParser lexFileParser = new LexFileParser("input/mylexTest.l");
        Map<String, String> exp_map = lexFileParser.getExp_Map();
        RegFormalizer regFormalizer = new RegFormalizer();

        Map<String[], String> formalizedExpMap = new HashMap<>();
        for (Map.Entry<String, String> entry : exp_map.entrySet()) {
            System.out.println("规范化前的正规表达式：" + entry.getKey());
            String[] formalizedReg = regFormalizer.formalize(entry.getKey());
            formalizedReg = new EscapeUtil().reverseEscapeFormalizedReg(formalizedReg);
            System.out.print("规范化后的正规表达式：");
            for (String s : formalizedReg) {
                System.out.print(s);
            }
            System.out.println("\n================================");
            formalizedExpMap.put(formalizedReg, entry.getValue());
        }
        NFA nfa = new NFABuilder().buildNFA(new NFA(), formalizedExpMap);
        DFA dfa = new DFABuilder(nfa).buildDFA();

        int startStateId = dfa.getStartStateId();
        System.out.println(startStateId);

        //======
        int curStateId = startStateId;

        Map<Integer, DFAState> stateMap = dfa.getStateMap();
        DFAState curState = stateMap.get(0);
        String[] test = "1212211".split("");
        for (int i = 0; i < test.length; i++) {
            System.out.println("现在状态编号:" + curState.getId() + " 可跳转边数:" + curState.getEdge2StateMap().size());
            System.out.println("出发边:" + test[i].charAt(0));
            if (curState.getEdge2StateMap().containsKey((int) test[i].charAt(0))) {
                curState = curState.getEdge2StateMap().get((int) test[i].charAt(0));
                System.out.println("边" + test[i] + "跳转到" + curState.getId());
            } else {
                System.out.println("不跳转");
            }
            if (dfa.getEndFuncMap().containsKey(curState)) {
                System.out.print("结束状态,结束函数:");
                for (String s : dfa.getEndFuncMap().get(curState)) {
                    System.out.println(s);
                }
                System.out.println();
            }
        }

    }

    private void testNFABuilder() {
        String formalizedReg = "abc||*";
        NFAFragment nfaFragment = new NFABuilder().buildNFAFragment(formalizedReg.split(""), new NFA());
        System.out.println(nfaFragment);
    }

    private void readFile() throws IOException {
        File file = new File("lex/src/com/gud/test.l");
        BufferedReader buf = new BufferedReader(new FileReader(file));

        String lineStr = buf.readLine();

        System.out.println((int) "ø".toCharArray()[0]);

        String[] formalizedReg = new RegFormalizer().formalize(lineStr);
        System.out.println(formalizedReg);
        System.out.println(new NFABuilder().transInfixToSuffix(formalizedReg));
    }
}
