# admin-management-system
Provides administrative functions including content management, user management, system configuration, and data maintenance.
技术栈：springboot
vue


## quick-deploy
```bash
sudo mysql -uroot -p
```

```sql
CREATE DATABASE IF NOT EXISTS knowledge_graph_db
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'se_admin'@'127.0.0.1' IDENTIFIED BY '一个强密码';
GRANT ALL PRIVILEGES ON knowledge_graph_db.* TO 'se_admin'@'127.0.0.1';
FLUSH PRIVILEGES;
EXIT;
```

```sql
mysql -use_admin -p xxxx < ./init.sql
```

- modify `application.yml`

```bash
cd admin-management-system/backend
./mvnw -DskipTests package
java -jar target/*.jar
```