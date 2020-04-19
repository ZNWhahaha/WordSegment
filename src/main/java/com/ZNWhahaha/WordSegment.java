package com.ZNWhahaha;

import java.io.*;
import java.util.*;

/**
 * @ClassName : WordSegment
 * @Description : 基于隐马尔可夫模型的中文分词器
 * @Author : ZNWhahaha
 * @Date : 2020/3/27
*/
public class WordSegment {

    //初始状态概率向量
    public double[] pi;
    //状态转移矩阵
    public double[][] A;
    //发射概率矩阵
    public double[][] B;

    //定义无穷小量
    public double infinity = (double) -Math.pow(2, 31);


    public WordSegment() {
        pi = new double[4];
        A = new double[4][4];
        B = new double[4][65536];
    }

    /**
     * @ClassName : WordSegment
     * @Description : 求和
     * @param arr

     * @Return : double
     * @Author : ZNWhahaha
     * @Date : 2020/3/27
     */
    public double sum(double[] arr) {
        double sum = 0;
        for(double a : arr) {
            sum += a;
        }
        return sum;
    }

    /**
     * @ClassName : WordSegment
     * @Description : 创建数据类
     * @param 

     * @Return :
     * @Author : ZNWhahaha
     * @Date : 2020/3/29
     */
    public static class SequenceData{
        public List<int[]> xSeqs;
        public List<int[]> ySeqs;
        public int allSeqLen;
    }

    /**
     * @ClassName : WordSegment
     * @Description : 训练
     * @param data

     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/3/27
    */
    public void train(SequenceData data) {
        for(int i = 0; i < data.xSeqs.size(); i++) {
            int[] xSeq = data.xSeqs.get(i);
            int[] ySeq = data.ySeqs.get(i);
            for(int j = 0; j < xSeq.length -1; j++) {
                pi[ySeq[j]]++;
                A[ySeq[j]][ySeq[j+1]]++;
                B[ySeq[j]][xSeq[j]]++;
            }
            //最后一个没统计到的节点
            pi[ySeq[ySeq.length -1]]++;
            B[ySeq[ySeq.length-1]][xSeq[xSeq.length-1]]++;
        }
        //概率取对数。
        logProba(data.allSeqLen);
    }


    /**
     * @ClassName : WordSegment
     * @Description : 概率取对数，
     * 概率取对数，用加法代替乘法，防止数据溢出
     * @param seqLen

     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/3/27
    */
    public void logProba(int seqLen) {
        double a = Math.log(seqLen);
        for(int i = 0; i < pi.length; i++) {
            pi[i] = Math.log(pi[i]) - a;
        }
        for(int i = 0; i < A.length; i++) {
            double sum = Math.log(sum(A[i]));
            for(int j = 0; j < A[0].length; j++) {
                if(A[i][j] == 0){
                    A[i][j] = infinity;
                }
                else
                    A[i][j] = Math.log(A[i][j]) - sum;
            }
        }
        for(int i = 0; i < B.length; i++) {
            double sum = Math.log(sum(B[i]));
            for(int j = 0; j < B[0].length; j++) {
                if(B[i][j] == 0){
                    B[i][j] = infinity;
                }
                else
                    B[i][j] = Math.log(B[i][j]) - sum;
            }
        }
    }





    /**
     * @ClassName : WordSegment
     * @Description : 解码
     * @param predict
     * @param sentence
     * @Return : java.lang.String[]
     * @Author : ZNWhahaha
     * @Date : 2020/3/29
     *
     *解码为分词结果
     *0 1 2 3
     *B M E S
    */
    public String[] decode(int[] predict, String sentence) {
        List<String> res = new ArrayList<>();
        char[] chars = sentence.toCharArray();
        for(int i = 0; i < predict.length;i++) {
            if(predict[i] == 0 || predict[i] == 1) {
                int a = i;
                while(predict[i] != 2) {
                    i++;
                    if(i == predict.length) {
                        break;
                    }
                }
                int b = i;
                if(b == predict.length) {
                    b--;
                }
                res.add(new String(chars,a,b-a+1));
            } else {
                res.add(new String(chars,i,1));
            }
        }
        String[] s = new String[res.size()];
        return res.toArray(s);
    }
    /**
     * @ClassName : WordSegment
     * @Description : 维特比算法
     * @param x

     * @Return : int[]
     * @Author : ZNWhahaha
     * @Date : 2020/3/27
    */
    public int[] viterbi(char[] x) {
        double[][] delta = new double[x.length][4];
        int[][] track = new int[x.length][4];
        //delta初始值
        for(int i = 0; i < 4; i++) {
            delta[0][i] = pi[i] + B[i][x[0]];
        }
        for(int t = 1; t < x.length; t++) {
            for(int i = 0; i < 4; i++) {
                delta[t][i] = B[i][x[t]];
                double max = -Double.MAX_VALUE;
                for(int j = 0; j < 4;j++) {
                    double tmp = delta[t-1][j] + A[j][i];
                    if(tmp > max) {
                        max = tmp;
                        track[t][i] = j;
                    }
                }
                delta[t][i] += max;
            }
        }
        int T = x.length-1;
        //回溯找到最优路径
        int[] tags = new int[x.length];
        double p = delta[T][0];
        for(int i = 1; i < 4; i++) {
            if(delta[T][i] > p) {
                p = delta[T][i];
                tags[T] = i;
            }
        }
        for(int i = T-1; i >=0; i--) {
            tags[i] = track[i+1][tags[i+1]];
        }
        return tags;
    }



