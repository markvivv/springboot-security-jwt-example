# 使用方法
- 打开`application.yml`配置数据库连接和账号，数据库账号需要能够访问到mysql的`information_schema`数据库
- `database.name`配置的是需要解析的数据库
- 运行`Table2wordMysqlApplication`，程序会开始执行
- 本程序会解析数据库中所有的表、视图
- 最后会在工程根目录生成以数据库命名的docx文件