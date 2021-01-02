package com.gud;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 转义工具类
 * Created By Gud on 2021/1/1 8:05 下午
 */
public class EscapeUtil {

    //转义
    Map<String, String> escapeMap;
    Map<String, String> escapeReverseMap;

    public EscapeUtil() {
        escapeMap = new HashMap<>();
        escapeReverseMap = new HashMap<>();
        String[] escape = {"*","•", "|", "\\"};
        String[] escaped = { "\\*", "\\•", "\\|","\\\\"};
        if (escape.length == escaped.length) {
            for (int i = 0; i < escape.length; i++) {
                escapeMap.put(escape[i], escaped[i]);
                escapeReverseMap.put(escaped[i], escape[i]);
            }
        } else {
            System.out.println("出错!");
        }
    }

    public String[] escapeFormalizedStr(String reg){
        String[] split = reg.split("");
        int len=split.length;
        for (int i = 1; i < split.length; i++) {
            if("\\".equals(split[i-1])&&escapeMap.containsKey(split[i])){
                split[i-1]=escapeMap.get(split[i]);
                split[i]="";
                len--;
            }
        }
         return Arrays.stream(split).filter(s -> (s != null && s.length() > 0)).toArray(String[]::new);
    }
}
