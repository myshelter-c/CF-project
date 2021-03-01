package com.scriptures.shareApp.util;

import com.scriptures.shareApp.dao.entity.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//基于用户的协同过滤的算法的实现
public class RecommendUtilsByUser {

    /**
     * 将用户的购买行为组装成一个map,key为userId，value也是一个map，这个map记录的是商品以及它对应的点击量
     * @param memberActives 用户的行为列表
     * @return 组装好的用户的购买行为的map集合
     */
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> assembleMemebrBehaviorForCommodity(List<MemberActiveCommodity> memberActives){
        ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> res = new ConcurrentHashMap<>();
        //遍历用户行为列表
        for (MemberActiveCommodity memberActive : memberActives) {
            //获得用户id
            String userId = memberActive.getMemberId();
            //获得商品id
            String commodityId = memberActive.getCommodityId();
            //获得该商品的点击数
            Long clicks = memberActive.getClicks();
            //进行组装
            if (res.containsKey(userId)){//存在则取出更新
                ConcurrentHashMap<String, Long> exitMap = res.get(userId);
                exitMap.put(commodityId, clicks);
                res.put(userId, exitMap);
            }else{//不存在就直接插入
                ConcurrentHashMap<String, Long> categorySecondMap = new ConcurrentHashMap<>();
                categorySecondMap.put(commodityId, clicks);
                res.put(userId, categorySecondMap);
            }
        }
        return res;
    }

    /**
     * 计算用户与用户之间的相似性，返回计算出的用户与用户之间的相似度对象
     * @param activeMap 用户对各个商品的购买行为的一个map集合
     * @return 计算出的用户与用户之间的相似度的对象存储形式
     */
    public static List<MemberSimilarity> calcSimilarityBetweenUsers2(ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> activeMap) {

        // 用户之间的相似度对集合
        List<MemberSimilarity> similarityList = new ArrayList<>();

        // 获取所有的键的集合
        Set<String> userSet = activeMap.keySet();

        // 把这些集合放入ArrayList中
        List<String> userIdList = new ArrayList<>(userSet);
        // 小于两个说明当前map集合中只有一个map集合的购买行为，或者一个都没有，直接返回
        if (userIdList.size() < 2) {
            return similarityList;
        }

        // 计算所有的用户之间的相似度对
        for (int i = 0; i < userIdList.size() - 1; i++) {
            for (int j = i + 1; j < userIdList.size(); j++) {
                // 分别获取两个用户对每个商品的点击量
                ConcurrentHashMap<String, Long> userCommodityMap = activeMap.get(userIdList.get(i));
                ConcurrentHashMap<String, Long> userRefCommodityMap = activeMap.get(userIdList.get(j));
                System.out.println(i+" userCategorySecondMap :"+userCommodityMap);
                System.out.println(j+" userRefCategorySecondMap: "+userRefCommodityMap);
                // 获取两个map中商品id的集合
                Set<String> key1Set = userCommodityMap.keySet();
                Set<String> key2Set = userRefCommodityMap.keySet();
                Iterator<String> it1 = key1Set.iterator();
                Iterator<String> it2 = key2Set.iterator();
                Set<String> twoMemberKeySet = new HashSet<>();
                // 两用户之间的相似度
                double similarity = 0.0;
                // 余弦相似度公式中的分子
                double molecule = 0.0;
                // 余弦相似度公式中的分母
                double denominator = 1.0;
                // 余弦相似度公式中分母根号下的两个向量的模的值
                double vector1 = 0.0;
                double vector2 = 0.0;
                //遍历
                while (it1.hasNext() || it2.hasNext()) {
                   if (it1.hasNext())twoMemberKeySet.add(it1.next());
                   if (it2.hasNext())twoMemberKeySet.add(it2.next());
                }
                Iterator<String> setKey = twoMemberKeySet.iterator();
                while (setKey.hasNext()) {
                    String commodityKey = setKey.next();

                    // 获取商品对应的点击数
                    Long hits1 = userCommodityMap.getOrDefault(commodityKey,0L);
                    Long hits2 = userRefCommodityMap.getOrDefault(commodityKey, 0L);
                    // 累加分子
                    molecule += hits1 * hits2;
                    // 累加分母中的两个向量的模
                    vector1 += Math.pow(hits1, 2);
                    vector2 += Math.pow(hits2, 2);
                }
                // 计算分母
                denominator = Math.sqrt(vector1) * Math.sqrt(vector2);
                // 计算整体相似度
                similarity = molecule / denominator;

                // 创建用户相似度对对象
                MemberSimilarity memberSimilarity = new MemberSimilarity();
                memberSimilarity.setMemberId(userIdList.get(i));
                memberSimilarity.setMemberRefId(userIdList.get(j));
                memberSimilarity.setSimilarity(similarity);
                // 将计算出的用户以及用户之间的相似度对象存入list集合
                similarityList.add(memberSimilarity);
            }
        }
        return similarityList;
    }



