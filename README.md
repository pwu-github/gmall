SOA架构(面向服务架构)
简单理解：https://blog.csdn.net/qq_41723615/article/details/90201139

#用户模块
1、gmall-user-service     server-port: 8070
2、gmall-user-web         server-port: 8080

#后台管理系统
给商家录入商品基本信息使用
1、gmall-manage-service   server-port: 8071
2、gmall-manage-web       server-port: 8081
3、后台管理系统前端工程: 进入gmall-admin目录，cmd运行 npm run dev 命令
注：右键 cmd 窗口，调出属性设置。一定要把  快速编辑模式 去掉

#商品详情
1、gmall-item-service     server-port: 8072 (实际上不需要这个模块)
(商品详情页的数据通过spuService和skuService就可以获取，也就是说gmall-manage-service就可以实现,这也是为什么要服务拆分，面向服务)
2、gmall-item-web         server-port: 8082

#搜索模块
1、gmall-search-service   server-port：8073
2、gmall-search-web       server-port：8083

#购物车模块
1、gmall-cart-service     server-port：8074
2、gmall-cart-web         server-port：8084

#用户认证中心
1、gmall-passport-service server-port：8075 (实际上不需要这个模块，用gmall-user-service即可)
2、gmall-passport-web     server-port：8085

#订单模块
1、gmall-order-service    server-port：8076
2、gmall-order-web        server-port：8086

#支付模块
gmall-payment             server-port：8087
支付模块涉及到金钱交易，所以service和web在一个服务中，支付service只会在支付web层被调用，不会给其他系统调用。

#注册中心
dubbo管理页面: http://192.168.253.131:8080/dubbo     user/pass : root/root
1、启动dubbo和zookeeper服务(已经设置为开机自启，如果服务启动失败，需要手动启动)：
cd /etc/init.d 
service dubbo-admin start
service zookeeper start
2、Tomcat 连接失败，没有开启8080端口
3、zookeeper连接失败，没有开启2181端口

#redis(https://blog.csdn.net/weixin_38091140/article/details/91472362)
redis命令参考：http://redisdoc.com/
1、cd /opt/redis-3.0.4/src
./redis-server
这种启动方式需要一直打开窗口，不能进行其他操作，不太方便
2、后台进程的方式启动redis
nohup ./redis-server &
3、设置Redis开机自启
https://blog.csdn.net/Super_RD/article/details/89713996
4、连接Redis
redis-cli -h 192.168.253.131 -p 6379
keys *  //查看redis中的key
get key  //获得key对应的数据
5、获取hash结构的value
hvals user:1:cart
6、查询hash结构的value中的value
hget user:1:cart 107


#es
1、启动es
启动es必须要切换到es用户，否则没有权限启动
(1)控制台启动(日志打印在控制台)
cd /opt/es/elasticsearch-6.3.1/bin
./elasticsearch
(2)后台启动(会在bin目录下生成一个nohup.out文件，该文件包括了控制台打印的日志信息)
cd /opt/es/elasticsearch-6.3.1/bin
nohup ./elasticsearch &
2、es访问
http://192.168.253.131:9200/

#kibana(es交互工具)
访问：http://192.168.253.131:5601/
1、启动
/opt/es/kibana-6.3.1-linux-x86_64/bin
nohup ./kibana &
2、查看kibana进程
ps -ef | grep node

#activeMq
1、启动、关闭
service activemq start
service activemq stop
2、启动消费端
service activemq start
3、访问http://192.168.253.131:8161/    admin/admin
4、windows启动
D:\BaiduNetdiskDownload\《谷粒商城》\谷粒商城开发工具\apache-activemq-5.12.0-bin\apache-activemq-5.12.0\bin
cmd ： activemq start

#存储sku信息
http://192.168.253.131:5601/app/kibana#/dev_tools/console?_g=()
`PUT gmall
 {
   "mappings": {
     "PmsSkuInfo":{
       "properties": {
         "id":{
           "type": "keyword",
           "index": false
         },
         "skuName":{
           "type": "text",
           "analyzer": "ik_max_word"
         },
         "skuDesc":{
           "type": "text",
           "analyzer": "ik_smart"
         },
         "catalog3Id":{
           "type": "keyword"
         },
         "price":{
           "type": "double"
         },
         "skuDefaultImg":{
           "type": "keyword",
           "index": false
         },
         "hotScore":{
           "type": "double"
         },
         "productId":{
           "type": "keyword"
         },
         "skuAttrValueList":{
           "properties": {
             "attrId":{
               "type": "keyword"
             },
             "valueId":{
               "type": "keyword"
             }
           }
         }
       }
     }
   }
 }
`

#配置本地域名解析
将127.0.0.1 localhost 映射为指定的域名
打开C:\Windows\System32\drivers\etc 下的hosts文件
如下配置即可： 127.0.0.1      localhost   user.gmall.com
配置成功，管理员打开cmd命令行，输入 ipconfig /flushdns ,刷新DNS，即可用 user.gmall.com:port 访问本地启动的服务
可以进一步用Nginx代理端口号，实现用user.gmall.com直接访问
当配置过多的域名时，可能会解析失败，把解析失败的域名位置往前调整，直到每一个域名都能被解析出来

#常见的错误
1、serviceImpl没有加@Service注解（dubbo包）
2、主方法没有加@MapperScan 注解（dubbo包）
3、controller 没有加@CrossOrigin注解，注入时用@Reference注解（dubbo包）。

4、端口没有开放
检查端口是否开放：https://www.cnblogs.com/sxmny/p/11224842.html
开放端口：firewall-cmd --zone=public --add-port=61616/tcp --permanent
重启防火墙：firewall-cmd --reload

5、如果遇到thymeleaf解析HTML问题，可在pom文件中加入如下依赖，并在properties配置文件中加入
spring.thymeleaf.mode=LEGACYHTML5，目的是为了让springboot的thymeleaf在解析HTML时没有那么严格
<dependency>
     <groupId>net.sourceforge.nekohtml</groupId>
     <artifactId>nekohtml</artifactId>
 </dependency>
 
 <dependency>
     <groupId>xml-apis</groupId>
     <artifactId>xml-apis</artifactId>
 </dependency>
 
 <dependency>
 	<groupId>org.apache.xmlgraphics</groupId>
 	<artifactId>batik-ext</artifactId>
 </dependency>
 
6、如果启动服务时，只能启动一个，并且报了dubbo端口号绑定类似的错误，就在properties文件中配置dubbo协议之间的通讯端口号，自己任意配置
spring.dubbo.protocol.port=20881

7、解决request.getRemoteAddr()获取的值为0:0:0:0:0:0:0:1这个小问题
https://www.cnblogs.com/qrlozte/p/3532540.html
打开C:\Windows\System32\drivers\etc 下的hosts文件,注释掉  #	::1  localhost。如果还是不行，就不用localhost，用127.0.0.1访问，或者用域名访问



