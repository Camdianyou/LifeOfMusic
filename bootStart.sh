#!/bin/sh
echo ======================
echo 自动化部署
echo ======================

echo 停止原来运行中的程序
APP_NAME=LifeOfMusic

tpid=`ps -ef | grep $APP_NAME | grep -v grep | grep -v kill | awk '{print $2}'`
if [ ${tpid} ]; then
    echo 'Stop Process ...'
    kill -15 $tpid
fi
sleep 2
tpid=`ps -ef | grep $APP_NAME | grep -v grep | grep -v kill | awk '{print $2}'`
if [ ${tpid} ]; then
    echo 'Stop Process ...'
    kill -9 $tpid
else
  echo 'Stop Success'
fi

echo 准备从git仓库里面拉取新代码
cd /usr/local/LifeOfMusic

echo 开始从Git仓库拉取最新代码
git pull
echo 拉取新代码成功

echo 开始打包
output=`mvn clean package -Dmaven.test.skip=true`

cd target

echo 启动项目
nohup java -jar lom_take_out-1.0-SNAPSHOT.jar &> LifeOfMusic.log &
echo 项目启动完成