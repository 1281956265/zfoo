package com.zfoo.util.gaea;

import com.zfoo.protocol.util.StringUtils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * 生成新的序列号（一般用于生成兑换码）
 */
public class GenSerialUtil {
    public static final String Base32Alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    //默认code长度 生成的所有code都是9位  6则取前六位
    public static final int DEFULT_CODE_LEN = 12;
    //默认校验位长度
    public static final int DEFULT_CHECK_BIT_LEN = 7;
    //默认标识
    public static final int DEFULT_FLAG = 0;

    /**
     * 生成新的序列号    <br>
     * <p>生成规则：codeLen*5位的数 （二进制）<br>
     * 标识位  + 数据位 + 校验位 <br>
     * 然后将45位的数映射到用 ABCDEFGHJKLMNPQRSTUVWXYZ23456789 表示的序列号，要映射到32个字符中就是每5位代表一个字符(2^5=32)，
     * 所有生成的序列号是 codeLen位。
     *
     * @param codeLen     code长度
     * @param flag        标识
     * @param flagBitLen  标识长度
     * @param checkBitLen 校验位长度
     * @return
     */
    public static String generateNewCode(int codeLen, int flag, int flagBitLen, int checkBitLen) {
        // 长整形ID
        long ret = 0L;
        Random random = new Random();
        int checkModData = 1 << checkBitLen;
        int totalBitLen = codeLen * 5;
        int dataBitLen = totalBitLen - checkBitLen - flagBitLen;
        long randData = (long) (1 + (1L << dataBitLen - 1) * random.nextDouble());
        if (flagBitLen > 0) {
            //防止越位，若16位标识则是 0xffff
            flag = flag & ((1 << flagBitLen) - 1);
            //高位标志位
            ret += (long) flag << (totalBitLen - flagBitLen);
        }

        // 中位数据位
        ret += randData << checkBitLen;
        //低位校验位
        long checkNum = (ret >> checkBitLen) % checkModData;
        // 1 - checkBitLen位 校验位
        ret += checkNum;
        return convertToBase32SerialCode(ret, codeLen);
    }

    public static String generateNewCode(int flag, int flagBitLen) {
        return generateNewCode(DEFULT_CODE_LEN, flag, flagBitLen, DEFULT_CHECK_BIT_LEN);
    }

    public static String generateNewCode(int flag) {
        int flagBitLen = getFlagLenByFlag(flag);
        return generateNewCode(DEFULT_CODE_LEN, flag, flagBitLen, DEFULT_CHECK_BIT_LEN);
    }

    /**
     * 根据flag获取flagLen
     */
    private static int getFlagLenByFlag(int flag) {
        int flagBitLen;
        if (flag == 0) {
            flagBitLen = 0;
        } else {
            flagBitLen = Integer.toBinaryString(flag).length();
        }
        return flagBitLen;
    }

    public static String generateNewCode() {
        return generateNewCode(DEFULT_FLAG);
    }

    /**
     * @param historyCodeSet 历史生成的序列号 集合
     * @param number
     * @param codeLen
     * @param flag
     * @param flagBitLen
     * @param checkBitLen
     * @return
     */
    public static Set<String> generateCodes(Set<String> historyCodeSet, int number, int codeLen, int flag, int flagBitLen, int checkBitLen) {
        Set<String> generatedCodes = new HashSet<String>(number * 4 / 3 + 1);
        if (historyCodeSet == null) {
            historyCodeSet = new HashSet<String>(0);
        }
        while (generatedCodes.size() < number) {
            String code = generateNewCode(codeLen, flag, flagBitLen, checkBitLen);
            if (!historyCodeSet.contains(code)) {
                generatedCodes.add(code);
            }
//            System.out.println(code);
        }
        return generatedCodes;
    }

    /**
     * @param historyCodeSet
     * @param number
     * @return
     */
    public static Set<String> generateCodes(Set<String> historyCodeSet, int number, int flag) {
        int flagBitLen = getFlagLenByFlag(flag);
        return generateCodes(historyCodeSet, number, DEFULT_CODE_LEN, flag, flagBitLen, DEFULT_CHECK_BIT_LEN);
    }

    /**
     * @param historyCodeSet
     * @param number
     * @return
     */
    public static Set<String> generateCodes(Set<String> historyCodeSet, int number) {
        return generateCodes(historyCodeSet, number, DEFULT_FLAG);
    }


    /**
     * 将随机数转换成BASE32编码 序列码
     *
     * @return
     */
    private static String convertToBase32SerialCode(long longRandValue, int codeLen) {
        StringBuffer codeSerial = new StringBuffer(16);
        long tmpRandValue = longRandValue;
        for (int i = 0; i < codeLen; i++) {
            int code = (int) (tmpRandValue & 0x1F);
            char convertCode = Base32Alphabet.charAt(code);
            codeSerial.append(convertCode);
            tmpRandValue = tmpRandValue >> 5;
        }
        return codeSerial.reverse().toString();
    }


    /**
     * 将兑换码序列字符转化成数字。
     *
     * @return
     */
    private static int convertBase32CharToNum(char ch) {
        int index = Base32Alphabet.indexOf(ch);
        return index;
    }

    /**
     * 将序列号转成长整数
     *
     * @return
     */
    public static long convertBase32CharToNum(String serialCode) {
        if (StringUtils.isEmpty(serialCode)) {
            return -1;
        }

        long id = 0;
        for (int i = 0; i < serialCode.length(); i++) {
            int originNum = convertBase32CharToNum(serialCode.charAt(i));
            if (originNum == -1) {
                return 0;
            }
            id = id << 5;
            id += originNum;
        }
        return id;
    }

    /**
     * 校验序列号是否合法
     *
     * @param code
     * @return
     */
    public static boolean checkCodeValid(String code, int checkBitLen) {
        long id = 0;
        int checkModData = 1 << checkBitLen;
        for (int i = 0; i < code.length(); ++i) {
            long originNum = convertBase32CharToNum(code.charAt(i));
            if (originNum >= 32)
                // 字符非法
                return false;
            id = id << 5;
            id += originNum;
        }

        long data = id >> checkBitLen;
        // 最后n位是校验码
        long checkNum = id & (checkModData - 1);

        if (data % checkModData == checkNum)
            return true;

        return false;
    }

    public static boolean checkCodeValid(String code) {
        if (code == null || code.length() == 0) {
            return false;
        }
        return checkCodeValid(code, DEFULT_CHECK_BIT_LEN);
    }

    /**
     * 从序列号提取标识
     *
     * @param code       序列号
     * @param flagBitLen 标识位长度
     * @return
     */
    public static Long getFlagFromCode(String code, int flagBitLen) {
        long id = convertBase32CharToNum(code);
        return id >> (code.length() * 5 - flagBitLen);
    }

    /**
     * 检验code的flag
     */
    public static boolean checkCodeFlag(String code, int flag, int flagBitLen) {
        Long codeFlag = getFlagFromCode(code, flagBitLen);
        return codeFlag.equals((long) flag);
    }

    /**
     * 检验code的flag
     */
    public static boolean checkCodeFlag(String code, int flag) {
        int flagBitLen = getFlagLenByFlag(flag);
        return checkCodeFlag(code, flag, flagBitLen);
    }

    public static void main(String[] args) {
        // 生成100个码
        Set<String> codes = generateCodes(new HashSet<>(), 100, 45);
        System.out.println(codes);
        for (String string : codes) {
            boolean b = checkCodeFlag(string, 1);
            boolean b1 = checkCodeValid(string);
            if (!b || ! b1) {
                System.err.println("非法！");
            }
        }
    }
}