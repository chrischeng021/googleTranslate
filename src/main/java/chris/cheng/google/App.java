package chris.cheng.google;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.*;

public class App {
    public static final String TARGET_LANGUAGE = "fr";
    public static final String SPLIT_CHAR = "\\.";
    public static Translate translate = TranslateOptions.getDefaultInstance().getService();

    //从输入文件读取待翻译内容
    private static String getInput(String filePath){
        String input = null;
        File sourceFile = new File(filePath);
        if(!sourceFile.exists()){
            System.out.println("invalid File Path!");
        }
        else{
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = null;
            String tempString;
            try{
                reader = new BufferedReader(new FileReader(sourceFile));
                while((tempString = reader.readLine()) != null){
                    sb.append(tempString);
                }
                reader.close();
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            finally {
                if(null != reader){
                    try{
                        reader.close();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
            input = sb.toString().replace("\n","\\.");
        }
        return input;
    }

    public static void process(String sourceFilePath){
        String source = getInput(sourceFilePath);
        if(null == source || source.length() == 0){
            System.out.println("Please Check Source File Path!");
            return;
        }
        else{
            String sourceArr[] = source.split(SPLIT_CHAR);
            File summary = new File(sourceFilePath+"_summary.txt");
            File compare = new File(sourceFilePath+"_compare.txt");
            System.out.println("待翻译文本句数：" + sourceArr.length);

            System.out.println(summary.getName());
            System.out.println(compare.getName());
            try{
                FileWriter summaryWriter = new FileWriter(summary.getName(), true);
                FileWriter compareWriter = new FileWriter(compare.getName(), true);
                int sentenceNum = 0;
                for(String sentence : sourceArr){
                    if(sentence.length() == 0 || sentence == "")
                        continue;
                    sentenceNum++;
                    sentence = sentence.trim();
                    System.out.println("当前翻译句数：" + sentenceNum);
                    System.out.println("Original  Text:\n" + sentence + "\n");
                    String translatedSentence;
                    if(sentence.length() < 2)
                        translatedSentence = sentence;
                    else
                        translatedSentence = translateSingleSentence(translate, sentence);
                    summaryWriter.write("Index：" + sentenceNum+ "\n");
                    summaryWriter.write(translatedSentence + "\n\n");
                    compareWriter.write("Index：" + sentenceNum + "\n" + sentence + "\n");
                    compareWriter.write(translatedSentence + "\n\n");
                    System.out.println("Translate Text:\n" + translatedSentence + "\n");
                }
                summaryWriter.close();
                compareWriter.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    //真正调用Google Translation API的入口
    private static String translateSingleSentence(Translate translate, String source){
        Translation translation =
                translate.translate(
                        source,
                        TranslateOption.sourceLanguage(translate.detect(source).getLanguage()),
                        TranslateOption.targetLanguage(TARGET_LANGUAGE));
        return translation.getTranslatedText().replace("&#39;", "\'").replace("&quot;","\"");
    }

    public static void main(String[] args){
        process(args[0]);        
    }
}
