package com.scriptures.shareApp.util;

import com.scriptures.shareApp.dao.entity.MemberActiveCommodity;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

//基于物品的协同过滤的算法的实现
public class RecommendUtilsByitem {

    /**
     * 将用户的购买行为组装成一个map,key为userId，value也是一个map，这个map记录的是用户对每个商品的喜爱程度，即用户喜爱度矩阵
     * @param memberActives 用户的行为列表
     * @return 组装好的用户的购买行为的map集合
     */
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> assembleMemebrBehaviorForCommodity(List<MemberActiveCommodity> memberActives){
        ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> res = new ConcurrentHashMap<>();
        //遍历用户行为列表
        for (MemberActiveCommodity memberActive : memberActives) {
            //获得用户id
            String userId = memberActive.getMemberId();
            //获得商品id
            String commodityId = memberActive.getCommodityId();
            //获得该商品送分数，包括计算点击分数、是否分享过（后续可以根据分享次数）（有分享默认2分）、是否收藏过（有收藏默认3分）
            double totalScore = 0;
            totalScore += memberActive.getScore().doubleValue();
            totalScore += memberActive.getCollect()>0 ? 3 : 0;
            totalScore += memberActive.getShareCount()>0 ? 2 : 0;
            //进行组装
            if (res.containsKey(userId)){//存在则取出更新
                ConcurrentHashMap<String, Double> exitMap = res.get(userId);
                exitMap.put(commodityId, totalScore);
                res.put(userId, exitMap);
            }else{//不存在就直接插入
                ConcurrentHashMap<String, Double> categorySecondMap = new ConcurrentHashMap<>();
                categorySecondMap.put(commodityId, totalScore);
                res.put(userId, categorySecondMap);
            }
        }
        return res;
    }

    //计算物品相似度
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> calcSimilarityBetweenCommodity(List<String> commodityIds, ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> activeMap){
        ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> res = new ConcurrentHashMap<>();//两个物品同时存在的用户人数
        //遍历用户行为列表
        for (int i = 0; i < commodityIds.size(); i++) {
            for (int j = 0; j < commodityIds.size(); j++) {
                int count = 0;
                for (String key : activeMap.keySet()) {
                    ConcurrentHashMap<String, Double> temp = activeMap.get(key);
                    if (temp.containsKey(commodityIds.get(i)) && temp.containsKey(commodityIds.get(j))){
                        count++;
                    }
                }
                ConcurrentHashMap<String, Integer> relationCount = res.getOrDefault(commodityIds.get(i), new ConcurrentHashMap<>());
                relationCount.put(commodityIds.get(j), count);
                res.put(commodityIds.get(i), relationCount);
            }
        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> resForSimilarityBetweenCommodity = new ConcurrentHashMap<>();//所有物品之间的相似度
        for (String commodityId : res.keySet()) {
            for (String refCommodityId : res.get(commodityId).keySet()) {
                int relationCount = res.get(commodityId).get(refCommodityId); //两个商品都有的用户数量
                int singleCountA = res.get(commodityId).get(commodityId);//拥有A商品的用户人数
                int singleCountB = res.get(refCommodityId).get(refCommodityId);//拥有B商品的用户人数
                double weight=0.5;//weight的范围是[0.5,1],提高weight,就可以惩罚热门物品itemb
                double valueA = Math.pow(singleCountA, 1 - weight);
                double valueB = Math.pow(singleCountB, weight);
                double temp = relationCount/(valueA*valueB);
                double similarityBetweenCommodity = commodityId.equals(refCommodityId) ? 0.0 : (double) Math.round(temp * 100) / 100;
                ConcurrentHashMap<String, Double> relationSimilarity = resForSimilarityBetweenCommodity.getOrDefault(commodityId, new ConcurrentHashMap<>());
                relationSimilarity.put(refCommodityId, similarityBetweenCommodity);
                resForSimilarityBetweenCommodity.put(commodityId, relationSimilarity);
            }
        }
        return resForSimilarityBetweenCommodity;
    }



    //使用物品相识度计算总分数
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> calcAllScore(ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> resForSimilarityBetweenCommodity,
                                                                                            ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> memberFavoriteMap){

        ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> AllScoreMap = new ConcurrentHashMap<>();
        //遍历用户喜好矩阵列表
        for (String memberId : memberFavoriteMap.keySet()) {
            double score = 0;
            ConcurrentHashMap<String, Double> temp = new ConcurrentHashMap<>();//用来封装一个用户对其喜爱的所有物品的喜爱程度分数
            for (String commodityId : memberFavoriteMap.get(memberId).keySet()) {//抽出
                temp.put(commodityId, memberFavoriteMap.get(memberId).get(commodityId));
            }
            ConcurrentHashMap<String, Double> oneMemberForScore = AllScoreMap.getOrDefault(memberId, new ConcurrentHashMap<>());//一个用户对所有物品的预测分数
            for (String row : resForSimilarityBetweenCommodity.keySet()) {
                double totalCol = 0;//用以保存矩阵相乘时，一行 乘 一列的总和
                for (String col : temp.keySet()) {//遍历相似度矩阵的列
                    totalCol += temp.get(col) * resForSimilarityBetweenCommodity.get(row).get(col);//注意了，商品相似矩阵是对称性矩阵即K[i][j] = K[j][i]
                }
                totalCol = (double) Math.round(totalCol * 100) / 100;//小数点精确到两位
                oneMemberForScore.put(row, totalCol);
                AllScoreMap.put(memberId, oneMemberForScore);//一个用户对所有物品的预测分数放入map中
            }
        }
        return AllScoreMap;
    }


}