    /**
     * @ClassName : WordSegment
     * @Description : 加载pku训练集
     * @param

     * @Return : com.ZNWhahaha.WordSegment.SequenceData
     * @Author : ZNWhahaha
     * @Date : 2020/3/29
    */
    public static SequenceData loadPKUSegData() {
        List<String> lines =readLines("/Users/znw_mac/IdeaProjects/ChineseWordSegment0.1/TextFiles/pku_training_crf.utf8", "utf-8");
        List<int[]> xSeqs = new ArrayList<int[]>(lines.size());
        List<int[]> ySeqs = new ArrayList<int[]>(lines.size());
        Map<String, Integer> tagInt = new HashMap<String, Integer>();
        tagInt.put("B", 0);
        tagInt.put("M", 1);
        tagInt.put("E", 2);
        tagInt.put("S", 3);
        int[] x = new int[lines.size()];
        int[] y = new int[lines.size()];
        int c = 0;
        for(String line : lines) {
            String[] wn = line.split("\t");
            x[c] = wn[0].toCharArray()[0];
            y[c] = tagInt.get(wn[1]);
            c++;
        }
        xSeqs.add(x);
        ySeqs.add(y);
        SequenceData sequenceData = new SequenceData();
        sequenceData.xSeqs = xSeqs;
        sequenceData.ySeqs = ySeqs;
        sequenceData.allSeqLen = xSeqs.size();
        return sequenceData;
    }

    /**
     * @ClassName : WordSegment
     * @Description : 加载测试集
     * @param path
     * @param encoding
     * @Return : java.util.List<java.lang.String>
     * @Author : ZNWhahaha
     * @Date : 2020/3/29
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


    /**
     * @ClassName : WordSegment
     * @Description : 拆分、序列预测
     * @param sen

     * @Return : java.lang.String[]
     * @Author : ZNWhahaha
     * @Date : 2020/3/27
     */
    public String[] predictAndSplit(String sen) {
        char[] chs = sen.toCharArray();
        int[] tags = viterbi(chs);
        return decode(tags, sen);
    }

    /**
     * @ClassName : WordSegment
     * @Description : 外部调用接口
     * @param

     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/4/10
    */
    public List<String> run_Test(String line) throws IOException {
        String[] words = this.predictAndSplit(line);
        List<String> Words = Arrays.asList(words);
        System.out.println(String.join("/", words));
        return Words;
    }

    /**
     * @ClassName : WordSegment
     * @Description : 外部调用接口
     * @param

     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/4/10
     */
    public void run_Train(){
        SequenceData data = loadPKUSegData();
        this.train(data);
    }
    
    public static void main(String[] args) throws IOException {

        String filePath = "/Users/znw_mac/IdeaProjects/ChineseWordSegment0.1/TextFiles";
        File dir = new File(filePath);
        // 一、检查放置文件的文件夹路径是否存在，不存在则创建
        if (!dir.exists()) {
            dir.mkdirs();// mkdirs创建多级目录
        }
        File checkFile = new File(filePath + "/output.txt");
        FileWriter writer = null;



        SequenceData data = loadPKUSegData();
        WordSegment hmm = new WordSegment();
        hmm.train(data);
        List<String> testLines = readLines("/Users/znw_mac/IdeaProjects/ChineseWordSegment0.1/TextFiles/test.txt", "utf-8");
        for(String line : testLines) {

            String[] words = hmm.predictAndSplit(line);
            System.out.println(String.join("/", words));

            // 二、检查目标文件是否存在，不存在则创建
            if (!checkFile.exists()) {
                checkFile.createNewFile();// 创建目标文件
            }

            writer = new FileWriter(checkFile, true);
            writer.append(String.join("/", words));
            writer.flush();

        }
        
    }

}
