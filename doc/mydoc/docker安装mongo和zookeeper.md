# 使用docker安装 mongodb和zookeeper


1. mac本地安装docker:
```brew install --cask --appdir=/Applications docker```
如果报错可以手动下载docker.dmg然后安装

2. 登陆docker

3. 使用 ``docker --version`` 查看是否安装成功

4. docker 拉取对应版本的镜像
   ```docker pull zookeeper:3.6.4```
   ```docker pull mongo:4.4-rc```
5. mac安装mysql（由于是M1芯片需要制定platform 否则报错）
   ```docker pull --platform linux/amd64 mysql:8.0.20```
5. `docker images` 查看所有镜像
6. 运行mondgodb

```agsl
创建三个文件夹 用来挂载容器文件：
sudo mkdir -p /User/docker/mongo/conf
sudo mkdir -p /User/docker/mongo/data
sudo mkdir -p /User/docker/mongo/log
-p 确保目录名称存在，不存在的就建一个

// 创建配置文件
sudo vim /User/docker/mongo/conf/mongodb.conf

//文件内容


#端口
port=27017
#数据库文件存放目录
dbpath=/data/mongo/data
#日志文件存放路径
logpath=/data/mongo/log
#使用追加方式写日志
logappend=true
#以守护线程的方式运行，创建服务器进程
fork=true
#最大同时连接数
maxConns=100
#不启用验证
#noauth=true
#每次写入会记录一条操作日志
journal=true
#存储引擎有mmapv1、wiredTiger、mongorocks
storageEngine=wiredTiger
#访问IP
bind_ip=0.0.0.0
#用户验证
auth=true
security.authorization: enabled


//创建并运行容器(注意修改后面的imageId)
docker run -d -p 27017:27017 -v /Users/docker/mongo/data:/data/db -v /Users/docker/mongo/conf:/data/conf -v /Users/docker/mongo/log:/data/log --name mongo 1d1d047 --auth

-d：后台运行
-p：将本机端口映射到容器端口 本机ip:容器ip
-v：将本地文件挂载到容器文件
--name：制定创建的容器名
最后跟上镜像id或者镜像名
--auth 使用密码才能访问容器服务


docker exec -it mongo mongo admin
# 创建一个名为 admin，密码为 123456 的用户。
>  db.createUser({ user:'admin',pwd:'123456',roles:[ { role:'userAdminAnyDatabase', db: 'admin'},"readWriteAnyDatabase"]});
# 尝试使用上面创建的用户信息进行连接。
> db.auth('admin', '123456')
```

7. 运行zookeeper
```agsl
sudo mkdir -p /Users/docker/zookeeper/data

(注意修改后面的imageId)
docker run -d -e TZ="Asia/Beijing" -p 2181:2181 -v /Users/docker/zookeeper/data:/data --name zookeeper --restart always 77736577

可以使用不加--restart always版本的命令
docker run -d -e TZ="Asia/Beijing" -p 2181:2181 -v /Users/docker/zookeeper/data:/data --name zookeeper 77736577

-e TZ="Asia/Beijing" # 指定北京时区 
--restart always #始终重新启动zookeeper
最后跟上镜像id
```

8. 运行mysql
```agsl
mkdir -p /User/docker/mysql
mkdir -p /User/docker/mysql/conf
mkdir -p /User/docker/mysql/data
mkdir -p /User/docker/mysql/logs
mkdir -p /User/docker/mysql/mysql-files

docker run --restart=always -p 3306:3306 --name mysql -e MYSQL_ROOT_PASSWORD="123456" -v /User/docker/mysql/conf:/etc/mysql/conf.d -v /User/docker/mysql/logs:/var/log/mysql -v /User/docker/mysql/data:/var/lib/mysql -v /User/docker/mysql/mysql-files:/var/lib/mysql-files -d mysql:8.0.20

```

9常用docker命令
```agsl
查询所有镜像：docker images
删除镜像：docker rmi 镜像ID    -f强制删除


查询运行的容器：docker ps    docker container ls

查询所有容器：docker ps -a    docker container ls -a

启动 停止 杀死容器
docker start 
docker stop
docker kill 

删除容器：docker container rm 容器id
```