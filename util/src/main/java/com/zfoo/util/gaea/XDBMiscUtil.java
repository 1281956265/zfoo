
package com.zfoo.util.gaea;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * 一些辅助性的数学方法 随机算法 权重算法
 * 
 */
public class XDBMiscUtil {
	private static Random random = new Random(System.currentTimeMillis());

	public static Random getRandom() {
		return ThreadLocalRandom.current();
	}

	public static Random getRandom(Random defaultRandom) {
		return defaultRandom != null ? defaultRandom : getRandom();
	}
	
	/**
	 * 获取随机数 [ start, end ] 或 [ end, start ]
	 * 
	 * @param start 起始值
	 * @param end 结束值
	 * @param random 随机数实体，可能某些功能要用到自己的Random实例，以保证绝对整体随机
	 */
	public static int getRandomBetween(int start, int end, Random random) {
		int absSub = Math.abs(start - end);
		int randomSub = random.nextInt(absSub + 1);
		return end > start ? start + randomSub : end + randomSub;
	}
	
	public static int getRandomBetween(int start, int end) {
		return getRandomBetween(start, end, getRandom());
	}
	
	/**
	 * 获取随机浮点数 [start, end ] 或 [ end, start ]
	 */
	public static float getRandomBetween(final float start, final float end) {
		float absSub = Math.abs(start - end);
		float randomSub = getRandom().nextFloat() * absSub;
		return end > start ? start + randomSub : end + randomSub;
	}
	
	public static <T> T getRandom(Collection<T> datas) {
		return getRandom(datas, getRandom());
	}
	
	/**
	 * 从集合中获取随机一个 这个实现不好 看下面这个
	 */
//	@SuppressWarnings("unchecked")
//	public static <T> T getRandom(Collection<T> datas, Random random) {
//		if(null == datas || datas.isEmpty()) {
//			return null;
//		}
//
//		Object[] os = datas.toArray();
//		int index = getRandomBetween(0, os.length -1, random);
//		return (T)os[index];
//	}

	/**
	 * 从集合中获取随机一个
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getRandom(Collection<T> datas, Random random) {
		if (datas == null || datas.size() == 0) {
			return null;
		}
		T result = null;
		int index = getRandom(random).nextInt(datas.size());
		if (datas instanceof List) {
			result = ((List<T>) datas).get(index);
		} else {
			for (T t : datas) {
				if (index-- == 0) {
					result = t;
					break;
				}
			}
		}
		return result;
	}

	public static <T> T getArrayRandom(T[] arr) {
		return getArrayRandom(arr, getRandom());
	}

	/**
	 * 数组中随机一个元素
	 */
	public static <T> T getArrayRandom(T[] arr, Random random) {
		if (arr == null || arr.length == 0) {
			return null;
		}
		return arr[getRandom(random).nextInt(arr.length)];
	}

	
	/**
	 * 获取随机百分整数
	 * 
	 * @return [1, 100] 的随机数
	 */
	public static int getRatePercent() {
		return getRandomBetween(1, 100);
	}

	/**
	 * 检测百分数概率是否成功
	 * 
	 * @param value 给定的概率
	 */
	public static boolean checkRatePercent(int value) {
		return value >= getRatePercent();
	}

	/**
	 * 获取以[0, base]内的概率随机数值 [base, 0]也没关系 :)
	 */
	public static int getRateValue(int base) {
		return getRandomBetween(0, base);
	}
	
	public static int getRateValue(int base, Random random) {
		return getRandomBetween(0, base, random);
	}

	/**
	 * 检测概率事件是否成功
	 * 注意：调用需 base - 1
	 * @param base 基数
	 * @param value 概率值
	 */
	public static boolean checkRate(int base, int value) {
		return checkRate(base, value, getRandom());
	}
	
	public static boolean checkRate(int base, int value, Random random) {
		if (base < 0) {
			return false;
		}
		
		int tmp = getRateValue(base, random);
		return value > tmp;
	}

	/**
	 * 从 [min, max]中取出一组不重复的随机数
	 */
	public static List<Integer> getRandomValues(int min, int max, int num) {

		final List<Integer> list = new LinkedList<Integer>();
		if (num <= 0 || max < 0 || max < min) {
			return list;
		}

		if (num > (max - min + 1)) {
			num = max - min + 1;
		}

		for (int i = 0; i < num; ++ i) {
			int val = getRandomBetween(min, max);
			while (list.contains(val)) {
				val = min + ((val + 1 - min) % (max - min + 1));
			}

			list.add(val);
		}

		return list;
	}

	public static int getProbability(int[] weights, int base) {
		return getProbability(weights, base, getRandom());
	}

	/**
	 * 返回一组权重中最后发生的那个.比如玩家做完任务后有30权重获得物品A,30权重
	 * 获得物品B,40权重获得物品C.把权重作为数组传入,然后该方法返回数组的下标.
	 * 在上面的例子中,假如用户传入[30,30,40] 100,返回2，则表示玩家应获得物品C
	 *
	 * 注意：权重之和小于base时，有可能返回-1，代表所有概率都未触发
	 */
	public static int getProbability(int[] weights, int base, Random random) {
		if (null == weights || 0 == weights.length) {
			return -1;
		}

		int value = getRandomBetween(0, base - 1, random);
		for (int i = 0; i < weights.length; i ++) {
			if (value < weights[i]) {
				return i;
			}
			value -= weights[i];
		}

		return -1;
	}

