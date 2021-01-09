package com.gud;

import com.gud.struct.ProductionItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created By Gud on 2021/1/2 7:56 下午
 */
public class YaccFileParser {

    Map<String, Integer> nonTerminalMap = new HashMap<>();
    Map<String, Integer> terminalMap = new HashMap<>();
    Map<Integer, Boolean> isNonTerminalNullableMap = new HashMap<>();
    //@todo meaning?
    Map<Integer, Boolean> leftAssociativeMap = new HashMap<>();

    Map<Integer, Integer> priorityMap = new HashMap<>();

    Vector<String> words = new Vector<>();

    Map<Integer, String> productionActionMap = new HashMap<>();
    List<ProductionItem> productionItemDeque = new ArrayList<>();

    int indexSymbol = 0;


    public void parseYaccFile(String filename) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        String[] segments = content.split("%%");
        if (segments.length != 3) {
            throw new IOException("YACC文件不完整");
        }

        parseDefineSegment(segments[0]);
        parseGrammarSegment(segments[1]);
        parseProgramSegment(segments[2]);
    }


    /**
     * 读取 说明部分； 需要解析 左右结合、优先级
     *
     * @param defineSegment
     */
    private void parseDefineSegment(String defineSegment) {
        int curPriority = 0;
        String[] lines = defineSegment.split(System.lineSeparator());
        for (int i = 0; i < lines.length; i++) {

            String line = lines[i].replaceAll("\t", " ");
            //@todo 分隔符可能存在问题
            String[] split = line.split(" ");

            TokenType tokenType = TokenType.NONE;
            if (split.length > 0) {
                if ("%token".equals(split[0])) {
                    tokenType = TokenType.TOKEN;
                } else if ("%left".equals(split[0])) {
                    tokenType = TokenType.LEFT;
                } else if ("%right".equals(split[0])) {
                    tokenType = TokenType.RIGHT;
                } else if ("%start".equals(split[0])) {
                    tokenType = TokenType.START;
                } else {
                    continue;
                }
            }

            curPriority++;
            for (int j = 1; j < split.length; j++) {
                if (!"".equals(split[j])) {
                    //去除单引号
                    String s = removeSingleQuote(split[j]);
                    terminalMap.put(s, indexSymbol);
                    priorityMap.put(indexSymbol, curPriority);
                    leftAssociativeMap.put(indexSymbol, tokenType == TokenType.LEFT);
                    words.add(s);
                    ++indexSymbol;
                }
            }
        }
    }

    private String removeSingleQuote(String s) {
        if (s != null && s.startsWith("\'") && s.endsWith("\'") && s.length() > 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    private void parseGrammarSegment(String grammarSegment) {

        int state = 0;
        //匹配 { }
        int braceMatchFlag = 0;
        int productionHead = -1;
        String[] lines = grammarSegment.split(System.lineSeparator());
        List<Integer> productionBody = new ArrayList<>();

        int numOfProd = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].replaceAll("\t", " ");
            if ("".equals(line)) {
                continue;
            }
            switch (state) {
                case 0: {
                    //读取产生式头部
                    line = line.replaceAll(" ", "");
                    if ("".equals(line)) {
                        continue;
                    } else {
                        if (nonTerminalMap.containsKey(line)) {
                            productionHead = nonTerminalMap.get(line);
                        } else {
                            nonTerminalMap.put(line, indexSymbol);
                            //默认新保存的非终结符不产生空串
                            isNonTerminalNullableMap.put(indexSymbol, false);
                            words.add(line);
                            productionHead = indexSymbol;
                            ++indexSymbol;
                        }
                        state = 1;
                    }
                    break;
                }
                case 1: {
                    //读取产生式
                    String[] split = line.split(" ");
                    System.out.println(split);
                    for (int j = 0; j < split.length; j++) {
                        if ("".equals(split[j])) {
                            continue;
                        }
                        if (":".equals(split[j]) || "|".equals(split[j])) {
                            //产生式
                            j++;
                            while (j < split.length) {
                                String s = removeSingleQuote(split[j], productionBody);
                                if ("".equals(s)) {
                                    continue;
                                }
                                if(!split[j].startsWith("\'")){
                                    if (nonTerminalMap.containsKey(s)) {
                                        productionBody.add(nonTerminalMap.get(s));
                                    } else {
                                        nonTerminalMap.put(s, indexSymbol);
                                        isNonTerminalNullableMap.put(indexSymbol, false);
                                        words.add(s);
                                        productionBody.add(indexSymbol);
                                        ++indexSymbol;
                                    }
                                }
                                j++;
                            }
                            //保存产生式
                            productionItemDeque.add(new ProductionItem(productionHead, productionBody, productionBody.size(), numOfProd++));

                            //@todo 判断产生式是否产生空串
                            if(productionBody.isEmpty()){
                                isNonTerminalNullableMap.put(productionHead,true);
                            }

                            productionBody.clear();

                        } else if ("{".equals(split[j])) {
                            //action part 定义动作
                            //中间代码
                            braceMatchFlag++;
                            StringBuilder sb = new StringBuilder();
                            j++;
                            while (j < split.length) {
                                if ("}".equals(split[j]) && braceMatchFlag == 1) {
                                    --braceMatchFlag;
                                    productionActionMap.put(numOfProd, sb.toString());
                                } else {
                                    sb.append(split[j]);
                                    sb.append(" ");
                                    if ("{".equals(split[j])) {
                                        braceMatchFlag++;
                                    } else if ("}".equals(split[j])) {
                                        braceMatchFlag--;
                                    }

                                }
                                j++;
                            }

                        } else if (";".equals(split[j])) {
                            state = 0;
                        } else {
                            break;
                        }
                        //for循环结束
                    }
                    break;
                }
                default: {
                    System.out.println("wrong!");
                }
            }
        }

        //@todo translateAction

        terminalMap.put("$",indexSymbol);
        words.add("$");
        indexSymbol++;



    }

    /**
     * 去除单引号;终结符
     *
     * @param s
     * @return
     */
    private String removeSingleQuote(String s, List<Integer> productionBody) {
        if (s != null && s.startsWith("\'") && s.endsWith("\'") && s.length() > 2) {
            s = s.substring(1, s.length() - 1);
            if (terminalMap.containsKey(s)) {
                productionBody.add(terminalMap.get(s));
            } else {
                terminalMap.put(s, indexSymbol);
                words.add(s);
                productionBody.add(indexSymbol);
                indexSymbol++;
            }
        }
        return s;
    }

    private void parseProgramSegment(String programSegment) {

    }

    enum TokenType {
        TOKEN, LEFT, RIGHT, START, NONE
    }




}
