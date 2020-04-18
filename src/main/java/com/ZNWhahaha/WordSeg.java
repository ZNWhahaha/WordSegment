package com.ZNWhahaha;

import com.sun.tools.javac.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Dictionary;
import java.util.List;

public class WordSeg {

    //训练集文件路径
    private String train_path;
    //测试集文件路径
    private String test_path;

    private Dictionary count;
    private Dictionary cnt;
    private List words;

    /**
     * @ClassName : WordSeg
     * @Description : 序列标注
     * @param i
     * @param lens
     * @Return : char
     * @Author : ZNWhahaha
     * @Date : 2020/3/27
    */
    private char findIndex(int i,int lens){
        if(lens == 1){
            return 'S';
        }

        if (i == 0){
            return 'B';
        }
        else if (i == lens -1){
            return 'E';
        }

        return 'M';
    }

    private  void training(WordSeg ws){
        try {

            System.out.println("training ...");
            //清洗空格用
            String a = " ";
            String strs = "";
            //读文件编码设置
            String encoding="utf8";
            //存储读出的文本
            String lineTxt = null;
            //存储初步分词
            String[] group;
            String word;
            int lens;
            File file=new File(ws.train_path);
            if (file.isFile() && file.exists()) {

                //读文件操作
                InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);
                BufferedReader bufferedReader = new BufferedReader(read);

                while((lineTxt = bufferedReader.readLine()) != null){
                    //简单的数据清洗
                    lineTxt = lineTxt.replace(" \n","");
                    lineTxt = lineTxt.replace("\n","");
                    group = lineTxt.split(a);
                    for (String scen:group){
                        scen = scen.trim();
                        String[] scens = scen.split("");
                        lens = scens.length;
                        for (int i = 0; i < lens; i++) {
                            word = scens[i];
                            if (!words.contains(word)){
                                 ws.words.add(word);
                            }
                            int st = ws.findIndex(i,lens);
                            ws.cnt.put(st,0);
                            
                        }


                    }

                }
            }
        }catch (Exception e){
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        

    }

    private void init (WordSeg ws, File TrainingFIle, File TestFile){
        
    }

    public static void main( String[] args )
    {

    }
}
