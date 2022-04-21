import requests
import json
import time
import urllib
import os
import datetime
import logging
 
#gitEE Token令牌
ACCESS_TOKE = 'xxxxxxxxxxxxxxxxx'

#gitEE owner
OWNER = 'xxxx'
#gitEE repo
REPO = 'xxxx'
#gitEE issue number
NUMBER = 'xxxx'

QL_Login_name = 'admin'
QL_Login_password = 'admin'

URL_QL = 'http://127.0.0.1:5700/api/'
URL_QL_Login = URL_QL+'login?t={}'
URL_QL_Env_Search = URL_QL+'envs?searchValue={}&t={}'
URL_QL_Env = URL_QL+'envs?t={}'
URL_QL_Env_Enable = URL_QL+'envs/enable?t={}'

# 日志模块
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
logFormat = logging.Formatter("%(message)s")

# 日志输出流
stream = logging.StreamHandler()
stream.setFormatter(logFormat)
logger.addHandler(stream)


# 检测配置文件并下载函数
# Ps:云函数可能不适用,该函数语法不够优雅,希望大佬给个优雅方案
def checkFile(urlList):
    for url in urlList:
        fileName = url.split('/')[-1]
        fileUrl = f'https://ghproxy.com/{url}'
        try:
            if not os.path.exists(fileName):
                global downFlag
                downFlag = True
                logger.info(f"`{fileName}`不存在,尝试进行下载...")
                content = requests.get(url=fileUrl).content.decode('utf-8')
                with open(file=fileName, mode='w', encoding='utf-8') as fc:
                    fc.write(content)
        except:
            logger.info(f'请手动下载配置文件`{fileName[:-3]}`到 {os.path.dirname(os.path.abspath(__file__))}')
            logger.info(f'下载地址:{fileUrl}\n')

# 检测必备文件
fileUrlList = [
    'https://raw.githubusercontent.com/Mashiro2000/HeyTapTask/main/sendNotify.py',
]
checkFile(fileUrlList)

# 配信文件
try:
    from sendNotify import send
except Exception as error:
    logger.info('推送文件有误')
    logger.info(f'失败原因:{error}')
 
# 配信内容格式
allMess = ''
def notify(content=None):
    global allMess
    allMess = allMess + content + '\n'
    logger.info(content)

"""
查询gitee的评论信息
"""

def get():
    url = 'https://gitee.com/api/v5/repos/{}/{}/issues/{}/comments?access_token={}&page=1&per_page=20&order=asc' \
        .format(OWNER, REPO, NUMBER, ACCESS_TOKE)
    rep = req(url)
    return rep


def req(url):
    rep = requests.get(url)
    logger.info("请求状态 【{}】".format(rep.status_code))
    if rep.status_code >= 200 and rep.status_code < 300:       
        content = rep.content.decode('utf-8')       
        return content
    else:
        logger.info("请求错误 【{}】".format(rep.status_code))
        return None


"""
删除评论
"""

def delete(gitee_id):
    url = 'https://gitee.com/api/v5/repos/{}/{}/issues/comments/{}?access_token={}' \
        .format(OWNER, REPO, gitee_id, ACCESS_TOKE)
    resp = requests.delete(url)
    if resp.status_code >= 200 and resp.status_code < 300:   
        content = resp.content.decode('utf-8')       
        return content
    else:
        logger.info("删除Gitee评论错误 【{}】".format(resp.status_code))
        return None


#获取系统时间戳
def getTimestamp():
    now = time.time() #返回float数据
    #毫秒级时间戳
    nowTimes = int(round(now * 1000))
    return nowTimes

def getNowDateTimeStr():
    return datetime.datetime.now().strftime('%Y-%m-%d')

#登录QL ， 获取token
def loginQL():
    url = URL_QL_Login.format(getTimestamp())
    payload = {
          "username": QL_Login_name ,
          "password": QL_Login_password 
        }
    resp = requests.post(url, data = payload)
    if resp.status_code >= 200 and resp.status_code < 300:
        content = resp.content.decode('utf-8')
        result = json.loads(content)
        if(result['code'] == 200):
            token = result['token']
            logger.info('登录成功')
            return token
        else:
            logger.info('登录失败【{}】'.format(content))
            return ""
    else:
        logger.info("登录错误 【{}】".format(resp.status_code))
        return ''


