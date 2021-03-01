# CF-project
基于协同过滤算法的项目（未完成）  
该项目在之前的纷享app项目的基础之前上进行改进，去除文章部分模块，添加推荐模块  
该算法分别使用基于用户、基于物品两种方式进行实现（现只展示两种方法的工具类） 
# 推荐模块：
数据库方面
* 添加用户行为表：用于记录用户对商品的商品的点击、是否收藏、分享次数等，用于计算用户相似度、物品相似度
* 添加用户相似度表：用于记录已经计算好的用户相似度，以提升系统性能
* 添加物品相似度表：用于记录已经计算好的物品相似度，以提升系统性能  

后台代码实现
* 使用Map数据结构，将用户与其对应的行为数据、物品与其浏览过用户的次数构造成矩阵，进行相似度计算（相似度计算为余弦相似度公式）
![image](https://user-images.githubusercontent.com/33857411/109535459-8c0b6580-7af7-11eb-851b-7784d037a11d.png)


* 设向量 A = (A1,A2,A3,...,An), B = (B1,B2,B3,...,Bn)，推广到多维，公式为：  
![image](https://user-images.githubusercontent.com/33857411/109535600-b8bf7d00-7af7-11eb-8760-69c76ef92ff9.png)  
其中的 A1,A2,A3...就可以理解为该用户对不同的商品的点击量。（这里将使用数据归一化，将其转化为五分值，好进行比较）

