> 想了下，脚本在青龙容器里面运行，却使用青龙的openApi去操作CK，感觉有点大材小用，翻了翻青龙的源码，发现直接操作本地文件写CK，再更新一下env就可以了。  

记录下操作代码（2.10.6以上版本）：  

1. 操作CK存储的数据库；  
    CK存储在青龙目录 ql/db/database.sqlite(sqlite是轻量型的关系数据库)中，表名是 Envs ，字段名 ['id', 'value', 'timestamp', 'status', 'position', 'name', 'remarks', 'createdAt', 'updatedAt'] ， 对这个数据库进行修改 。  

2. 同步os.environ；  
    修改青龙目录 ql/config/env.sh。

这样就可以了。


未写完的测试代码 先放着
```
import os
import logging
import sys
import sqlite3
import datetime

# 日志模块
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
logFormat = logging.Formatter("%(message)s")

# 日志输出流
stream = logging.StreamHandler()
stream.setFormatter(logFormat)
logger.addHandler(stream)

def env(key):
    return os.environ.get(key)

"""
conn = sqlite3.connect("../db/database.sqlite")
c = conn.cursor()

cursor = c.execute("SELECT name FROM sqlite_master where type='table' order by name")
for row in cursor:
    logger.info(f"value:{row[0]}")


col_names=[]
c.execute('pragma table_info({})'.format('Envs'))
col_name=c.fetchall()
col_name=[x[1] for x in col_name]
col_names.append(col_name)
logger.info(f"{col_names}")

logger.info("\n ******************************** \n")


cursor = c.execute("select * from Envs")
for row in cursor:
    logger.info(f"row[0] type: {type(row[0])} ,value : {row[0]}")
    logger.info(f"row[1] type: {type(row[1])} ,value : {row[1]}")
    logger.info(f"row[2] type: {type(row[2])} ,value : {row[2]}")
    logger.info(f"row[3] type: {type(row[3])} ,value : {row[3]}")
    logger.info(f"row[4] type: {type(row[4])} ,value : {row[4]}")
    logger.info(f"row[5] type: {type(row[5])} ,value : {row[5]}")
    logger.info(f"row[6] type: {type(row[6])} ,value : {row[6]}")
    logger.info(f"row[7] type: {type(row[7])} ,value : {row[7]}")
    logger.info(f"row[8] type: {type(row[8])} ,value : {row[8]}")

    logger.info("\n ====================== \n")


createdAt= "2022-04-22T06:17:46.724Z"
#id= 13
name= "JD_COOKIE"
position= 649999999.875
remarks= "测试DB文件操作"
status= 0
timestamp= "Fri Apr 22 2022 14:17:46 GMT+0800 (中国标准时间)"
updatedAt= "2022-04-22T09:39:24.945Z"
value= "pt_pin=12yuop;pt_key=sjfkshafuweiryuihjskdf"

logger.info("\n ******************************** \n")

#['id', 'value', 'timestamp', 'status', 'position', 'name', 'remarks', 'createdAt', 'updatedAt']

c = conn.cursor()
cursor = c.execute(f"insert into Envs (name,position,remarks,status,timestamp,value,createdAt,updatedAt) values (\"{name}\",{position},\"{remarks}\",{status},\"{timestamp}\",\"{value}\",\"{createdAt}\",\"{updatedAt}\")")
conn.commit()

logger.info("\n 数据插入完成 \n")
logger.info("\n ******************************** \n")

"""
gcc =  env('JD_COOKIE')
logger.info(f"JD_COOKIE: {gcc}")

"""
logger.info("\n ******************************** \n")

c = conn.cursor()
cursor = c.execute("select * from Envs")
for row in cursor:
    logger.info(f"row[0] type: {type(row[0])} ,value : {row[0]}")
    logger.info(f"row[1] type: {type(row[1])} ,value : {row[1]}")
    logger.info(f"row[2] type: {type(row[2])} ,value : {row[2]}")
    logger.info(f"row[3] type: {type(row[3])} ,value : {row[3]}")
    logger.info(f"row[4] type: {type(row[4])} ,value : {row[4]}")
    logger.info(f"row[5] type: {type(row[5])} ,value : {row[5]}")
    logger.info(f"row[6] type: {type(row[6])} ,value : {row[6]}")
    logger.info(f"row[7] type: {type(row[7])} ,value : {row[7]}")
    logger.info(f"row[8] type: {type(row[8])} ,value : {row[8]}")

    logger.info("\n ====================== \n")

"""

logger.info("\n 修改青龙目录 ql/config/env.sh 还没写\n")

```


截取qinglong 同步env.sh 的源码 地址在 https://github.com/whyour/qinglong/blob/e07d2b6639984833bc012e3afae54b17d4549008/back/services/env.ts
```

public async set_envs() {
    const envs = await this.envs(
      '',
      { position: -1 },
      { name: { [Op.not]: null } },
    );
    const groups = _.groupBy(envs, 'name');
    let env_string = '';
    for (const key in groups) {
      if (Object.prototype.hasOwnProperty.call(groups, key)) {
        const group = groups[key];

        // 忽略不符合bash要求的环境变量名称
        if (/^[a-zA-Z_][0-9a-zA-Z_]+$/.test(key)) {
          let value = _(group)
            .filter((x) => x.status !== EnvStatus.disabled)
            .map('value')
            .join('&')
            .replace(/"/g, '\"')
            .trim();
          env_string += `export ${key}="${value}"\n`;
        }
      }
    }
    fs.writeFileSync(config.envFile, env_string);
  }

```

