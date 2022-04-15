import requests
import json
import time
import urllib
import os
import datetime

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
    print("请求状态 【{}】".format(rep.status_code))
    if rep.status_code == 200:       
        content = rep.content.decode('utf-8')       
        return content
    else:
        print("请求错误 【{}】".format(rep.status_code))
        return None


"""
删除评论
"""

def delete(gitee_id):
    url = 'https://gitee.com/api/v5/repos/{}/{}/issues/comments/{}?access_token={}' \
        .format(OWNER, REPO, gitee_id, ACCESS_TOKE)
    resp = requests.delete(url)
    print("请求状态 【{}】".format(resp.status_code))
    if resp.status_code == 200:
        content = resp.content.decode('utf-8')       
        return content
    else:
        print("请求错误 【{}】".format(resp.status_code))
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
    print("开始登录 url:【{}】 , \ndata:{}".format(url, payload))     
    resp = requests.post(url, data = payload)
    print("请求状态 【{}】".format(resp.status_code))
    if resp.status_code == 200:
        content = resp.content.decode('utf-8')
        result = json.loads(content)
        if(result['code'] == 200):
            token = result['token']
            print('登录成功【{}】'.format(token))
            return token
        else:
            print('登录失败【{}】'.format(content))
            return ""
    else:
        print("请求错误 【{}】".format(resp.status_code))
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
        print('cookie格式错误，未找到pt_pin')
        print(cookie)
        return None
    url = URL_QL_Env_Search.format(urllib.parse.quote(searchValue),getTimestamp())
    #添加请求头
    headers = {
        'Authorization':'Bearer {}'.format(token)
    }
    print("开始查询环境变量 url:【{}】 ".format(url)) 
    resp = requests.get(url, headers = headers)
    print("请求状态 【{}】".format(resp.status_code))
    if resp.status_code == 200:
        content = resp.content.decode('utf-8')
        result = json.loads(content)
        if(result['code'] == 200):
            array = result['data']
            print('*********************')
            print('查询到【{}】'.format(len(array)))
            print(content)
            print('........')
            if(len(array)==0):
                # 添加变量
                  saveEnv(token, '', cookie, searchValue)  
            else:
                _value = array[0]['value']
                _id = array[0]['_id']
                _status = array[0]['status']
                _remarks = array[0]['remarks']
                if(_value == cookie):
                    statusName = '启用' if _status == 0 else '禁用'
                    print('cookie是最新值，当前QL的cookie状态【{}】'.format(statusName))
                else:
                     # 修改变量   
                    saveEnv(token, _id, cookie, _remarks)
                if(_status != 0):
                    #解除cookie禁用
                    enableEnv(token, _id)
            print('请求成功【{}】'.format(content))
        else:
            print('请求失败【{}】'.format(content))
    else:
        print("请求错误 【{}】".format(resp.status_code))


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
        print("开始修改环境变量 url:【{}】 , \ndata:{}".format(url, payload)) 
        resp = requests.put(url, data = payload, headers=headers)
    else:
        print("开始添加环境变量 url:【{}】 , \ndata:{}".format(url, payload)) 
        resp = requests.post(url, data = payload, headers=headers)
    print("请求状态 【{}】".format(resp.status_code))
    if resp.status_code == 200:
        content = resp.content.decode('utf-8')
        result = json.loads(content)
        if(result['code'] == 200):
            print('请求成功【{}】'.format(content))
        else:
            print('请求失败【{}】'.format(content))
    else:
        print("请求错误 【{}】".format(resp.status_code))


#启用变量
def enableEnv(token, id):
    url = URL_QL_Env_Enable.format(getTimestamp())
     #添加请求头
    headers = {
        "content-type":"application/json",
        'Authorization':'Bearer {}'.format(token)
    }
    payload = json.dumps([id])
    print("开始启用环境变量 url:【{}】 , \ndata:{}".format(url, payload)) 
    resp = requests.put(url, data = payload, headers=headers)
    print("请求状态 【{}】".format(resp.status_code))
    if resp.status_code == 200:
        content = resp.content.decode('utf-8')
        result = json.loads(content)
        if(result['code'] == 200):
            print('请求成功【{}】'.format(content))
        else:
            print('请求失败【{}】'.format(content))
    else:
        print("请求错误 【{}】".format(resp.status_code))

def getCacheToken():
    cur_path = os.path.abspath(os.path.dirname(__file__))
    with open('/ql/config/auth.json', 'r') as f:
            token=json.loads(f.read())['token']
    print('获取到ql/config/auth.json的缓存token')
    print(token)
    url = URL_QL_Env_Search.format('',getTimestamp())
    #添加请求头
    headers = {
        'Authorization':'Bearer {}'.format(token)
    }
    print("开始验证token是否失效 ".format(url)) 
    resp = requests.get(url, headers = headers)
    print("验证token请求状态 【{}】".format(resp.status_code))
    if(resp.status_code == 401):
        print('token已失效，登陆过期')
        return ''
    else:
        return token
        
    
           
datas = get()
dataArray = json.loads(datas)
print(type(dataArray))
if len(dataArray)==0:
    print("没有cookie需要更新，运行结束！")
    exit()
print('检测到共有【{}】个cookie'.format(len(dataArray)))
#检测缓存token是否失效
ql_token = getCacheToken()
if(len(ql_token)==0):
    ql_token = loginQL()
if len(ql_token)>0:
    index = 0
    for value in dataArray:
        git_value = value['body']
        print("开始处理第{}个cookie：【{}】".format(++index, git_value))
        handleEnv(ql_token, git_value)
        gitee_id = value['id']
        print('删除comment，id=【{}】'.format(gitee_id))
        delete(gitee_id)
        print('====================================\n')