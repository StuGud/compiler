package com.gud;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * lex文件三部分：
 * <p>
 * definitions
 * %%
 * rules
 * %%
 * user code
 * <p>
 * Created By Gud
 */
public class LexFileParser {

    //预定义
    private Map<String, String> def_Map = new HashMap<>();
    //正规表达式和对应的action
    private Map<String, String> exp_Map = new HashMap<>();
    //自定义程序的头文件部分
    private List<String> includeStrList = new ArrayList<>();
    //自定义程序的主体部分
    private List<String> commentStrList = new ArrayList<>();


    public LexFileParser(String filename) throws IOException {
        parseLexFile(filename);
    }

    public Map<String, String> getDef_Map() {
        return def_Map;
    }

    public Map<String, String> getExp_Map() {
        return exp_Map;
    }

    public List<String> getIncludeStrList() {
        return includeStrList;
    }

    public List<String> getCommentStrList() {
        return commentStrList;
    }

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
        String lineStr = "";

        int test = 0;

        while ((lineStr = buf.readLine()) != null) {
            line++;

            //System.out.println("line" + line + ": " + lineStr);

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
                        String s = lineStr.replaceAll("\r|\t|\n", " ");
                        String[] split = s.split(" ");
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
                        //System.out.println(++test + " " + exp_Map.size());

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
     * @param lineStr
     * @todo 待检查正确性
     * @todo Rules 对应多个？？？
     */
    private void getRegularAndFunc(String lineStr) {
        String s = lineStr.replaceAll("\r|\t|\n", " ");
        String[] temp = s.split(" ");
        //String[] temp = lineStr.split("\t");

        if (temp[0].startsWith("\"") && temp[0].endsWith("\"")) {
            //双引号 为保留字

            exp_Map.put(temp[0], lineStr.substring(temp[0].length()));
        } else if (temp[0].startsWith("(\"") && temp[0].endsWith("\")")) {
            //例子：   ("}"|"%>")    { count(); return('}'); }

            String[] x = temp[0].split("\\|");
            exp_Map.put(x[0].substring(1), temp[temp.length - 1]);
            exp_Map.put(x[1].substring(0, x[1].length() - 1), temp[temp.length - 1]);
        } else {
            String replacedExp = replacePredefinedElements(temp[0]);
            //if replacedExp!="\r" {
            //	exp_Map[replacedExp] = temp[len(temp)-1]
            //}
            if (!replacedExp.equals("")) {
                exp_Map.put(replacedExp, lineStr.substring(temp[0].length()));
            }
        }


    }

    /**
     * 对于多层定义的处理
     * 分层定义 替换
     * 当且仅当 满足其中有格式{D},而且D已定义
     */
    private void replaceDef() {
        boolean flag = true;
        while (flag) {
            flag = false;
            for (Map.Entry<String, String> entry1 : def_Map.entrySet()) {
                String target = "\\{" + entry1.getKey() + "}";
                String replace = entry1.getValue();
                for (Map.Entry<String, String> entry2 : def_Map.entrySet()) {
                    if(entry1.getKey()!=entry2.getKey()){
                        if (entry2.getValue().indexOf(entry1.getKey()) != -1) {
                            //entry2.setValue(entry2.getValue().replaceAll(target,replace));
                            def_Map.put(entry2.getKey(), entry2.getValue().replaceAll(target,replace));
                            flag=true;
                        }
                    }
                }
            }
        }
    }

    private String replacePredefinedElements(String exp) {
        String replaced = exp;
        for (Map.Entry<String, String> entry : def_Map.entrySet()) {
            String target = "\\{" + entry.getKey() + "}";
            String replace = "(" + entry.getValue() + ")";
            if (replaced.indexOf(entry.getKey()) != -1) {
                replaced = replaced.replaceAll(target, replace);
            }
        }
        return replaced;
    }
}
