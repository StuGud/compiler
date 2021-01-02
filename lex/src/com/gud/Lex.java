package com.gud;

import java.io.IOException;
import java.util.Map;

/**
 * Created By Gud on 2020/12/29 12:42 上午
 */
public class Lex {
    public static void main(String[] args) throws IOException {

        LexParser lexParser = new LexParser("lex/src/com/gud/c99_test2.l");
        Map<String, String> exp_map = lexParser.getExp_Map();
        RegFormalizer regFormalizer = new RegFormalizer();

        for(Map.Entry<String,String> entry: exp_map.entrySet()){
            System.out.println("规范化前的正规表达式："+entry.getKey());
            String formalizedReg = regFormalizer.formalize(entry.getKey());
            System.out.println("规范化后的正规表达式："+formalizedReg);
            System.out.println("================================");
        }

    }
}
