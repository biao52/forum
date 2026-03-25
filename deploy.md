# forum部署步骤

1. 先在ubuntu系统上安装docker
2. 配置轩辕镜像源
3. 安装宝塔面板
4. 在宝塔面板的docker中安装mysql
5. 在宝塔面板的终端中安装redis
6. 在宝塔面板的软件商店中安装nginx和java环境管理器
7. 修改项目的application.yml文件并打包项目(使用 `mvn clean package '-Dmaven.test.skip=true' '-Dmybatis.generator.skip=true'`命令 ),并将jar包上传到/www/wwwroot/forum/下.(需创建forum文件夹)
8. 点击宝塔面板的"网站",点击点击"添加项目",选择jar包,并配置jdk.
9. 直接访问 [http://192.168.217.131:58080/index.html]即可.部署成功.