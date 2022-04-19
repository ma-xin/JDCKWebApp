import requests
import json
import time
import urllib
import os
import datetime
import logging


# 日志模块
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
logFormat = logging.Formatter("%(message)s")

# 日志输出流
stream = logging.StreamHandler()
stream.setFormatter(logFormat)
logger.addHandler(stream)


# 引入配置文件
try:
    from giteeCKConfig import GiteeConfig,QLConfig
except Exception as error:
    logger.info('缺少配置文件【giteeCKConfig.py】')
    logger.info(f'失败原因:{error}')
    exit()

#gitEE Token令牌
ACCESS_TOKE = 'xxxxxxxxxxxxxxxx',
#gitEE owner
OWNER = 'xxxxxx',
#gitEE repo
REPO = 'xxxxxx',
#gitEE issue number
NUMBER = 'xxxxxx',

#青龙登录账号
QL_Login_name = 'admin',
#青龙登录密码
QL_Login_password = 'admin',

# 127.0.0.1有的环境出现无法访问，最好还是改成本地ip
URL_QL = 'http://192.168.0.155:5700/api/',

#登录接口
URL_QL_Login = URL_QL+'login?t={}'
#查询环境变量接口
URL_QL_Env_Search = URL_QL+'envs?searchValue={}&t={}'
#新增、修改环境变量接口
URL_QL_Env = URL_QL+'envs?t={}'
#启用环境变量接口
URL_QL_Env_Enable = URL_QL+'envs/enable?t={}'
#获取青龙版本（高版本）
URL_QL_VERSION = URL_QL+'system?t={}'



# 第三方库
try:
    import requests
except ModuleNotFoundError:
    logger.info("缺少requests依赖！程序将尝试安装依赖！")
    os.system("pip3 install requests -i https://pypi.tuna.tsinghua.edu.cn/simple")
    os.execl(sys.executable, 'python3', __file__, *sys.argv)


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
        return True
    url = URL_QL_Env_Search.format(urllib.parse.quote(searchValue),getTimestamp())
    #添加请求头
    headers = {
        'Authorization':'Bearer {}'.format(token)
    }
    resp = requests.get(url, headers = headers)
    flag = False 
    if resp.status_code >= 200 and resp.status_code < 300: 
        content = resp.content.decode('utf-8')
        result = json.loads(content)
        if(result['code'] == 200):
            array = result['data']
            logger.info('查询到【{}】'.format(len(array)))
            if(len(array)==0):
                logger.info("没有查询到CK【{}】，开始添加 ".format(searchValue)) 
                # 添加变量
                flag = saveEnv(token, '', cookie, searchValue)  
            else:
                logger.info("查询到{}个CK【{}】，开始修改 ".format(len(array), searchValue)) 
                _value = array[0]['value']
                _status = array[0]['status']
                if(_value == cookie):
                    statusName = '启用' if _status == 0 else '禁用'
                    notify('CK【{}】是最新值，不用修改，CK状态【{}】'.format(searchValue, statusName))
                    flag = True
                else:
                    # 修改变量   
                    # 2.11.0 字段 _id 改成 id
                    _id = array[0]['_id' if qlVersion<'2.11.0' else 'id']  
                    # 高版本remarks可为null
                    _remarks = array[0].get('remarks','')
                    flag = saveEnv(token, _id, cookie, _remarks)
                if(flag and _status != 0):
                    #解除cookie禁用
                    flag = enableEnv(token, _id) 
        else:
            logger.info('查询环境变量失败【{}】'.format(content))
    else:
        logger.info("查询环境变量错误 【{}】".format(resp.status_code))
    return flag


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
        # 2.11.0 字段 _id 改成 id
        payload['_id' if qlVersion<'2.11.0' else 'id'] = id
        resp = requests.put(url, data = payload, headers=headers)
    else:
        #2.10.6以上版本，新增环境变量需要传数组
        if(qlVersion>='2.10.6'):
            headers = {
                "content-type":"application/json",
                'Authorization':'Bearer {}'.format(token)
            }
            payload = json.dumps([payload])
            resp = requests.post(url, data = payload, headers=headers)
        else:
            resp = requests.post(url, data = payload, headers=headers)
    if resp.status_code >= 200 and resp.status_code < 300:
        content = resp.content.decode('utf-8')
        result = json.loads(content)
        if(result['code'] == 200):
            notify(f"成功->{'添加' if len(id)==0 else '修改'}CK:{rs[len(rs)-1]}")
            return True
        else:
            notify(f"失败->{'添加' if len(id)==0 else '修改'}CK:{rs[len(rs)-1]}")
            logger.info('保存变量失败【{}】'.format(content))
            return False
    else:
        logger.info("保存变量错误 【{}】".format(resp.status_code))
        return False


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
            return True
        else:
            logger.info('启用变量失败【{}】'.format(content))
            return False
    else:
        logger.info("启用变量错误 【{}】".format(resp.status_code))
        return False


