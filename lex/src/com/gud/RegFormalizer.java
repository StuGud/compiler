package com.gud;

import com.gud.util.EscapeUtil;

import java.util.*;

/**
 * Created By Gud on 2020/12/29 3:01 下午
 * 正规表达式规范化
 */
public class RegFormalizer {

    //正规表达式中的转义
    Map<String, String> regEscapeMap = new HashMap<>();
    Map<String, String> regEscapeReverseMap = new HashMap<>();

    Map<String, String> unicodeEscapeMap = new HashMap<>();

    List<String> allCharList = new ArrayList<>();


    public RegFormalizer() {
        initAllCharList();
        initUnicodeEscapeMap();
        initRegEscapeMap();
    }

    private String transToLiteralSymbol(String s) {
        return regEscapeMap.getOrDefault(s, s);
    }

    private String transToLiteralSymbol(int i) {
        String s = String.valueOf((char) i);
        return regEscapeMap.getOrDefault(s, s);
    }

    private void initRegEscapeMap() {
        String[] escape = {"+", "*", "•", "(", ")", "[", "]", "-", "^", "?", "|", ".", "\\"};
        String[] escaped = {"\\+", "\\*", "\\•", "\\(", "\\)", "\\[", "\\]", "\\-", "\\^", "\\?", "\\|", "\\.", "\\\\"};
        if (escape.length == escaped.length) {
            for (int i = 0; i < escape.length; i++) {
                regEscapeMap.put(escape[i], escaped[i]);
                regEscapeReverseMap.put(escaped[i], escape[i]);
            }
        } else {
            System.out.println("出错!");
        }
    }

    private void initAllCharList() {
        allCharList.add("(");
        for (int i = 0; i < 127; i++) {
            allCharList.add(transToLiteralSymbol(i));
            allCharList.add("|");
        }
        allCharList.add(transToLiteralSymbol(127));
        allCharList.add(")");
    }

    private void initUnicodeEscapeMap() {
        Map<String, String> unicodeEscapeMap = new HashMap<>();
        unicodeEscapeMap.put("t", "\t");
        unicodeEscapeMap.put("n", "\n");
        unicodeEscapeMap.put("b", "\b");
        unicodeEscapeMap.put("f", "\f");
        //tempEscapeMap.put("v","\v");
        unicodeEscapeMap.put("r", "\r");
        unicodeEscapeMap.put("s", " ");
    }


