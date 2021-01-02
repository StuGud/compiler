package com.gud;

import java.util.*;

/**
 * Created By Gud on 2020/12/29 3:01 下午
 * 正规表达式规范化
 */
public class RegFormalizer {

    //转义
    Map<String, String> escapeMap;
    Map<String, String> escapeReverseMap;


    public RegFormalizer() {
        escapeMap = new HashMap<>();
        escapeReverseMap = new HashMap<>();
        String[] escape = {"+", "*", "•", "(", ")", "[", "]", "-", "^", "?", "|", ".", "\\"};
        String[] escaped = {"\\+", "\\*", "\\•", "\\(", "\\)", "\\[", "\\]", "\\-", "\\^", "\\?", "\\|", "\\.", "\\\\"};
        if (escape.length == escaped.length) {
            for (int i = 0; i < escape.length; i++) {
                escapeMap.put(escape[i], escaped[i]);
                escapeReverseMap.put(escaped[i], escape[i]);
            }
        } else {
            System.out.println("出错!");
        }

    }


    /**
     * 处理转义字符
     *
     * @param reg
     * @return
     * @todo java中没有/v
     */
    private String processUnicodeEscape(String reg) {
        Map<String, String> tempEscapeMap = new HashMap<>();
        tempEscapeMap.put("t", "\t");
        tempEscapeMap.put("n", "\n");
        tempEscapeMap.put("b", "\b");
        tempEscapeMap.put("f", "\f");
        //java中只有\b \t \n \f \r
        //tempEscapeMap.put("v","\v");
        tempEscapeMap.put("r", "\r");
        tempEscapeMap.put("s", " ");

        String[] split = reg.split("");
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("\\")) {
                split[i] = "";
                if (i + 1 < split.length && tempEscapeMap.containsKey(split[i + 1])) {
                    split[i + 1] = tempEscapeMap.get(split[i + 1]);
                }
            }
        }
        return String.join("", split);
    }

    /**
     * @param reg
     * @return 返回的字符串中可能存在空串""
     */
    private String[] processRegexEscape(String reg) {
        String[] split = reg.split("");
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("\\")) {
                if (i + 1 < split.length && escapeMap.containsKey(split[i + 1])) {
                    split[i] = escapeMap.get(split[i + 1]);
                    split[i + 1] = "";
                }
            }
        }
        return split;
    }

    /**
     * 解析 . => (a|b|c|...)
     *
     * @param reg
     * @return
     */
    private String transformAllExp(String reg) {
        String[] spilt = processRegexEscape(reg);

        //创建 allChar
        String[] temp = new String[128];
        for (int i = 0; i < 128; i++) {
            temp[i] = String.valueOf((char) i);
            if (escapeMap.containsKey(temp[i])) {
                temp[i] = escapeMap.get(temp[i]);
            }
        }
        String allChar = "(" + String.join("|", temp) + ")";

        //进行替换
        for (int i = 0; i < spilt.length; i++) {
            if (spilt[i].equals(".")) {
                spilt[i] = allChar;
            }
        }

        return String.join("", spilt);
    }


    /**
     * 解析[^] (a|b|c...)
     *
     * @param reg
     * @return
     */
    private String transformNegRangeExp(String reg) {
        String[] split = processRegexEscape(reg);
        for (int i = 0; i < split.length - 2; i++) {
            if (split[i].equals("[") && split[i + 1].equals("^")) {
                int end = 0;
                String negCharSet = "";
                for (int j = i + 2; j < split.length; j++) {
                    negCharSet += split[j];
                    if (split[j].equals("]")) {
                        end = j;
                        break;
                    }
                }
                if (end != 0) {
                    String[] temp = new String[128];
                    for (int j = 0; j < 128; j++) {
                        temp[j] = String.valueOf((char) j);
                        if (escapeMap.containsKey(temp[j])) {
                            temp[j] = escapeMap.get(temp[j]);
                        }
                        if (negCharSet.indexOf(temp[j]) != -1) {
                            temp[j] = "";
                        }
                    }
                    split[i] = "(" + String.join("|", temp) + ")";
                    for (int j = i + 1; j <= end; j++) {
                        split[j] = "";
                    }
                }
            }
        }
        return String.join("", split);
    }

    String escapeStr(String singleStr) {
        return escapeMap.getOrDefault(singleStr, singleStr);
    }

    /**
     * 对于/[ 我们想表达的是 [ 这个符号，和正规表达式的[符号区分开来
     *
     * @param escapedStr
     * @return
     */
    String reverseEscapedStr(String escapedStr) {
        return escapeReverseMap.getOrDefault(escapedStr, escapedStr);
    }


    /**
     * 解析[?-?] [a-z] (a|b
     *
     * @param reg
     * @return
     */
    private String transformRangeExpAdvanced(String reg) {
        String[] split = processRegexEscape(reg);
        for (int i = 0; i < split.length; i++) {
            if ("[".equals(split[i])) {
                List<String> starts = new LinkedList<>();
                List<String> ends = new LinkedList<>();
                int end = 0;
                StringBuilder rangeContent = new StringBuilder();
                for (int j = i + 1; j < split.length; ) {
                    if ("]".equals(split[j])) {
                        end = j;
                        break;
                    }
                    if (j + 2 < split.length) {
                        if ("-".equals(split[j + 1])) {
                            starts.add(split[j]);
                            ends.add(split[j + 2]);
                            j += 3;
                        } else {
                            System.out.println("sys.出现一些问题");
                            break;
                        }
                    } else {
                        System.out.println("sys.出现一些问题");
                        break;
                    }
                }
                if (starts.size() == ends.size()) {
                    Iterator<String> sIterator = starts.iterator();
                    Iterator<String> eIterator = ends.iterator();
                    char endChar=0;
                    while (sIterator.hasNext()) {
                        //进行转义 /t 转为 t
                        char startChar = escapeStr(sIterator.next()).toCharArray()[0];
                        endChar = escapeStr(eIterator.next()).toCharArray()[0];
                        for (int j = (int) startChar; j < (int) endChar; j++) {
                            rangeContent.append(escapeStr(String.valueOf((char) j)) + "|");
                        }

                    }
                    rangeContent.append(escapeStr(String.valueOf(endChar)));
                } else {
                    System.out.println("sys.解析[?-?]出现问题");
                }
                String rangContentStr = rangeContent.toString();
                if ((!"".equals(rangContentStr)) && end != 0) {
                    split[i] = "(" + rangContentStr + ")";
                    for (int j = i + 1; j <= end; j++) {
                        split[j] = "";
                    }
                }
            }
        }
        return String.join("", split);
    }

    /**
     * 解析 [ab] 为 (a|b)
     *
     * @param reg
     * @return
     */
    private String transformOr(String reg) {
        String[] split = processRegexEscape(reg);
        for (int i = 0; i < split.length; i++) {
            if ("[".equals(split[i])) {
                int end = 0;
                StringBuilder rangeContent = new StringBuilder();
                for (int j = i + 1; j < split.length; j++) {
                    if ("]".equals(split[j])) {
                        end = j;
                        break;
                    } else {
                        rangeContent.append(split[j] + "|");
                    }
                }
                if (rangeContent.length() >= 2) {
                    rangeContent.deleteCharAt(rangeContent.length() - 1);
                }
                String rangContentStr = rangeContent.toString();
                if ((!"".equals(rangContentStr)) && end != 0) {
                    split[i] = "(" + rangContentStr + ")";
                    for (int j = i + 1; j <= end; j++) {
                        split[j] = "";
                    }
                }
            }
        }
        return String.join("", split);
    }

    /**
     * 解析()+
     *
     * @param reg
     * @return
     */
    private String transformOneOrMore(String reg) {
        String[] split = processRegexEscape(reg);
        int pointer = 0;
        for (; pointer < split.length; pointer++) {
            if ("+".equals(split[pointer])) {
                //右括号的位置
                int end = pointer - 1;
                int start = findLeftBracketIndex(split, end);
                if (start > -1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = start + 1; i <= end - 1; i++) {
                        sb.append(split[i]);
                    }
                    String content = sb.toString();
                    split[start] = "(" + content + ")•(" + content + ")?";
                    for (int i = start + 1; i <= end + 1; i++) {
                        split[i] = "";
                    }
                } else {
                    System.out.println("()+匹配失败：" + reg);
                }
            }
        }
        return String.join("", split);
    }

    /**
     * 解析()?
     *
     * @param reg
     * @return
     */
    private String transformZeroOrMore(String reg) {
        String[] split = processRegexEscape(reg);

        for (int pointer = 0; pointer < split.length; pointer++) {
            if ("?".equals(split[pointer])) {
                //右括号的位置
                int end = pointer - 1;
                //对应的左括号的位置
                int start = findLeftBracketIndex(split, end);
                if (start > -1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = start + 1; i <= end - 1; i++) {
                        sb.append(split[i]);
                    }
                    String content = sb.toString();
                    split[start] = "(((" + content + ")*)|ø)";
                    for (int i = start + 1; i <= end + 1; i++) {
                        split[i] = "";
                    }
                } else {
                    System.out.println("()?匹配失败：" + reg);
                }
            }
        }

        return String.join("", split);
    }

    /**
     * 寻找指定右括号对应左括号的位置
     *
     * @param split
     * @param end
     * @return
     * @todo 封装上面两个函数的共同部分
     */
    private int findLeftBracketIndex(String[] split, int end) {
        int start = end - 1;
        if (")".equals(split[end])) {
            for (int i = 1; i > 0; ) {
                if (start == -1) {
                    start = -2;
                    break;
                }
                if ("(".equals(split[start])) {
                    i--;
                } else if (")".equals(split[start])) {
                    i++;
                }
                start--;
            }
        } else {
            start = -2;
        }
        start++;
        return start;
    }

    /**
     * 转义字符恢复
     *
     * @param reg
     * @return
     */
    private String escapeReverse(String reg) {

        Map<String, String> tempReverseEscapeMap = new HashMap<>();
        tempReverseEscapeMap.put("\\+", "+");
        tempReverseEscapeMap.put("\\[", "[");
        tempReverseEscapeMap.put("\\]", "]");
        tempReverseEscapeMap.put("\\-", "-");
        tempReverseEscapeMap.put("\\^", "^");
        tempReverseEscapeMap.put("\\?", "?");
        tempReverseEscapeMap.put("\\.", ".");

        String[] split = processRegexEscape(reg);
        for (int i = 0; i < split.length; i++) {
            split[i] = tempReverseEscapeMap.getOrDefault(split[i], split[i]);
        }
        return String.join("", split);
    }

    /**
     * 添加连接符号
     *
     * @param reg
     * @return
     */
    private String addConnectPoint(String reg) {
        String[] split = processRegexEscape(reg);
        int start = 0;
        String keyCharSetStr = "()|*•";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < split.length - 1; i++) {
            if ((!keyCharSetStr.contains(split[i])) || ")".equals(split[i])) {
                if ((!keyCharSetStr.contains(split[i + 1])) || "(".equals(split[i + 1])) {
                    sb.append(split[i]);
                    sb.append("•");
                } else {
                    sb.append(split[i]);
                }
            } else {
                sb.append(split[i]);
            }
        }
        sb.append(split[split.length - 1]);
        return sb.toString();
    }

    /**
     * @param reg 正规表达式
     * @return 规范化后的正规表达式
     */
    public String formalize(String reg) {
        if (reg.startsWith("\"") && reg.startsWith("\"")) {
            return reg;
        }
        return addConnectPoint(
                escapeReverse(
                        transformZeroOrMore(
                                transformOneOrMore(
                                        transformOr(
                                                transformRangeExpAdvanced(
                                                        transformNegRangeExp(
                                                                transformAllExp(
                                                                        processUnicodeEscape(reg)))))))));
    }
}
