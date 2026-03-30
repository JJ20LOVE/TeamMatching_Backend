package club.boyuan.official.teammatching;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("club.boyuan.official.teammatching.mapper")
@EnableCaching
@EnableScheduling
public class TeamMatchingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamMatchingApplication.class, args);
    }

}
