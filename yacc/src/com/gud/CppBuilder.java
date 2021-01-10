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

    List<TransitionItem> lr1StateTransitionList=new ArrayList<>();
    List<List<TableItem>> lr1ParseTable=new ArrayList<>();

    Map<Integer, List<TableItem>> lalr1ParseTable=new HashMap<>();
    Map<Integer, TransitionItem> lalr1StateTransitionMap=new HashMap<>();

    public CppBuilder(LR1Builder lr1Builder) {
        symbolIndex= lr1Builder.symbolIndex;
        nonTerminalMap= lr1Builder.nonTerminalMap;
        symbols=lr1Builder.symbols;
        lr1StateTransitionList=lr1Builder.lr1StateTransitionList;
        lr1ParseTable=lr1Builder.lr1ParseTable;

    }

    /**
     * 只输出LR1
     */
    public void outputLRStateSet(){
        StringBuilder sb = new StringBuilder();
        sb.append("LR1数量："+lr1StateTransitionList.size()+"\n");

        int stateIndex=0;
        for(TransitionItem transitionItem:lr1StateTransitionList){
            sb.append(stateIndex++ +":"+"\n");
            for(LR1Item lr1Item: transitionItem.getLr1State().getItemList()){
                ProductionItem production=lr1Item.getProd();
                List<Integer> productionBody= production.getBody();

                sb.append(symbols.get(production.getHead())+" -> ");

                for (int i = 0; i < lr1Item.getDotLoc(); i++) {
                    sb.append(symbols.get(productionBody.get(i))+" ");
                }
                sb.append(" · ");
                for (int i = lr1Item.getDotLoc(); i < productionBody.size(); i++) {
                    sb.append(symbols.get(productionBody.get(i))+" ");
                }

                sb.append(" , ");

                //预测符
                for (Integer pred: lr1Item.getPredSet()){
                    sb.append(symbols.get(pred)+"|");
                }
                sb.deleteCharAt(sb.length()-1);

                sb.append("\n");
            }
            sb.append("\n\n");
        }

        writeFile("LR1_state_set.txt", sb.toString());

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

    private void outputNonterminal(){
        StringBuilder sb=new StringBuilder();
        for (Map.Entry<String,Integer> entry:nonTerminalMap.entrySet()){
            sb.append(entry.getKey()+" "+entry.getValue()+"\n");
        }
        writeFile("LR(1)_nonterminal.txt",sb.toString());
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
}

