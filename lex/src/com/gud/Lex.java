package com.gud;

import com.gud.struct.DFA;
import com.gud.struct.NFA;
import com.gud.util.EscapeUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created By Gud
 */
public class Lex {
    public static void main(String[] args) throws IOException {

        LexFileParser lexFileParser = new LexFileParser("input/mylex.l");
        Map<String, String> exp_map = lexFileParser.getExp_Map();
        RegFormalizer regFormalizer = new RegFormalizer();

        Map<String[],String> formalizedExpMap=new HashMap<>();
        for(Map.Entry<String,String> entry: exp_map.entrySet()){
            System.out.println("规范化前的正规表达式："+entry.getKey());
            String[] formalizedReg = regFormalizer.formalize(entry.getKey());
            formalizedReg = new EscapeUtil().reverseEscapeFormalizedReg(formalizedReg);
            System.out.print("规范化后的正规表达式：");
            for (String s:formalizedReg){
                System.out.print(s);
            }
            System.out.println("\n================================");
            formalizedExpMap.put(formalizedReg, entry.getValue());
        }
        NFA nfa = new NFABuilder().buildNFA(new NFA(), formalizedExpMap);
        DFA dfa = new DFABuilder(nfa).buildDFA();
        CppBuilder cppBuilder = new CppBuilder(dfa);
        cppBuilder.outputProgram();

    }
}
