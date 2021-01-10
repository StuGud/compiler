package com.gud;

import com.gud.struct.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created By Gud on 2021/1/9 12:18 上午
 */
public class CppBuilder {

    int symbolIndex;
    Map<String, Integer> nonTerminalMap = new HashMap<>();
    List<String> symbols = new ArrayList<>();

    List<TransitionItem> lr1StateTransitionList = new ArrayList<>();
    List<List<TableItem>> lr1ParseTable = new ArrayList<>();

    Map<Integer, List<TableItem>> lalr1ParseTable = new HashMap<>();
    Map<Integer, TransitionItem> lalr1StateTransitionMap = new HashMap<>();

    //产生式以及对应的action语法制导翻译
    List<ProductionItem> productionItemDeque = new ArrayList<>();
    Map<Integer, String> productionActionMap = new HashMap<>();

    public CppBuilder(LR1Builder lr1Builder) {
        symbolIndex = lr1Builder.symbolIndex;
        nonTerminalMap = lr1Builder.nonTerminalMap;
        symbols = lr1Builder.symbols;
        lr1StateTransitionList = lr1Builder.lr1StateTransitionList;
        lr1ParseTable = lr1Builder.lr1ParseTable;

    }

    //@todo

    /**
     * 只输出LR1
     */
    public void outputLRStateSet() {
        StringBuilder sb = new StringBuilder();
        sb.append("LR1数量：" + lr1StateTransitionList.size() + "\n");

        int stateIndex = 0;
        for (TransitionItem transitionItem : lr1StateTransitionList) {
            sb.append(stateIndex++ + ":" + "\n");
            for (LR1Item lr1Item : transitionItem.getLr1State().getItemList()) {
                ProductionItem production = lr1Item.getProd();
                List<Integer> productionBody = production.getBody();

                sb.append(symbols.get(production.getHead()) + " -> ");

                for (int i = 0; i < lr1Item.getDotLoc(); i++) {
                    sb.append(symbols.get(productionBody.get(i)) + " ");
                }
                sb.append(" · ");
                for (int i = lr1Item.getDotLoc(); i < productionBody.size(); i++) {
                    sb.append(symbols.get(productionBody.get(i)) + " ");
                }

                sb.append(" , ");

                //预测符
                for (Integer pred : lr1Item.getPredSet()) {
                    sb.append(symbols.get(pred) + "|");
                }
                sb.deleteCharAt(sb.length() - 1);

                sb.append("\n");
            }
            sb.append("\n\n");
        }

        writeFile("LR1_state_set.txt", sb.toString());

    }

    public void outputParseTable() {

        StringBuilder sb = new StringBuilder();

        int stateIndex = 0;
        for (List<TableItem> row : lr1ParseTable) {
            sb.append("state " + stateIndex++ + "\n");
            for (int edge = 0; edge < symbols.size(); edge++) {
                switch (row.get(edge).getAction()) {
                    case SHIFT: {
                        sb.append(symbols.get(edge) + " Shift " + row.get(edge).getIndex() + "\n");
                        break;
                    }
                    case REDUCTION: {
                        sb.append(symbols.get(edge) + " Reduce " + row.get(edge).getIndex() + "\n");
                        break;
                    }
                    case ACCEPT: {
                        sb.append(symbols.get(edge) + " Accept " + "\n");
                        break;
                    }
                    case GOTO_STATE: {
                        sb.append(symbols.get(edge) + " GOTO " + row.get(edge).getIndex() + "\n");
                        break;
                    }
                }
            }
            sb.append("\n");
        }

        writeFile("LR1_parse_table.txt", sb.toString());
    }

