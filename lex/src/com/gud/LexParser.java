package com.gud;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * lex文件三部分：
 * definitions
 * %%
 * rules
 * %%
 * user code
 * <p>
 * Created By Gud on 2020/12/29 12:28 上午
 */
public class LexParser {

    //
    Map<String, String> def_Map = new HashMap<>();
    Map<String, String> exp_Map = new HashMap<>();
    List<String> includeStrList = new ArrayList<>();
    List<String> commentStrList = new ArrayList<>();

    /**
     * @param filename
     * @throws IOException
     * @todo outPut可以使用StringBuilder
     */
    public void parseLexFile(String filename) throws IOException {
        File file = new File(filename);
        BufferedReader buf = new BufferedReader(new FileReader(file));

        //lex文件总共三部分
        int state = 0;
        int line = 0;
        String lineStr = null;

        int test=0;

        while ((lineStr = buf.readLine()) != null) {
            line++;
            System.out.println("line" + line + ": " + lineStr);

            switch (state) {
                case 0: {
                    if (lineStr.startsWith("%{")) {
                        //def扫描结束，进行一些替换
                        replaceDef();
                        state = 1;
                    } else if (lineStr.startsWith("%%")) {
                        state = 2;
                    } else if (lineStr.equals(System.lineSeparator())) {
                        break;
                    } else {
                        String[] split = lineStr.split("\t");
                        String exTemp = split[split.length - 1].split(System.lineSeparator())[0];
                        if (split[0].length() > 0) {
                            def_Map.put(split[0], exTemp);
                        }
                    }
                    break;
                }
                case 1: {
                    if (lineStr.startsWith("%}")) {
                        state = 0;
                    } else {
                        if (lineStr.startsWith("#")) {
                            includeStrList.add(lineStr);
                        } else {
                            commentStrList.add(lineStr);
                        }
                    }
                    break;
                }
                case 2: {
                    if (lineStr.startsWith("%%")) {
                        //getRegularAndFunc(outPut);
                        state = 3;
                    } else if (lineStr.equals("\n")) {
                        break;
                    } else {
                        //outPut += lineStr;
                        getRegularAndFunc(lineStr);
                        System.out.println(++test+" "+exp_Map.size());

                    }
                    break;
                }
                case 3: {
                    commentStrList.add(lineStr);
                    break;
                }
                default: {
                    System.out.println("line" + line + "结构不完整");
                    break;
                }
            }

        }
        buf.close();
    }


    /**
     * 分层定义 替换
     * 当且仅当 满足其中有格式{D},而且D已定义
     */
    private void replaceDef() {
        System.out.println("replaceDef");
        for (Map.Entry<String, String> entry : def_Map.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            for (Map.Entry<String, String> entry1 : def_Map.entrySet()) {
                String m = entry1.getKey();
                String n = entry1.getValue();
                if (!k.equals(m) && v.contains(m)) {
                    String target = "\\{" + m + "}";
                    String replace = "\\{" + n + "}";
                    def_Map.put(k, v.replaceAll(target, replace));
                }
            }
        }
    }

    /**
     * @param lineStr
     * @todo 待检查正确性
     * @todo Rules 对应多个？？？
     */
    private void getRegularAndFunc(String lineStr) {

        String[] temp = lineStr.split("\t");

        //1. 双引号 为保留字
        //2.
        if (temp[0].startsWith("\"") && temp[0].endsWith("\"")) {
            exp_Map.put(temp[0], temp[temp.length - 1]);
        } else if (temp[0].startsWith("(\"") && temp[0].endsWith("\")")) {
            String[] x = temp[0].split("\\|");
            exp_Map.put(x[0].substring(1), temp[temp.length - 1]);
            exp_Map.put(x[1].substring(0, x[1].length() - 1), temp[temp.length - 1]);
        } else {
            String replacedExp = replacePredefinedElements(temp[0]);
            //if replacedExp!="\r" {
            //	exp_Map[replacedExp] = temp[len(temp)-1]
            //}
            if (!replacedExp.equals("")) {
                exp_Map.put(replacedExp, temp[temp.length - 1]);
            }
        }


    }

    private String replacePredefinedElements(String exp) {
        String replaced = exp;
        for (Map.Entry<String, String> entry : def_Map.entrySet()) {
            if (replaced.indexOf(entry.getKey()) != -1) {
                String target = "\\{" + entry.getKey() + "}";
                String replace = "\\{" + entry.getValue() + "}";
                replaced = replaced.replaceAll(target, replace);
            }
        }
        replaced = replaced.replaceAll("\\{", "(");
        replaced = replaced.replaceAll("}", ")");
        return replaced;
    }
}