#http://127.0.0.1:5700/api/crons?searchValue=jd_EQNaHmuBtTpn&t=1649232343.5590558
#查询环境变量
def handleEnv(token, cookie):
    searchValueArray = cookie.split(";")
    searchValue = ''
    for value in searchValueArray:
        if value.startswith('pt_pin='):
            searchValue = value.replace('pt_pin=','') 
    if(len(searchValue)==0):
        logger.info('cookie格式错误，未找到pt_pin')
        logger.info(cookie)
        return None
    url = URL_QL_Env_Search.format(urllib.parse.quote(searchValue),getTimestamp())
    #添加请求头
    headers = {
        'Authorization':'Bearer {}'.format(token)
    }
    resp = requests.get(url, headers = headers)
    if resp.status_code >= 200 and resp.status_code < 300: 
        content = resp.content.decode('utf-8')
        result = json.loads(content)
        if(result['code'] == 200):
            array = result['data']
            print('*********************')
            print('查询到【{}】'.format(len(array)))
            print(content)
            print('........')
            if(len(array)==0):
                logger.info("没有查询到CK【{}】，开始添加 ".format(searchValue)) 
                # 添加变量
                saveEnv(token, '', cookie, searchValue)  
            else:
                logger.info("查询到{}个CK【{}】，开始修改 ".format(len(array), searchValue)) 
                _value = array[0]['value']
                _status = array[0]['status']
                if(_value == cookie):
                    statusName = '启用' if _status == 0 else '禁用'
                    notify('CK【{}】是最新值，不用修改，CK状态【{}】'.format(searchValue, statusName))
                else:
                    # 修改变量   
                    _id = array[0]['_id']                
                    _remarks = array[0]['remarks']
                    saveEnv(token, _id, cookie, _remarks)
                if(_status != 0):
                    #解除cookie禁用
                    enableEnv(token, _id)
        else:
            logger.info('查询环境变量失败【{}】'.format(content))
    else:
        logger.info("查询环境变量错误 【{}】".format(resp.status_code))


#添加/修改变量
def saveEnv(token, id, cookie, remarks):
    url = URL_QL_Env.format(getTimestamp())
     #添加请求头
    headers = {
        'Authorization':'Bearer {}'.format(token)
    }
    rs = remarks.split('^^',1)
    nowTime = getNowDateTimeStr()
    remarks = '{}^^{}'.format(nowTime, rs[len(rs)-1])
    payload = {
          "name": "JD_COOKIE",
          "value": cookie,
          "remarks": remarks 
        }
    if len(id)>0:
        payload['_id'] = id
        resp = requests.put(url, data = payload, headers=headers)
    else:
        resp = requests.post(url, data = payload, headers=headers)
    if resp.status_code >= 200 and resp.status_code < 300:
        content = resp.content.decode('utf-8')
        result = json.loads(content)
        if(result['code'] == 200):
            notify(f"成功->{'添加' if len(id=0) else '修改'}CK:{rs[len(rs)-1]}")
        else:
            notify(f"失败->{'添加' if len(id=0) else '修改'}CK:{rs[len(rs)-1]}")
            logger.info('保存变量失败【{}】'.format(content))
    else:
        logger.info("保存变量错误 【{}】".format(resp.status_code))


#启用变量
def enableEnv(token, id):
    url = URL_QL_Env_Enable.format(getTimestamp())
     #添加请求头
    headers = {
        "content-type":"application/json",
        'Authorization':'Bearer {}'.format(token)
    }
    payload = json.dumps([id])
    resp = requests.put(url, data = payload, headers=headers)
    if resp.status_code >= 200 and resp.status_code < 300:
        content = resp.content.decode('utf-8')
        result = json.loads(content)
        if(result['code'] == 200):
            logger.info('启用变量成功')
        else:
            logger.info('启用变量失败【{}】'.format(content))
    else:
        logger.info("启用变量错误 【{}】".format(resp.status_code))



def getCacheToken():
    cur_path = os.path.abspath(os.path.dirname(__file__))
    with open('/ql/config/auth.json', 'r') as f:
            token=json.loads(f.read())['token']
    logger.info('获取到ql/config/auth.json的缓存token')
    url = URL_QL_Env_Search.format('',getTimestamp())
    #添加请求头
    headers = {
        'Authorization':'Bearer {}'.format(token)
    }
    logger.info("验证token是否失效 ".format(url)) 
    resp = requests.get(url, headers = headers)
    if(resp.status_code == 401):
        logger.info('token已失效，登陆过期')
        return ''
    else:
        return token
        
    
           
datas = get()
dataArray = json.loads(datas)
if len(dataArray)==0:
    logger.info("没有cookie需要更新，运行结束！")
    exit()
logger.info('检测到共有【{}】个cookie'.format(len(dataArray)))
#检测缓存token是否失效
ql_token = getCacheToken()
if(len(ql_token)==0):
    ql_token = loginQL()
if len(ql_token)>0:
    index = 0
    # 日志录入时间
    notify(f"任务:拉取Gitee上的CK\n时间:{time.strftime('%Y-%m-%d %H:%M:%S',time.localtime())}")
    notify(f"共检测到{len(dataArray)}个CK")
    for value in dataArray:
        git_value = value['body']
        logger.info("开始处理第{}个cookie：【{}】".format(index+1, git_value))
        handleEnv(ql_token, git_value)
        gitee_id = value['id']
        logger.info('删除comment，id=【{}】'.format(gitee_id))
        delete(gitee_id)
        logger.info('====================================\n')
    send('拉取Gite缓存CK',allMess)