    /**
     * 将用户的购买行为组装成一个map,key为userId，value也是一个map，这个map记录的是二级类目以及它对应的点击量
     * @param memberActives 用户的行为列表
     * @return 组装好的用户的购买行为的map集合
     */
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> assembleMemebrBehavior(List<MemberActive> memberActives){
        ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> res = new ConcurrentHashMap<>();
        //遍历用户行为列表
        for (MemberActive memberActive : memberActives) {
            //获得用户id
            String userId = memberActive.getId();
            //获得该二级种类id
            String commodityId = memberActive.getCategoryIdSecond();
            //获得该二级种类的点击数
            Long hits = memberActive.getClicks();
            //进行组装
            if (res.containsKey(userId)){//存在则取出更新
                ConcurrentHashMap<String, Long> exitMap = res.get(userId);
                exitMap.put(commodityId, hits);
                res.put(userId, exitMap);
            }else{//不存在就直接插入
                ConcurrentHashMap<String, Long> categorySecondMap = new ConcurrentHashMap<>();
                categorySecondMap.put(commodityId, hits);
                res.put(userId, categorySecondMap);
            }
        }
        return res;
    }

    /**
     * 计算用户与用户之间的相似性，返回计算出的用户与用户之间的相似度对象
     * @param activeMap 用户对各个二级类目的购买行为的一个map集合
     * @return 计算出的用户与用户之间的相似度的对象存储形式
     */
    public static List<MemberSimilarity> calcSimilarityBetweenUsers(ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> activeMap) {

        // 用户之间的相似度对集合
        List<MemberSimilarity> similarityList = new ArrayList<>();


        // 获取所有的键的集合
        Set<String> userSet = activeMap.keySet();

        // 把这些集合放入ArrayList中
        List<String> userIdList = new ArrayList<>(userSet);

        // 小于两个说明当前map集合中只有一个map集合的购买行为，或者一个都没有，直接返回
        if (userIdList.size() < 2) {
            return similarityList;
        }

        // 计算所有的用户之间的相似度对
        for (int i = 0; i < userIdList.size() - 1; i++) {
            for (int j = i + 1; j < userIdList.size(); j++) {
                // 分别获取两个用户对每个二级类目的点击量
                ConcurrentHashMap<String, Long> userCategorySecondMap = activeMap.get(userIdList.get(i));
                ConcurrentHashMap<String, Long> userRefCategorySecondMap = activeMap.get(userIdList.get(j));
                System.out.println(i+" userCategorySecondMap :"+userCategorySecondMap);
                System.out.println(j+" userRefCategorySecondMap: "+userRefCategorySecondMap);
                // 获取两个map中二级类目id的集合
                Set<String> key1Set = userCategorySecondMap.keySet();
                Set<String> key2Set = userRefCategorySecondMap.keySet();
                Iterator<String> it1 = key1Set.iterator();
                Iterator<String> it2 = key2Set.iterator();

                // 两用户之间的相似度
                double similarity = 0.0;
                // 余弦相似度公式中的分子
                double molecule = 0.0;
                // 余弦相似度公式中的分母
                double denominator = 1.0;
                // 余弦相似度公式中分母根号下的两个向量的模的值
                double vector1 = 0.0;
                double vector2 = 0.0;

                while (it1.hasNext() && it2.hasNext()) {
                    String it1Id = it1.next();
                    String it2Id = it2.next();
                    // 获取二级类目对应的点击次数
                    Long hits1 = userCategorySecondMap.get(it1Id);
                    Long hits2 = userRefCategorySecondMap.get(it2Id);
                    // 累加分子
                    molecule += hits1 * hits2;
                    // 累加分母中的两个向量的模
                    vector1 += Math.pow(hits1, 2);
                    vector2 += Math.pow(hits2, 2);
                }
                // 计算分母
                denominator = Math.sqrt(vector1) * Math.sqrt(vector2);
                // 计算整体相似度
                similarity = molecule / denominator;

                // 创建用户相似度对对象
                MemberSimilarity memberSimilarity = new MemberSimilarity();
                memberSimilarity.setMemberId(userIdList.get(i));
                memberSimilarity.setMemberRefId(userIdList.get(j));
                memberSimilarity.setSimilarity(similarity);
                // 将计算出的用户以及用户之间的相似度对象存入list集合
                similarityList.add(memberSimilarity);
            }
        }
        return similarityList;
    }

