package com.gud;

import com.gud.struct.ACTION_TYPE;
import com.gud.struct.TableItem;
import com.gud.struct.TransitionItem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created By Gud on 2021/1/9 12:18 上午
 */
public class CppBuilder {

    int indexSymbol;
    Map<String, Integer> nonTerminalMap = new HashMap<>();
    Vector<String> words = new Vector<>();

    List<TransitionItem> lr1StateTransitionList;
    List<List<TableItem>> lr1ParseTable;

    Map<Integer, List<TableItem>> lalr1ParseTable;
    Map<Integer, TransitionItem> lalr1StateTransitionMap;

    private void outputTable() throws IOException {

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
                for (int edge = 0; edge < indexSymbol; edge++) {
                    if (entry.getValue().get(edge).getAction() != ACTION_TYPE.ERROR) {
                        sb.append("tran.insert(std::make_pair(\"" + words.get(edge)
                                + "\", TableItem(" + entry.getValue().get(edge).getAction()
                                + entry.getValue().get(edge).getIndex() + ")));\n");
                    }
                    sb.append("_parseTable.insert(std::make_pair(" + entry.getKey() + ",tran));\n");
                    sb.append("tran.swap(std::map<std::string, TableItem>());\n\n");
                }
            }
        } else {
            for (int i = 0; i < lr1ParseTable.size(); i++) {
                sb.append("// state" + i + "\n");
                List<TableItem> row = lr1ParseTable.get(i);
                for (int edge = 0; edge < indexSymbol; edge++) {
                    if (row.get(edge).getAction() != ACTION_TYPE.ERROR) {
                        sb.append("tran.insert(std::make_pair(\"" + words.get(edge)
                                + "\", TableItem(" + row.get(edge).getAction()
                                + row.get(edge).getIndex() + ")));\n");
                    }
                    sb.append("_parseTable.insert(std::make_pair(" + i + ",tran));\n");
                    sb.append("tran.swap(std::map<std::string, TableItem>());\n\n");
                }
            }
        }

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