#获取token （兼容）
def getToken():
    token = ''
    # 1. 判断系统目录下是否存在auth.json 
    if(len(token)==0):
        try:
            token = getCacheToken()
        except Exception as error:
            logger.info('无法使用缓存auth.json获取token')
    # 2. 使用登录接口获取token
    if(len(token)==0):
        token = loginQL()
       
    return token

#低版本青龙使用缓存文件获取token（具体适用到哪个版本，没研究过）
def getCacheToken():
    #青龙token的缓存地址  
    # 2.12.0以下（不包含）在 /ql/config/auth.json , 2.12.0（包含）以上在 /ql/data/config/auth.json 
    QL_Auth_Path = '/ql/config/auth.json'
    QL_Auth_Data_Path = '/ql/data/config/auth.json'
    #检查anth.json 是否存在
    if not (os.path.exists(QL_Auth_Path)):
        QL_Auth_Path = QL_Auth_Data_Path
        
    cur_path = os.path.abspath(os.path.dirname(__file__))
    with open(QL_Auth_Path, 'r') as f:
            token=json.loads(f.read())['token']
    logger.info(f'获取到{QL_Auth_Path}的缓存token')
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
        

#尝试获取青龙版本
def getQlVersion(token):
    url = URL_QL_VERSION.format(getTimestamp())
     #添加请求头
    headers = {
        'Authorization':'Bearer {}'.format(token)
    }
    try:
        resp = requests.get(url, headers=headers)
        if resp.status_code >= 200 and resp.status_code < 300: 
            content = resp.content.decode('utf-8')
            result = json.loads(content)
            if(result['code'] == 200):
                return result['data']['version']
            else:
                logger.info(f"获取青龙版本失败{content}") 
                return '0'
        else:
            logger.info(f"获取青龙版本接口错误{resp.status_code}") 
            return '0'
    except Exception as error:
        logger.info(error)
        logger.info(f"获取青龙版本接口异常") 
        return '0'


# =================  main ==========================
      
datas = get()
dataArray = json.loads(datas)
if len(dataArray)==0:
    logger.info("没有cookie需要更新，运行结束！")
    exit()
logger.info('检测到共有【{}】个cookie'.format(len(dataArray)))

#检测缓存token是否失效
ql_token = getToken()
if len(ql_token)>0:
    #尝试获取青龙版本
    qlVersion = getQlVersion(ql_token)
    logger.info(f'当期青龙版本：{qlVersion}')   
    index = 0
    # 日志录入时间
    notify(f"任务:拉取Gitee上的CK\n时间:{time.strftime('%Y-%m-%d %H:%M:%S',time.localtime())}")
    notify(f"共检测到{len(dataArray)}个CK")
    for value in dataArray:
        index=index+1
        git_value = value['body']
        logger.info("开始处理第{}个cookie：【{}】".format(index, git_value))
        flag = False
        try:
            flag = handleEnv(ql_token, git_value)
        except Exception as error: 
            logger.info(f"CK写入青龙异常。") 
        if(flag):
            gitee_id = value['id']
            logger.info('删除comment，id=【{}】'.format(gitee_id))
            delete(gitee_id)
            logger.info('====================================\n')
    
else:
    logger.info('获取青龙token失败，无法操作CK！')
send('拉取Gite缓存CK',allMess)
    