    /**
     * 找出与userId购买行为最相似的topN个用户
     * @param memberId 需要参考的用户id
     * @param memberSimilarityList 用户相似度列表
     * @param topN 与userId相似用户的数量
     * @return 与usereId最相似的topN个用户
     * 这里只返回用户id列表，getSimilarityBetweenUsers2附带分数
     */
    public static List<String> getSimilarityBetweenUsers(String memberId, List<MemberSimilarity> memberSimilarityList, Integer topN) {
        // 用来记录与userId相似度最高的前N个用户的id
        List<String> similarityList = new ArrayList<>(topN);

        // 堆排序找出最高的前N个用户，建立小根堆，每次需要添加的数先与堆顶进行比较，小于堆顶则不需要添加，大于则添加并调整堆，以上重复到结束，则最后剩下就是前N个用户
        PriorityQueue<MemberSimilarity> minHeap = new PriorityQueue<>(new Comparator<MemberSimilarity>() {
            @Override
            public int compare(MemberSimilarity o1, MemberSimilarity o2) {
                if (o1.getSimilarity() - o2.getSimilarity() > 0) {
                    return 1;
                } else if (o1.getSimilarity() - o2.getSimilarity() < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        for (MemberSimilarity memberSimilarity : memberSimilarityList) {
            if (minHeap.size() < topN) {
                minHeap.offer(memberSimilarity);
                System.out.println(minHeap.peek().getSimilarity());
            } else if (minHeap.peek().getSimilarity() < memberSimilarity.getSimilarity()) {
                minHeap.poll();
                minHeap.offer(memberSimilarity);
            }
        }
        // 把得到的最大的相似度的用户的id取出来(不要取它自己)
        while (!minHeap.isEmpty()){
            MemberSimilarity memberSimilarity = minHeap.poll();
            if (memberSimilarity.getMemberId().equals(memberId)){
                similarityList.add(memberSimilarity.getMemberRefId());
            }else{
                similarityList.add(memberSimilarity.getMemberId());
            }
        }

        return similarityList;
    }


    //找出与userId购买行为最相似的topN个用户,记录用户id与相似度
    public static ConcurrentHashMap<String, Double> getSimilarityBetweenUsers2(String memberId, List<MemberSimilarity> memberSimilarityList, Integer topN) {
        // 用来记录与userId相似度最高的前N个用户的id
        ConcurrentHashMap<String, Double> similarityList = new ConcurrentHashMap<>();

        // 堆排序找出最高的前N个用户，建立小根堆，每次需要添加的数先与堆顶进行比较，小于堆顶则不需要添加，大于则添加并调整堆，以上重复到结束，则最后剩下就是前N个用户
        PriorityQueue<MemberSimilarity> minHeap = new PriorityQueue<>(new Comparator<MemberSimilarity>() {
            @Override
            public int compare(MemberSimilarity o1, MemberSimilarity o2) {
                if (o1.getSimilarity() - o2.getSimilarity() > 0) {
                    return 1;
                } else if (o1.getSimilarity() - o2.getSimilarity() < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        for (MemberSimilarity memberSimilarity : memberSimilarityList) {
            if (minHeap.size() < topN) {
                minHeap.offer(memberSimilarity);
                System.out.println(minHeap.peek().getSimilarity());
            } else if (minHeap.peek().getSimilarity() < memberSimilarity.getSimilarity()) {
                minHeap.poll();
                minHeap.offer(memberSimilarity);
            }
        }
        // 把得到的最大的相似度的用户的id取出来(不要取它自己)
        while (!minHeap.isEmpty()){
            MemberSimilarity memberSimilarity = minHeap.poll();
            if (memberSimilarity.getMemberId().equals(memberId)){
                similarityList.put(memberSimilarity.getMemberRefId(), memberSimilarity.getSimilarity());
            }else{
                similarityList.put(memberSimilarity.getMemberId(), memberSimilarity.getSimilarity());
            }
        }

        return similarityList;
    }



    /**
     * 将用户的点击数归一化，使数据更具有可比性，再以次分数来计算最值得推荐的商品
     * @param  memberActives 用户的行为列表
     * @return 计算出的用户对每一类商品的评分（五分制）
     */

    public static List<MemberActiveCommodity> getActiveScoreByCommodity(List<MemberActiveCommodity> memberActives){
        //该map用于将用户行为列表按照用户id进行分组
        ConcurrentHashMap<String, List<MemberActiveCommodity>> temp = new ConcurrentHashMap<>();
        //开始组装
        for (MemberActiveCommodity memberActive : memberActives) {
            List<MemberActiveCommodity> list;
            if (temp.containsKey(memberActive.getMemberId())){
                list = temp.get(memberActive.getMemberId());
            }else{
                list = new ArrayList<>();
            }
            list.add(memberActive);
            temp.put(memberActive.getMemberId(), list);
        }
        List<MemberActiveCommodity> res = new ArrayList<>();
        //计算出每个用户对所有种类的点击的总和
        for (String memberId : temp.keySet()) {
            double maxHits = 0;//每个用户的最大点击数
            double minHits = 0;//每个用户的最小点击数
            List<MemberActiveCommodity> memberActivesForTotal = temp.get(memberId);
            for (MemberActiveCommodity memberActive : memberActivesForTotal) {
                maxHits = maxHits > memberActive.getClicks() ? maxHits : memberActive.getClicks();
                minHits = minHits < memberActive.getClicks() ? minHits : memberActive.getClicks();
            }
            //使用归一化特征缩放(0-1缩放)
            //计算对每个二级商品种类的分值
            for (MemberActiveCommodity memberActive : memberActivesForTotal) {
                long x = memberActive.getClicks();
                memberActive.setScore(BigDecimal.valueOf((x-minHits)/(maxHits-minHits)*5.0));
                res.add(memberActive);
            }
        }
        return res;
    }


    /**
     * 将用户的点击数归一化，使数据更具有可比性，再以次分数来计算最值得推荐的种类
     * @param  memberActives 用户的行为列表
     * @return 计算出的用户对每一类商品的评分（五分制）
     */

    public static List<MemberActive> getActiveScore(List<MemberActive> memberActives){
        //该map用于将用户行为列表按照用户id进行分组
        ConcurrentHashMap<String, List<MemberActive>> temp = new ConcurrentHashMap<>();
        //开始组装
        for (MemberActive memberActive : memberActives) {
            List<MemberActive> list;
            if (temp.containsKey(memberActive.getId())){
                 list = temp.get(memberActive.getId());
            }else{
                 list = new ArrayList<>();
            }
            list.add(memberActive);
            temp.put(memberActive.getId(), list);
        }
        List<MemberActive> res = new ArrayList<>();
        //计算出每个用户对所有种类的点击的总和
        for (String memberId : temp.keySet()) {
            double maxHits = 0;//每个用户的最大点击数
            double minHits = 0;//每个用户的最小点击数
            List<MemberActive> memberActivesForTotal = temp.get(memberId);
            for (MemberActive memberActive : memberActivesForTotal) {
                maxHits = maxHits > memberActive.getClicks() ? maxHits : memberActive.getClicks();
                minHits = minHits < memberActive.getClicks() ? minHits : memberActive.getClicks();
            }
            //使用归一化特征缩放(0-1缩放)
            //计算对每个二级商品种类的分值
            for (MemberActive memberActive : memberActivesForTotal) {
                long x = memberActive.getClicks();
                memberActive.setScore((x-minHits)/(maxHits-minHits)*5.0);
                res.add(memberActive);
            }
        }
        return res;
    }

    /**
     * 到前top-N中用户的行为中访问的二级类目中查找最常点击的二级类目，并获得被推荐的类目id
     * @param memberActives 一位相似用户的行为集合
     * @param count 是这些用户的每个人推荐的类目个数
     * @return 可以推荐给userId的二级类目id列表
     */
    public static List<MemberActive> getRecommendCategorySecond(List<MemberActive> memberActives, int count) {
        List<MemberActive> recommedCategorySecond = new ArrayList<>();

        Collections.sort(memberActives, new Comparator<MemberActive>() {
            @Override
            public int compare(MemberActive o1, MemberActive o2) {
                if (o1.getScore() - o2.getScore() < 0) {
                    return 1;
                } else if (o1.getScore() - o2.getScore() > 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        for (int i = 0; i < count; i++) {
            recommedCategorySecond.add(memberActives.get(i));
        }
        return recommedCategorySecond;
    }

}
