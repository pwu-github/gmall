SOA架构(面向服务架构)
简单理解：https://blog.csdn.net/qq_41723615/article/details/90201139

#用户模块
1、用户模块服务端(service)：gmall-user-service serer-port:8070
2、用户模块web端(controller)：gmall-user-web server-port:8080

#后台管理系统是给商家录入商品基本信息使用
1、后台管理系统服务端(service)：gmall-manage-service serer-port:8071
2、后台管理系统web端(controller)：gmall-manage-web server-port:8081
3、后台管理系统前端工程: 进入gmall-admin目录，cmd运行 npm run dev 命令
注：右键 cmd 窗口，调出属性设置。一定要把  快速编辑模式 去掉

#商品详情页面
1、gmall-item-service  serer-port:8072 (实际上不需要这个模块)
(商品详情页的数据通过spuService和skuService就可以获取，也就是说gmall-manage-service就可以实现,这也是为什么要服务拆分，面向服务)
2、gmall-item-web      serer-port:8082

#注册中心
dubbo管理页面: http://192.168.253.131:8080/     user/pass : root/root
1、启动dubbo和zookeeper服务：
cd /etc/init.d 
service dubbo-admin start
service zookeeper restart
2、Tomcat 连接失败，没有开启8080端口
3、zookeeper连接失败，没有开启2181端口
检查端口是否开放：https://www.cnblogs.com/sxmny/p/11224842.html
开放端口：firewall-cmd --zone=public --add-port=8080/tcp --permanent
重启防火墙：firewall-cmd --reload

#常见的错误
1、serviceImpl没有加@Service注解（dubbo包）
2、主方法没有加@MapperScan 注解（dubbo包）
3、controller 没有加@CrossOrigin注解（dubbo包），注入时用@Reference注解（dubbo包）。



