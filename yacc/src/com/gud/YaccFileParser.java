package com.gud;

import com.gud.struct.ProductionItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created By Gud on 2021/1/2 7:56 下午
 */
public class YaccFileParser {

    //所有符号
    List<String> symbols = new ArrayList<>();

    //终结符部分
    Map<String, Integer> terminalMap = new HashMap<>();
    //@todo meaning?
    Map<Integer, Boolean> leftAssociativeMap = new HashMap<>();
    Map<Integer, Integer> priorityMap = new HashMap<>();

    //非终结符部分
    Map<String, Integer> nonTerminalMap = new HashMap<>();
    Map<Integer, Boolean> isNonTerminalNullableMap = new HashMap<>();

    //产生式以及对应的action语法制导翻译
    List<ProductionItem> productionItemDeque = new ArrayList<>();
    Map<Integer, String> productionActionMap = new HashMap<>();

    int symbolIndex = 0;

    //.y文件里面的预定义程序部分
    String predefinedProgram;

    public void parseYaccFile(String filename) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        String[] segments = content.split("%%");
        if (segments.length != 3) {
            throw new IOException("YACC文件不完整");
        }

        System.out.println("开始解析.y文件");

        parseDefineSegment(segments[0]);
        parseGrammarSegment(segments[1]);
        parseProgramSegment(segments[2]);

        System.out.println(".y文件解析完成");
    }


    private enum TokenType {
        TOKEN, LEFT, RIGHT, START, NONE
    }

    private int saveSymbol(String symbol, Map<String, Integer> map) {
        if (map.containsKey(symbol)) {
            return map.get(symbol);
        } else {
            map.put(symbol, symbolIndex);
            symbols.add(symbol);
            ++symbolIndex;
            return symbolIndex - 1;
        }
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

            String line = lines[i].replaceAll("\t|\n|\r", " ");
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
                    int curSymbolIndex = saveSymbol(s, terminalMap);
                    priorityMap.put(curSymbolIndex, curPriority);
                    leftAssociativeMap.put(curSymbolIndex, tokenType == TokenType.LEFT);
                }
            }
        }
    }

    /**
     * 去除单引号
     *
     * @param s
     * @return
     */
    private String removeSingleQuote(String s) {
        if (s != null && s.startsWith("\'") && s.endsWith("\'") && s.length() > 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    private void parseGrammarSegment(String grammarSegment) {

        String[] lines = grammarSegment.split(System.lineSeparator());

        int state = 0;
        //匹配 { }
        int braceMatchFlag = 0;
        int productionHead = -1;


        int numOfProd = 0;

        for (int i = 0; i < lines.length; i++) {

            //System.out.println("line:"+i);

            String line = lines[i].replaceAll("\t|\n|\r", " ");
            if ("".equals(line)) {
                continue;
            }
            switch (state) {
                case 0: {
                    //读取产生式头部
                    line = line.replaceAll(" ", "");
                    if ("".equals(line)) {
                        continue;
                    }
                    productionHead = saveSymbol(line, nonTerminalMap);

                    //默认新保存的非终结符不产生空串
                    isNonTerminalNullableMap.put(productionHead, false);
                    state = 1;
                    break;
                }

                case 1: {
                    //读取产生式
                    String[] split = line.split(" ");
                    for (int j = 0; j < split.length; j++) {
                        if ("".equals(split[j])) {
                            continue;
                        }

                        if (":".equals(split[j]) || "|".equals(split[j])) {
                            List<Integer> productionBody = new ArrayList<>();
                            //产生式
                            j++;
                            while (j < split.length) {

                                if ("".equals(split[j])) {
                                    j++;
                                    continue;
                                }
                                //是非终结符，还是终结符
                                int curSymbolIndex = 0;
                                if (!split[j].startsWith("\'")) {
                                    curSymbolIndex = saveSymbol(split[j], nonTerminalMap);
                                    isNonTerminalNullableMap.put(curSymbolIndex, false);
                                } else {
                                    String s = removeSingleQuote(split[j]);
                                    curSymbolIndex = saveSymbol(s, terminalMap);
                                }
                                productionBody.add(curSymbolIndex);
                                j++;
                            }

                            //保存产生式
                            productionItemDeque.add(new ProductionItem(productionHead, productionBody, productionBody.size(), numOfProd++));

                            //判断产生式是否产生空串
                            if (productionBody.isEmpty()) {
                                isNonTerminalNullableMap.put(productionHead, true);
                            }
                            //productionBody.clear();

                        } else if ("{".equals(split[j])) {
                            //action part 定义动作
                            //中间代码
                            braceMatchFlag++;
                            StringBuilder sb = new StringBuilder();
                            j++;
                            while (j < split.length) {
                                if ("}".equals(split[j]) && braceMatchFlag == 1) {
                                    --braceMatchFlag;
                                    //@todo 注意numOfProd-1
                                    productionActionMap.put(numOfProd-1, sb.toString());
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
        saveSymbol("$",terminalMap);
    }


    private void parseProgramSegment(String programSegment) {
        predefinedProgram =programSegment;
    }
}
