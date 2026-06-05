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
----
```bash
sdk install java 17.0.11-tem
sdk use java 17.0.11-tem
sdk default java 17.0.11-tem
java -version / javac -version
```
----
```bash
apt update && apt install openjdk-17-jdk -y
update-alternatives --config java
update-alternatives --config javac
java -version
javac -version
```



```bash
chmod +x mvnw
cd admin-management-system/backend
./mvnw -DskipTests package
java -jar target/*.jar --server.port=8081
```