    /**
     * 处理转义字符
     *
     * @param reg
     * @return
     * @todo java中没有/v
     */
    private String[] processUnicodeEscape(String reg) {
        String[] split = reg.split("");
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("\\")) {
                if (i + 1 < split.length && unicodeEscapeMap.containsKey(split[i + 1])) {
                    split[i] = "";
                    split[i + 1] = unicodeEscapeMap.get(split[i + 1]);
                }
            }
        }
        return split;
    }

    /**
     * @param reg
     * @return 返回的字符串中可能存在空串""
     */
    private String[] processRegexEscape(String[] reg) {
        // String[] split = reg.split("");
        for (int i = 0; i < reg.length; i++) {
            if (reg[i].equals("\\")) {
                if (i + 1 < reg.length && regEscapeMap.containsKey(reg[i + 1])) {
                    reg[i] = regEscapeMap.get(reg[i + 1]);
                    reg[i + 1] = "";
                }
            }
        }
        return reg;
    }

    /**
     * 解析 . => (a|b|c|...)
     *
     * @param reg
     * @return
     */
    private String[] transformAllExp(String[] reg) {
        reg = Arrays.stream(reg).filter(s -> !s.isEmpty()).toArray(String[]::new);

        outputStr(1,reg);

        List<String> res = new LinkedList<>();

        //进行替换
        for (int i = 0; i < reg.length; i++) {
            if (reg[i].equals(".")) {
                res.addAll(allCharList);
            } else {
                res.add(reg[i]);
            }
        }
        return res.toArray(new String[res.size()]);
    }


    /**
     * 解析[^] (a|b|c...)
     *
     * @param reg
     * @return
     */
    private String[] transformNegRangeExp(String[] reg) {
        reg = Arrays.stream(reg).filter(s -> !s.isEmpty()).toArray(String[]::new);

        outputStr(2,reg);

        List<String> res = new LinkedList<>();

        int i = 0;
        for (; i < reg.length - 2; i++) {
            if (reg[i].equals("[") && reg[i + 1].equals("^")) {
                int end = 0;
                String negCharSet = "";
                for (int j = i + 2; j < reg.length; j++) {
                    negCharSet += reg[j];
                    if (reg[j].equals("]")) {
                        end = j;
                        break;
                    }
                }
                if (end != 0) {
                    List<String> temp = new LinkedList<>();
                    temp.add("(");
                    for (int j = 0; j < 128; j++) {
                        if (negCharSet.indexOf(transToLiteralSymbol(i)) == -1) {
                            temp.add(transToLiteralSymbol(i));
                            temp.add("|");
                        }
                    }
                    temp.set(temp.size() - 1, ")");
                    //@todo 看一下temp是否正确
                    if (temp.size() > 2) {
                        res.addAll(temp);
                        while (i <= end) {
                            i++;
                        }
                    } else {
                        res.add(reg[i]);
                    }
                } else {
                    res.add(reg[i]);
                }
            } else {
                res.add(reg[i]);
            }
        }
        for (; i < reg.length; i++) {
            res.add(reg[i]);
        }
        return res.toArray(new String[res.size()]);
    }

    String escapeStr(String singleStr) {
        return regEscapeMap.getOrDefault(singleStr, singleStr);
    }


    /**
     * 解析[?-?] [a-z] (a|b
     *
     * @param reg
     * @return
     */
    private String[] transformRangeExpAdvanced(String[] reg) {
        reg = Arrays.stream(reg).filter(s -> !s.isEmpty()).toArray(String[]::new);

        outputStr(3,reg);

        List<String> res = new LinkedList<>();

        for (int i = 0; i < reg.length; i++) {
            if ("[".equals(reg[i])) {
                List<String> starts = new LinkedList<>();
                List<String> ends = new LinkedList<>();
                int end = 0;
                for (int j = i + 1; j < reg.length; ) {
                    if ("]".equals(reg[j])) {
                        end = j;
                        break;
                    }
                    if (j + 2 < reg.length) {
                        if ("-".equals(reg[j + 1])) {
                            starts.add(reg[j]);
                            ends.add(reg[j + 2]);
                            j += 3;
                        } else {
                            end=0;
                            System.out.println("范围匹配 - 失败[a-z]");
                            break;
                        }
                    } else {
                        end=0;
                        System.out.println("sys.出现一些问题");
                        break;
                    }
                }
                if (end>=4&&starts.size() == ends.size()&&starts.size()>=1) {
                    Iterator<String> sIterator = starts.iterator();
                    Iterator<String> eIterator = ends.iterator();
                    char endChar = 0;
                    List<String> temp = new LinkedList<>();
                    temp.add("(");
                    while (sIterator.hasNext()) {
                        //进行转义 /t 转为 t
                        char startChar = escapeStr(sIterator.next()).toCharArray()[0];
                        endChar = escapeStr(eIterator.next()).toCharArray()[0];
                        for (int j = startChar; j < (int) endChar; j++) {
                            temp.add(transToLiteralSymbol(j));
                            temp.add("|");
                        }

                    }
                    temp.add(transToLiteralSymbol(endChar));
                    temp.add(")");
                    if (temp.size() > 2) {
                        res.addAll(temp);
                        while (i < end) {
                            i++;
                        }
                    } else {
                        res.add(reg[i]);
                    }
                } else {
                    res.add(reg[i]);
                    System.out.println("sys.解析[?-?]出现问题");
                }
            } else {
                res.add(reg[i]);
            }
        }
        return res.toArray(new String[res.size()]);
    }

    /**
     * 解析 [ab] 为 (a|b)
     *
     * @param reg
     * @return
     */
    private String[] transformOr(String[] reg) {
        reg = Arrays.stream(reg).filter(s -> !s.isEmpty()).toArray(String[]::new);

        outputStr(4,reg);
        List<String> res = new LinkedList<>();

        for (int i = 0; i < reg.length; i++) {
            if ("[".equals(reg[i])) {
                int end = 0;
                List<String> temp = new LinkedList<>();
                temp.add("(");
                for (int j = i + 1; j < reg.length; j++) {
                    if ("]".equals(reg[j])) {
                        end = j;
                        break;
                    } else {
                        temp.add(reg[j]);
                        temp.add("|");
                    }
                }
                if (temp.size() >= 3) {
                    temp.set(temp.size() - 1, ")");
                    res.addAll(temp);

                    while (i <= end) {
                        i++;
                    }
                } else {
                    res.add(reg[i]);
                }

            } else {
                res.add(reg[i]);
            }
        }
        return res.toArray(new String[res.size()]);
    }

    /**
     * 解析()+
     *
     * @param reg
     * @return
     */
    private String[] transformOneOrMore(String[] reg) {
        reg = Arrays.stream(reg).filter(s -> !s.isEmpty()).toArray(String[]::new);
        outputStr(5,reg);
        List<String> res = new LinkedList<>();
        int pointer = 0;
        for (; pointer < reg.length; pointer++) {
            if ("+".equals(reg[pointer])) {
                //右括号的位置
                int end = pointer - 1;
                int start = findLeftBracketIndex(reg, end);

                if (start > -1) {
                    List<String> temp = new LinkedList<>();
                    temp.add("(");
                    for (int i = start + 1; i <= end - 1; i++) {
                        //@todo
                        temp.add(reg[i]);
                    }
                    temp.add(")");

                    for (int i = 0; i < end - start + 1; i++) {
                        res.remove(res.size() - 1);
                    }

                    //"(" + content + ")•(" + content + ")?";
                    res.addAll(temp);
                    res.add("•");
                    res.addAll(temp);
                    res.add("*");

                    pointer++;
                    for(;pointer < reg.length; pointer++){
                        res.add(reg[pointer]);
                    }
                    return transformOneOrMore(res.toArray(new String[res.size()]));
                } else {
                    res.add(reg[pointer]);
                    System.out.println("()+匹配失败：" + reg);
                }
            } else {
                res.add(reg[pointer]);
            }
        }
        return res.toArray(new String[res.size()]);
    }

    /**
     * 解析()?
     *
     * @param reg
     * @return
     */
    private String[] transformZeroOrMore(String[] reg) {
        reg = Arrays.stream(reg).filter(s -> !s.isEmpty()).toArray(String[]::new);
        outputStr(6,reg);
        List<String> res = new LinkedList<>();
        for (int pointer = 0; pointer < reg.length; pointer++) {
            if ("?".equals(reg[pointer])) {
                //右括号的位置
                int end = pointer - 1;
                //对应的左括号的位置
                int start = findLeftBracketIndex(reg, end);
                if (start > -1) {
                    List<String> temp = new LinkedList<>();
                    temp.add("(");
                    for (int i = start + 1; i <= end - 1; i++) {
                        //@todo
                        temp.add(reg[i]);
                    }
                    temp.add(")");

                    if (temp.size() > 2) {
                        for (int i = 0; i < end - start + 1; i++) {
                            res.remove(res.size() - 1);
                        }

                        res.add("(");
                        //res.add("(");
                        res.addAll(temp);
                        //res.add("*");
                        //res.add(")");
                        res.add("|");
                        res.add("ø");
                        res.add(")");

                        pointer++;
                        for(;pointer < reg.length; pointer++){
                            res.add(reg[pointer]);
                        }
                        return transformZeroOrMore(res.toArray(new String[res.size()]));
                    } else {
                        res.add(reg[pointer]);
                    }
                } else {
                    res.add(reg[pointer]);
                    System.out.println("()?匹配失败：" + reg);
                }
            } else {
                res.add(reg[pointer]);
            }
        }

        return res.toArray(new String[res.size()]);
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

        //String[] split = processRegexEscape(reg);
        String[] split = {};
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
    private String[] addConnectPoint(String[] reg) {
        reg = Arrays.stream(reg).filter(s -> !s.isEmpty()).toArray(String[]::new);
        outputStr(7,reg);
        List<String> res = new LinkedList<>();

        String keyCharSetStr = "()|*•";

        int i = 0;
        for (; i < reg.length - 1; i++) {
            if ((!keyCharSetStr.contains(reg[i])) || ")".equals(reg[i])) {
                if ((!keyCharSetStr.contains(reg[i + 1])) || "(".equals(reg[i + 1])) {
//                    sb.append(split[i]);
//                    sb.append("•");
                    res.add(reg[i]);
                    res.add("•");
                } else {
                    //sb.append(split[i]);
                    res.add(reg[i]);
                }
            } else {
                //sb.append(split[i]);
                res.add(reg[i]);
            }
        }
        for (; i < reg.length; i++) {
            res.add(reg[i]);
        }
        return res.toArray(new String[res.size()]);
    }

    /**
     * @param reg 正规表达式
     * @return 规范化后的正规表达式
     */
    public String[] formalize(String reg) {
        EscapeUtil escapeUtil = new EscapeUtil();
        if (reg.startsWith("\"") && reg.startsWith("\"")) {
            String[] split = reg.split("");
            List<String> res = new LinkedList<>();
            for (int i = 1; i < split.length - 1; i++) {
                res.add(transToLiteralSymbol(split[i]));
            }
            return addConnectPoint(
                    escapeUtil.escapeReservedWord(
                            res.toArray(new String[res.size()])));
        }

        //@todo 删除了escapeReverse()，是否有问题
        return addConnectPoint(
                escapeUtil.reverseEscapeFormalizedReg(
                        transformZeroOrMore(
                                transformOneOrMore(
                                        transformOr(
                                                transformRangeExpAdvanced(
                                                        transformNegRangeExp(
                                                                transformAllExp(
                                                                        processRegexEscape(
                                                                                processUnicodeEscape(reg))))))))));
    }

    private void outputStr(int i,String[] strings){
        System.out.print("before tran"+i+": ");
        for(String s:strings){
            System.out.print(s);
        }
        System.out.println();
    }
}
