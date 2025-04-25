# 代码沙箱

## 准备工作

### Docker安装镜像

- alpine:3.21 `docker pull alpine:3.21`
- openjdk:8-alpine `docker pull openjdk:8-alpine`
- python:3.11-alpine `docker pull python:3.11-alpine`

> 这一步也可以不执行, 修改DockerRunner里面的FIRST_INIT为True即可实现首先访问沙箱时自动安装镜像

### linux安装程序编译环境

jdk

```bash
##安装jdk
sudo apt update
sudo apt install openjdk-8-jdk

##验证
java -version
```

c/cpp

```bash
##安装GNU工具链
sudo apt update
sudo apt install build-essential

##验证
g++ --verison
```

go

```bash
##进入安装目录
cd /usr/local

##下载解压go
sudo wget https://go.dev/dl/go1.24.2.linux-amd64.tar.gz
sudo tar -zxvf go1.24.2.linux-amd64.tar.gz

##配置环境变量
sudo mkdir -p /home/go/bin /home/go/pkg /home/go/src
vi .bashrc
port GOROOT=/usr/local/go
export GOPATH=$HOME/go
export PATH=$PATH:$GOROOT/bin:$GOPATH/bin
source .bashrc

##验证
go version
```
