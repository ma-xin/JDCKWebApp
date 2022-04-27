> 想了下，脚本在青龙容器里面运行，却使用青龙的openApi去操作CK，感觉有点大材小用，翻了翻青龙的源码，发现直接操作本地文件写CK，再更新一下env就可以了。  

记录下操作代码（2.10.6以上版本）：  

1. 操作CK存储的数据库；  
    CK存储在青龙目录 ql/db/database.sqlite(sqlite是轻量型的关系数据库)中，表名是 Envs ，字段名 ['id', 'value', 'timestamp', 'status', 'position', 'name', 'remarks', 'createdAt', 'updatedAt'] ， 对这个数据库进行修改 。  

2. 同步os.environ；  
    修改青龙目录 ql/config/env.sh。

这样就可以了。