	/**
	 * 和getProbability(int []pros,int base)类似,优点是不需要提供base,程序会把pros数组之和作为base
	 */
	public static int getProbability(int[] weights) {
		return getProbability(weights, getRandom());
	}

	/**
	 * 和getProbability(int []pros,int base)类似,优点是不需要提供base,程序会把pros数组之和作为base
	 */
	public static int getProbability(int[] weights, Random random) {
		if (null == weights || 0 == weights.length) {
			return -1;
		}

		int base = 0;
		for (int i = 0; i < weights.length; i++) {
			base += weights[i];
		}
		return getProbability(weights, base, random);
	}

	/**
	 * 从一堆权重里随机出一个T
	 * @param datas key 为要随机的东西，value为权重
	 */
	public static <T> T getProbability(Map<T, Integer> datas) {
		List<T> results = new ArrayList<T>();
		int[] weights = new int[datas.size()];
		int i = 0;
		for(Map.Entry<T, Integer> entry : datas.entrySet()) {
			weights[i] = entry.getValue();
			results.add(entry.getKey());
			i ++;
		}
		int index = getProbability(weights);
		return results.get(index);
	}

	/**
	 * 从一堆权重里随机出num个不重复的T
	 * @param datas key 为要随机的东西，value为权重
	 * @param num 需要的个数
	 */
	public static <T> List<T> getProbability(Map<T, Integer> datas, int num) {
		if (datas.size() <= num) {
			return new ArrayList<>((datas.keySet()));
		}
		Map<T, Integer> dataCopy = new HashMap<>(datas);
		List<T> results = new ArrayList<>(num);
		for (int i = 0; i < num; i++) {
			T result = getProbability(dataCopy);
			results.add(result);
			dataCopy.remove(result);
		}
		return results;
	}

	public static int getProbability(Collection<Integer> weights) {
		return getProbability(weights, getRandom());
	}

	public static int getProbability(Collection<Integer> weights, Random random) {
		if(null == weights || weights.isEmpty()) {
			return -1;
		}

		int[] weightsArray = new int[weights.size()];
		int i = 0;
		for (int weight : weights) {
			weightsArray[i ++] = weight;
		}

		return getProbability(weightsArray, random);
	}

	public static  <T> T getProbability(List<T> prolist, Function<T,Integer> faction) {
		final int[] probs = new int[prolist.size()];
		for (int i = 0; i < probs.length; i++)
			probs[i] = faction.apply(prolist.get(i));
		
		int index = getProbability(probs);
		if(index == -1){
			return null;
		}
		return prolist.get(index);
	}

	/**
	 * 获得一个不重复的随机序列.比如要从20个数中随机出6个不同的数字,可以先把20个数存入数组中,然后调用该方法.注意方法调用完后totals
	 * 里面的elements的顺序是变了的.适用于从一部分数中找出其中的绝大部分
	 * 
	 * @param totals 所有数据存放在数组中
	 * @param dest 要返回的序列的长度
	 */
	public static <T> List<T> getRandomList(T[] totals, int dest) {

		if (dest <= 0) {
			throw new IllegalArgumentException();
		}
		List<T> result = new LinkedList<T>();
		if (dest > totals.length) { //如果要选的数比数组的长度还长,那就直接返回整个数组
			for(T data : totals) {
				result.add(data);
			}
			return result;
		}
		
		for (int i = 0; i < dest; i++) {
			// 得到一个位置
			int j = getRandom().nextInt(totals.length - i);
			result.add(totals[j]);
			// 将未用的数字放到已经被取走的位置中,这样保证不会重复
			totals[j] = totals[totals.length - 1 - i];
		}
		return result;
	}

	
	/**
	 * 从一个集合中，随机选取几个，构成一个新的List
	 * @param datas 源集合
	 * @param num 随机选取的个数
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getRandomList(Collection<T> datas, int num) {
		
		//如果集合小于需要的个数，则直接返回一个乱序的list
		if(datas.size() <= num) {
			List<T> result = new LinkedList<T>();
			result.addAll(datas);
			Collections.shuffle(result);
			return result;
		}
		
		return getRandomList((T[])datas.toArray(), num);
	}
	
	/**
	 * 在一定范围内随机化数值
	 * 主要用于随机化伤害等，例如最终伤害随机为原始值的90%~110%
	 *
	 * @param value 初始值
	 * @param minPct 最小比例
	 * @param maxPct 最大比例
	 * @return 在范围随机后的值
	 */
	public static int randomValue(int value, double minPct, double maxPct) {
		return getRandomBetween((int)(value * minPct), (int)(value * maxPct));
	}
	
	public static boolean isProbability(Double odds) {
		return isProbability(odds, getRandom());
	}
	
	public static boolean isProbability(Double odds, Random random) {
		if (odds.intValue() >= 1) {
			return true;
		}
		if (odds.intValue() < 0) {
			return false;
		}
		return random.nextDouble() < odds;
	}

	public static void main(String[] args) {
		
	}
	
}
