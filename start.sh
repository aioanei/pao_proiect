Start-Process -FilePath "C:\Program Files\Docker\Docker\Docker Desktop.exe"
docker start trading-postgres
mvn clean compile exec:java