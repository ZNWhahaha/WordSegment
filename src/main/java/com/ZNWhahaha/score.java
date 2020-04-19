package com.ZNWhahaha;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class score {

    public static void main(String[] args) throws IOException {

        int count = 1;
        int count_right = 0;
        int count_split = 0;
        int count_gold = 0;
        int process_count = 0;
        WordSegment ws = new WordSegment();
        ws.run_Train();
        for (String line : readLines("/Users/znw_mac/IdeaProjects/ChineseWordSegment0.1/TextFiles/test.txt", "utf-8")) {
            process_count += 1;
            if (process_count % 1000 == 0)
                System.out.println(process_count);
            line = line.trim();
            String[] gold = line.split(" ");
            List<String> goldlist  = Arrays.asList(gold);
            String sentence = line.replace(" ","");
            List<String> inlist = ws.run_Test(sentence);
            count += 1;
            count_split += inlist.size();
            count_gold += goldlist.size();
            List<String> tmp_in = inlist;
            List<String> tmp_gold = goldlist;

            for (String keyIn : tmp_in)  {
                for (String keyGO : tmp_gold)
                    if (keyIn.equals(keyGO)) {
                        count_right += 1;
                        tmp_gold = new ArrayList<String>(tmp_gold);
                        tmp_gold.remove(keyIn);
                    }
            }
        }
        

        Double P = count_right / (count_split * 1.0);
        Double R = count_right / (count_gold * 1.0);
        Double F = 2 * P * R / (P + R);

        System.out.println("P = "+ P);
        System.out.println("R = "+ R);
        System.out.println("F = "+ F);
    }



    /**
     * @ClassName : score
     * @Description : 按行读取文本
     * @param path
     * @param encoding
     * @Return : java.util.List<java.lang.String>
     * @Author : ZNWhahaha
     * @Date : 2020/4/10
    */
    public static List<String> readLines(String path, String encoding){
        if(encoding == null)
            encoding = "utf-8";
        List<String> re = new ArrayList<String>();
        try {
            InputStreamReader in = new InputStreamReader(new FileInputStream(path), encoding);
            BufferedReader reader = new BufferedReader(in);

            String line = null;
            while((line = reader.readLine()) != null) {
                re.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return re;
    }
}
