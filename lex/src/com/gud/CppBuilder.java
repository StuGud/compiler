package com.gud;

import com.gud.struct.DFA;
import com.gud.struct.DFAState;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created By Gud
 */
public class CppBuilder {

    DFA dfa;
    Map<DFAState, Set<String>> endFuncMap = new HashMap<>();
    Map<Integer, DFAState> stateMap = new HashMap<>();


    public CppBuilder(DFA dfa) {
        this.dfa = dfa;
        endFuncMap = dfa.getEndFuncMap();
        stateMap = dfa.getStateMap();
    }

    public void outputProgram() {
        outputTable();
        outputAction();
    }

    private void outputAction() {

        StringBuilder sb = new StringBuilder();

        sb.append("#ifndef _ACTION_LEX_H\n");
        sb.append("#define _ACTION_LEX_H\n");
        sb.append("#include <set>\n");

        sb.append("void initFinalSet(std::set<unsigned int>& finalSet) {\n");
        for (Map.Entry<DFAState, Set<String>> entry : dfa.getEndFuncMap().entrySet()) {
            sb.append("finalSet.insert(" + entry.getKey().getId() + ");\n");
        }
        sb.append("}\n\n");

        sb.append("std::string performAction(unsigned int state) {\n");
        sb.append("switch(state) {\n");
        for (Map.Entry<DFAState, Set<String>> entry : dfa.getEndFuncMap().entrySet()) {
            sb.append("case " + entry.getKey().getId() + ": {");

            if (entry.getValue().size() == 1) {
                for (String endFunc : entry.getValue()) {
                    sb.append(endFunc);
                    //System.out.println("endFunc"+endFunc);
                }
            } else {
                for (String endFunc : entry.getValue()) {
                    if ((!endFunc.contains("id"))) {
                        sb.append(endFunc);
                        //System.out.println("endFunc"+endFunc);
                    }
                    if ((endFunc.contains("void"))) {
                        sb.append(endFunc);
                        //System.out.println("endFunc"+endFunc);
                    }

                }
            }

//                StringBuilder temp=new StringBuilder();
//                for(String endFunc: entry.getValue()){
//                    temp.append(endFunc);
//                }
//                System.out.println("entrySet中函数个数大于一:"+temp.toString());


            sb.append("\n}\n");
        }
        sb.append("default: return \"\";\n");
        sb.append("}\n");
        sb.append("}// end function\n");

        sb.append("#endif //_ACTION_LEX_H\n");

        writeFile("output/actionLex.h", sb.toString());
    }

    private void outputTable() {
        StringBuilder sb = new StringBuilder();

        sb.append("#ifndef _TABLE_LEX_H\n");
        sb.append("#define _TABLE_LEX_H\n");
        sb.append("#include <vector>\n");
        sb.append("#include <map>\n");

        sb.append("void initMinDFAStateTranfer(std::vector<std::map<char, unsigned int> >* _minDFAStateTranfer) {\n");
        sb.append("std::map<char, unsigned int> tran;\n");

        //@todo
        for (int i = 0; i < dfa.getStateMap().size(); i++) {
            sb.append("// state " + i + ";\n");
            System.out.println(i);
            for (Map.Entry<Integer, DFAState> entry : dfa.getStateMap().get(i).getEdge2StateMap().entrySet()) {
                sb.append("tran.insert(std::make_pair(\'" + (char) (int) entry.getKey() + "\'," + entry.getValue().getId() + "));\n");
            }
            sb.append("_minDFAStateTranfer->push_back(tran);\n");
            sb.append("tran.clear();\n\n");
        }
        sb.append("}\n");

        sb.append("#endif //_TABLE_LEX_H\n");

        writeFile("output/tableLex.h", sb.toString());
    }

    /**
     * @param dfa
     * @param lexFileParser
     * @deprecated 废弃
     */
    public void buildCpp(DFA dfa, LexFileParser lexFileParser) {
        StringBuilder sb = new StringBuilder();

        List<String> includeStrList = lexFileParser.getIncludeStrList();
        for (String s : includeStrList) {
            sb.append(s + "\n");
        }

        String head = "#include <iostream>\n" +
                "#include <string>\n" +
                "#include <fstream>\n" +
                "#include <streambuf>\n" +
                "\n" +
                "std::string inputSrc;\n" +
                "\n" +
                "   int cp = 0;\n" +
                "   int state = 0;\n" +
                "   int keyState=0;//关键字对应的state\n" +
                "\n" +
                "   void dfa(char c);\n" +
                "\n" +
                "   int main(int argc, const char * argv[]) {\n" +
                "       std::ifstream f(argv[1]);\n" +
                "       std::string str((std::istreambuf_iterator<char>(f)),\n" +
                "                        std::istreambuf_iterator<char>());\n" +
                "       inputSrc = str;\n" +
                "       while (cp < inputSrc.length()) {\n" +
                "           try {\n" +
                "               dfa(inputSrc[cp]);\n" +
                "           } catch(int e) {\n" +
                "               std::cout << \"出现错误\";\n" +
                "               break;\n" +
                "           }\n" +
                "       }\n" +
                "       return 0;\n" +
                "   }";
        sb.append(head);

        List<String> commentStrList = lexFileParser.getCommentStrList();
        //@todo 这边格式注意一下
        for (String s : commentStrList) {
            sb.append(s + "\n");
        }

        for (Map.Entry<Integer, DFAState> entry : dfa.getStateMap().entrySet()) {

        }


    }

    private void writeFile(String filename, String data) {
        try {
            File file = new File(filename);

            //if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            //true = append file
            FileWriter fileWriter = new FileWriter(filename, true);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);

            bufferWriter.write(data);

            bufferWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
