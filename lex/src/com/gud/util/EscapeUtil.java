package com.gud.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 转义工具类
 * Created By Gud on 2021/1/1 8:05 下午
 */
public class EscapeUtil {

    //规范化的正规表达式存在的转义
    Map<String, String> formalizedRegEscapeMap = new HashMap<>();
    Map<String, String> formalizedRegEscapeReverseMap = new HashMap<>();

    //规范化的正规表达式存在的转义
    Map<String, String> escapeMap = new HashMap<>();
    Map<String, String> escapeReverseMap = new HashMap<>();

    Map<String, String> reservedWordEscapeMap = new HashMap<>();
    Map<String, String> reservedWordEscapeReverseMap = new HashMap<>();

    public EscapeUtil() {
        initFormalizedRegEscapeMap();
        initEscapeMap();
        initReservedWordEscape();
    }

    private void initFormalizedRegEscapeMap() {
        String[] escape = {"*", "•", "|", "\\"};
        String[] escaped = {"\\*", "\\•", "\\|", "\\\\"};

        for (int i = 0; i < escape.length; i++) {
            formalizedRegEscapeMap.put(escape[i], escaped[i]);
            formalizedRegEscapeReverseMap.put(escaped[i], escape[i]);
        }
    }

    private void initEscapeMap() {
        String[] escape = {"+", "[", "]", "-", "^", "?", "."};
        String[] escaped = {"\\+", "\\[", "\\]", "\\-", "\\^", "\\?", "\\."};

        for (int i = 0; i < escape.length; i++) {
            escapeMap.put(escape[i], escaped[i]);
            escapeReverseMap.put(escaped[i], escape[i]);
        }
    }


    /**
     * 保留字中是字母以及符号，转义的话
     */
    private void initReservedWordEscape(){
        String[] escape = {"*", "•", "|", "\\","(",")"};
        String[] escaped = {"\\*", "\\•", "\\|", "\\","\\(","\\)"};

        for (int i = 0; i < escape.length; i++) {
            reservedWordEscapeMap.put(escape[i], escaped[i]);
            reservedWordEscapeReverseMap.put(escaped[i], escape[i]);
        }
    }

    private void initUnicodeEscapeMap(){

    }

    public String[] reverseEscapeFormalizedReg(String[] formalizedReg){
        for (int i = 0; i < formalizedReg.length; i++) {
            formalizedReg[i]=escapeReverseMap.getOrDefault(formalizedReg[i],formalizedReg[i]);
        }
        return formalizedReg;
    }

    public String[] escapeReservedWord(String[] formalizedReg){
        for (int i = 0; i < formalizedReg.length; i++) {
            formalizedReg[i]=reservedWordEscapeMap.getOrDefault(formalizedReg[i],formalizedReg[i]);
        }
        return formalizedReg;
    }



    /**
     * 转义的概念。。。
     *
     * @param reg
     * @return
     */
    public String[] escapeFormalizedStr(String reg) {
        String[] split = reg.split("");

        for (int i = 0; i < split.length; i++) {
            if (formalizedRegEscapeMap.containsKey(split[i])) {
                split[i] = formalizedRegEscapeMap.get(split[i]);
            }
        }
        return split;
        //return Arrays.stream(split).filter(s -> (s != null && s.length() > 0)).toArray(String[]::new);
    }


}
