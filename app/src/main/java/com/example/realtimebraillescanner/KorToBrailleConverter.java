package com.example.realtimebraillescanner;

import android.util.Log;

import com.github.kimkevin.hangulparser.HangulParser;
import com.github.kimkevin.hangulparser.HangulParserException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class KorToBrailleConverter {

    int BASE_CODE = 44032;
    int CHOSUNG = 588;
    int JUNGSUNG = 28;
    mapping mapping = new mapping();
    String braille = "";
    Boolean flag10 = false;
    Boolean flag11 = false;
    Boolean flag17 = false;

    String[] CHOSUNG_LIST = {"ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ",
            "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ",
            "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};
    String[] JUNGSUNG_LIST = {"ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ",
            "ㅖ", "ㅗ", "ㅘ", "ㅙ", "ㅚ", "ㅛ", "ㅜ",
            "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"};
    String[] JONGSUNG_LIST = {" ", "ㄱ", "ㄲ", "ㄳ", "ㄴ", "ㄵ", "ㄶ", "ㄷ",
            "ㄹ", "ㄺ", "ㄻ", "ㄼ", "ㄽ", "ㄾ", "ㄿ", "ㅀ",
            "ㅁ", "ㅂ", "ㅄ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅊ",
            "ㅋ", "ㅌ", "ㅍ", "ㅎ"};

    KorToBrailleConverter(){}

    public ArrayList<String> extract_words(String text){ //문자열 띄어쓰기 기준으로 분리
        String[] words = text.split(" ");
        ArrayList<String> result = new ArrayList<>();

        for(int i = 0; i < words.length; i++){
            result.add(words[i]);
        }
        return result;
    }

    public int check_contraction(String word, int idx){ //check the data set of mapping.contraction
        Set<String> keys = mapping.contractions.keySet();
        for (String key : keys){
            if (word.substring(idx).startsWith(key)){
                if (!(word.substring(idx).equals("가")||word.substring(idx).equals("나")||word.substring(idx).equals("다")||word.substring(idx).equals("마")||word.substring(idx).equals("바")||word.substring(idx).equals("사")||word.substring(idx).equals("자")||word.substring(idx).equals("카")||word.substring(idx).equals("타")||word.substring(idx).equals("파")||word.substring(idx).equals("하"))){
                    flag10 = false;
                }
                braille += mapping.contractions.get(key);
                return key.length();
            }
        }
        return 0;
    }

    public Boolean check_contraction2(String Cho, String Jung, String Jong){
        // 초성 자음 + 약어 (억 || 언 || 얼 || 연 || 열 ... || 인)    등 검사
        ArrayList<String> jasoList= new ArrayList<>();
        String doubleJong = new String();   //규정 제15항 확인용 변수 (종성이 자음 두개로 이루어질 시 분리 후 첫 번째로 약자 확인)

        jasoList.add("ㅇ");
        jasoList.add(Jung);

        if (!(Jong.trim().equals(""))){     //종성 존재하면
            if(mapping.decompose.get(Jong)!=null){      //종성이 double 형태일 시
                doubleJong = mapping.decompose.get(Jong);
                jasoList.add(String.valueOf(doubleJong.charAt(0))); //double의 첫 자모 추가
            }
            else{
                jasoList.add(Jong); //종성이 낱개 자모일 시 해당 문자 추가
            }
        }

        try{
            String hangul = HangulParser.assemble(jasoList);
            if (mapping.contractions.get(hangul)!=null){    //약어 존재할 시

                if(hangul.equals("영") && (Cho.equals("ㅅ") || Cho.equals("ㅈ") || Cho.equals("ㅊ"))){
                    return false;
                }
                braille += mapping.CHOSUNG_letters.get(Cho);
                braille += mapping.contractions.get(hangul);
                flag10 = false;
                if (doubleJong.length()>1){ //double이면 낱개 자모 하나 더 추가
                    braille += mapping.JONGSUNG_letters.get(String.valueOf(doubleJong.charAt(1)));
                }
                return true;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public Boolean check_contraction3(String Cho, String Jung, String Jong){
        // 약어 (가 || 나 || 다 || 마 || 바 || 사 ... || 하 || 것) + 종성 자음    등 검사
        ArrayList<String> jasoList= new ArrayList<>();
        jasoList.add(Cho);
        jasoList.add(Jung);

        try{
            String hangul = HangulParser.assemble(jasoList);
            if (mapping.contractions.get(hangul)!=null){
                braille += mapping.contractions.get(hangul);
                braille += mapping.JONGSUNG_letters.get(Jong);
                flag10 = false;
                return true;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public Boolean check_number(String word, int idx){  //check the data set of numbers
        if (mapping.numbers.get(String.valueOf(word.charAt(idx))) != null){ //숫자일 경우
            if (idx != 0){      //첫 글자 이후일 경우
                if (mapping.numbers.get(String.valueOf(word.charAt(idx-1))) != null){   //직전 글자가 숫자일 경우 수표 추가할 필요 x
                    braille += mapping.numbers.get(String.valueOf(word.charAt(idx)));
                }
                else{   //처음 시작하는 숫자일 시 수표 추가
                    braille += mapping.number_start + mapping.numbers.get(String.valueOf(word.charAt(idx)));
                }
            }
            else{   //첫 인덱스이며 숫자일 경우 수표 추가
                braille += mapping.number_start + mapping.numbers.get(String.valueOf(word.charAt(idx)));
            }
            return true;
        }
        return false;
    }

    public Boolean check_punctuation(String word, int idx){ //check the data set of punctuation
        if (mapping.punctuation.get(String.valueOf(word.charAt(idx)))!=null){
            braille += mapping.punctuation.get(String.valueOf(word.charAt(idx)));
            return true;
        }
        return false;
    }

    public Boolean check_character(String word, int idx){//check the data set of characters in all letters
        String[] keys = new String[1];
        Character key = word.charAt(idx);
        keys[0] = String.valueOf(key);
        int char_code, char1, char2, char3;

        if (Pattern.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*", keys[0])){

            char_code = (int)(keys[0].charAt(0)) - BASE_CODE;

            if (char_code >= 0){        //완전한 글자일 때
                char1 = (int)(char_code / CHOSUNG);
                char2 = (int)((char_code - (CHOSUNG * char1)) / JUNGSUNG);
                char3 = (int)((char_code - (CHOSUNG * char1) - (JUNGSUNG * char2)));

                String Cho = CHOSUNG_LIST[char1];
                String Jung = JUNGSUNG_LIST[char2];
                String Jong = JONGSUNG_LIST[char3];

                if (!check_contraction2(Cho, Jung, Jong)){//늘 => ㄴ+'을' 과 같은 약어 경우 없을 시
                    if (!check_contraction3(Cho, Jung, Jong)){//감 => '가'+ㅁ 과 같은 약어 경우 없을 시

                        //약자, 약어없이 초,중,종 모든 파트 1:1 매핑
                        if (char3 == 0 && flag10 && Cho.equals("ㅇ") && Jung.equals("ㅖ")){  //규정 제10항
                            braille += "⠤";
                            braille += mapping.CHOSUNG_letters.get(Cho);
                            braille += mapping.JUNGSUNG_letters.get(Jung);
                        }
                        else if(flag10 && flag11 && Cho.equals("ㅇ") && Jung.equals("ㅐ")){ //규정 제11항
                            braille += "⠤";
                            braille += mapping.CHOSUNG_letters.get(Cho);
                            braille += mapping.JUNGSUNG_letters.get(Jung);
                            if (char3 != 0){
                                braille += mapping.JONGSUNG_letters.get(Jong);
                                flag10 = false;
                            }
                            flag11 = false;
                        }
                        else{   //1:1 매핑 케이스
                            braille += mapping.CHOSUNG_letters.get(Cho);
                            if (char3 != 0){    //종성 자음 존재할 경우만 추가
                                if(Jung.equals("ㅓ") && Jong.equals("ㅇ") && (Cho.equals("ㅅ") || Cho.equals("ㅆ") || Cho.equals("ㅈ") || Cho.equals("ㅉ") || Cho.equals("ㅊ"))){
                                    braille += "⠻";     //규정 제17항
                                }
                                else{
                                    braille += mapping.JUNGSUNG_letters.get(Jung);
                                    braille += mapping.JONGSUNG_letters.get(Jong);
                                }
                                flag10 = false;
                                flag11 = false;
                            }
                            else{   //종성 자음 존재하지 않고 모음까지만 존재하는 경우
                                braille += mapping.JUNGSUNG_letters.get(Jung);

                                flag10 = true;

                                if(Jung.equals("ㅑ") || Jung.equals("ㅘ") || Jung.equals("ㅜ") || Jung.equals("ㅝ")){
                                    flag11 = true;
                                }
                            }
                        }


                    }
                }
                return true;
            }
            else{           //자음 혹은 모음 하나만 있을 때
                braille += mapping.CHOSUNG_start;
                if (mapping.CHOSUNG_letters.get(keys[0]) != null){      //초성 자음일 경우
                    Log.d("점자 분해1", keys[0]);
                    String decompose = mapping.decompose.get(keys[0]);
                    if (decompose != null){
                        Log.d("점자 분해2", decompose);
                        if (decompose.length() > 1){
                            braille += mapping.JONGSUNG_letters.get(keys[0]);
                        }
                    }
                    else{
                        braille += mapping.CHOSUNG_letters.get(keys[0]);
                    }
                }
                else{      //중성 모음일 경우
                    braille += mapping.JUNGSUNG_letters.get(keys[0]);
                }
                return true;
            }
        }
        return false;
    }

    public String translate(String text){   //Kor --> Braille
        String[] words_token = text.split("\n");

        for(String token : words_token){
            ArrayList<String> words = new ArrayList<>();

            words = extract_words(token);

            for(String word : words){
                int i = 0;
                while (i < word.length()){
                    int check_cont = check_contraction(word, i);

                    if (check_cont != 0){
                        i += check_cont;
                        continue;
                    }
                    if (check_number(word, i)){
                        i+=1;
                        flag10 = false;
                        flag11 = false;
                        continue;
                    }
                    if (check_punctuation(word, i)){
                        i+=1;
                        flag10 = false;
                        flag11 = false;
                        continue;
                    }
                    check_character(word, i);
                    i += 1;
                }
                braille += " ";
                flag10 = false;
                flag11 = false;
                flag17 = false;
            }
            braille += "\n";
        }
        return braille;
    }

}