    private void outputNonterminal() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> entry : nonTerminalMap.entrySet()) {
            sb.append(entry.getKey() + " " + entry.getValue() + "\n");
        }
        writeFile("LR1_nonterminal_set.txt", sb.toString());
    }

    public void outputTable() {

        StringBuilder sb = new StringBuilder();
        sb.append("#ifndef _TABLE_YACC_H\n");
        sb.append("#define _TABLE_YACC_H\n");
        sb.append("#include \"StructDefine.h\"\n");
        sb.append("#include <vector>\n");
        sb.append("#include <map>\n");
        sb.append("void initTable(std::map<unsigned int, std::map<std::string, TableItem> >& _parseTable) {\n");
        sb.append("std::map<std::string, TableItem> tran;\n");

        if (!lalr1ParseTable.isEmpty()) {
            for (Map.Entry<Integer, List<TableItem>> entry : lalr1ParseTable.entrySet()) {
                sb.append("// state" + entry.getKey() + "\n");
                for (int edge = 0; edge < symbolIndex; edge++) {
                    if (entry.getValue().get(edge).getAction() != ACTION_TYPE.ERROR) {
                        sb.append("tran.insert(std::make_pair(\"" + symbols.get(edge)
                                + "\", TableItem(" + entry.getValue().get(edge).getAction()
                                + entry.getValue().get(edge).getIndex() + ")));\n");
                    }
                }
                sb.append("_parseTable.insert(std::make_pair(" + entry.getKey() + ",tran));\n");
                sb.append("tran.swap(std::map<std::string, TableItem>());\n\n");
            }
        } else {
            for (int i = 0; i < lr1ParseTable.size(); i++) {
                sb.append("// state" + i + "\n");
                List<TableItem> row = lr1ParseTable.get(i);
                for (int edge = 0; edge < symbolIndex; edge++) {
                    if (row.get(edge).getAction() != ACTION_TYPE.ERROR) {
                        sb.append("tran.insert(std::make_pair(\"" + symbols.get(edge)
                                + "\", TableItem(" + row.get(edge).getAction()
                                + row.get(edge).getIndex() + ")));\n");
                    }
                }
                sb.append("_parseTable.insert(std::make_pair(" + i + ",tran));\n");
                sb.append("tran.swap(std::map<std::string, TableItem>());\n\n");
            }
        }
        sb.append("}\n");
        sb.append("#endif //_TABLE_YACC_H\n");

        writeFile("tableYacc.h", sb.toString());
    }


    private void writeFile(String filename, String data) {
        try {
            File file = new File(filename);

            //if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            //true = append file
            FileWriter fileWriter = new FileWriter(file.getName(), true);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);

            bufferWriter.write(data);

            bufferWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 用于中间代码生成（三地址码）
    // 根据语法对每一个产生式有一个动作存储在productionAction中
    // 当读到特定产生式时进行填写下一些状态和回填
    //语法制导翻译
    private void translateAction() {
        for (Map.Entry<Integer, String> entry : productionActionMap.entrySet()) {
            char[] actionSplit = entry.getValue().toCharArray();
            String translatedAction = "";
            for (int i = 0; i < actionSplit.length; i++) {
                if ('$' == actionSplit[i]) {
                    i++;
                    if ('$' == actionSplit[i]) {
                        //以$$开头
                        //跳过$$.中的.
                        i += 2;
                        String s = "";
                        while (isAlpha(actionSplit[i])) {
                            s += actionSplit[i];
                            i++;
                        }
                        translatedAction += "reduceHead[\"" + s + "\"]";
                    } else if (isDigit(actionSplit[i])) {
                        int pos = actionSplit[i] - '0';
                        i++;
                        while (isDigit(actionSplit[i])) {
                            pos = 10 * pos + (actionSplit[i] - '0');
                            i++;
                        }
                        //跳过.
                        i++;
                        String s = "";
                        while (isAlpha(actionSplit[i])) {
                            s += actionSplit[i];
                            i++;
                        }
                        if ("width".equals(s) || "lexval".equals(s)) {
                            translatedAction += "atoi(stack[stacksize - " + productionItemDeque.get(entry.getKey()).getBodyLength()
                                    + " + " + pos + "]._map[\"" + s + "\"].c_str()";
                        } else {
                            translatedAction += "stack[stackSize - " + productionItemDeque.get(entry.getKey()).getBodyLength()
                                    + " + " + pos + "]._map[\"" + s + "\"]";
                        }
                    }
                    continue;
                } else if (';' == actionSplit[i]) {
                    translatedAction += ";\n";
                    i++;
                } else if ('|' == actionSplit[i] && '|' == actionSplit[i]) {
                    translatedAction += " + ";
                    i += 2;
                } else {
                    translatedAction += actionSplit[i];
                    i++;
                }
            }
            productionActionMap.put(entry.getKey(), translatedAction);
        }
    }

    public void outputAction() {
        translateAction();

        StringBuilder sb = new StringBuilder();
        sb.append("#ifndef _ACTION_YACC_H\n");
        sb.append("#define _ACTION_YACC_H\n");
        sb.append("#include \"structDefine.h\"\n");
        sb.append("#include \"supportFunction.h\"\n");
        sb.append("#include <string>\n");
        sb.append("#include <stack>\n");
        sb.append("#include <vector>\n");
        sb.append("#include <stdlib.h>\n");

        sb.append("extern unsigned int offset;\n");
        sb.append("std::string p;\n");
        sb.append("extern std::deque<StackItem> stack;\n");
        sb.append("extern std::stack<std::string> paramStack;\n");

        // 构建读取字符执行动作的自动函数performAction
        sb.append("std::pair<unsigned int, std::string> performAction(unsigned int index, std::map<std::string, std::string>& reduceHead) {\n");
        sb.append("size_t stackSize = stack.size() - 1;\n");
        sb.append("switch(index) {\n");

        for (int i = 0; i < productionActionMap.size(); i++) {
            sb.append("case" + i + ":");
            // 产生式显式表示
            sb.append("//" + symbols.get(productionItemDeque.get(i).getHead()) + "->");
            for (Integer curSymbol : productionItemDeque.get(i).getBody()) {
                sb.append(symbols.get(curSymbol) + " ");
            }
            //执行该动作产生的动作
            sb.append("\n" + productionActionMap.get(i) + "\n");
            sb.append("return std::pair<unsigned int, std::string>("
                    + productionItemDeque.get(i).getBodyLength() + ",\""
                    + symbols.get(productionItemDeque.get(i).getHead()) + "\");\n\n");
        }
        sb.append("default: return std::pair<unsigned int, std::string>(0,\"\");\n");
        sb.append("}\n");
        sb.append("}// end function\n");
        // 获取产生式用于输出reduce_sequence
        sb.append("std::string getProduction(unsigned int index) {\n");
        sb.append("switch(index) {\n");

        for (int i = 0; i < productionItemDeque.size(); i++) {
            sb.append("case " + i + " :");
            sb.append("return \"" + symbols.get(productionItemDeque.get(i).getHead()) + "->");
            for (Integer curSymbol : productionItemDeque.get(i).getBody()) {
                sb.append(symbols.get(curSymbol) + " ");
            }
            sb.append("\";\n");
        }
        sb.append("default: return \"\";\n");
        sb.append("}\n");
        sb.append("}\n");
        sb.append("#endif //_ACTION_YACC_H\n");

        //

        sb.append("struct node {\n");
        sb.append("std::string name;\n");
        sb.append("std::vector<string> son;\n");
        sb.append("node(string s):name(s){}\n");
        sb.append("};\n");
        sb.append("std::string getTree(unsigned int index) {\n");
        sb.append("std::vector<node> tree;\n");
        sb.append("switch(index) {\n");
        for (int i = 0; i < productionItemDeque.size(); i++) {
            sb.append("case " + i + " :");
            sb.append("node n(" + symbols.get(productionItemDeque.get(i).getHead()) + ")");
            for (Integer curSymbol : productionItemDeque.get(i).getBody()) {
                sb.append(symbols.get(curSymbol) + " ");
            }
            sb.append("\";\n");
        }
        sb.append("default: return \"\";\n");
        sb.append("}\n");
        sb.append("}\n");
        sb.append("#endif //_ACTION_YACC_H\n");

        writeFile("actionYacc.h", sb.toString());
    }

    private boolean isAlpha(char c) {
        if ('0' <= c && c <= '9') {
            return true;
        }
        return false;
    }

    private boolean isDigit(char c) {
        if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')) {
            return true;
        }
        return false;
    }

    public void generateParseProgram(){
        StringBuilder sb=new StringBuilder();
        sb.append("#include \"lex.h\"\n");
        sb.append("#include \"yaccHelp.h\"\n");


        outputTable();
        outputAction();
        outputParseTable();
        outputLRStateSet();
        outputNonterminal();


        sb.append("int main(int argc, char *argv[]) {\n");
        sb.append("if (argc < 2) {\n");
        sb.append("std::cout << \"please input the target source file name\" << std::endl;\n");
        sb.append("return 1;\n");
        sb.append("}\n");
        sb.append("std::list<Token> tokenlist;\n");
        sb.append("lexParse(argv[1], tokenlist);\n");
        sb.append("yaccReduce(tokenlist);\n");
        sb.append("std::cout << \"compile success\" << std::endl;\n");
        sb.append("return 0;\n");
        sb.append("}");

        writeFile("yacc.c",sb.toString());
    }
}

