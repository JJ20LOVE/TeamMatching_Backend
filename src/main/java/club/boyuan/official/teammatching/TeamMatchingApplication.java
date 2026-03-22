package club.boyuan.official.teammatching;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("club.boyuan.official.teammatching.mapper")
@EnableCaching
public class TeamMatchingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamMatchingApplication.class, args);
    }

}
