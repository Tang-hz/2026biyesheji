package com.gk.study;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gk.study.entity.User;
import com.gk.study.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@SpringBootApplication(excludeName = {
        // LangChain4j Spring Boot starter (0.36.0) is not compatible with Spring Boot 3.0.2 (Spring 6.0.4)
        // and can throw AbstractMethodError during startup. We exclude the auto-config and wire AI Services manually.
        "dev.langchain4j.spring.LangChain4jAutoConfig"
})
public class MySpringApplication {

    private static final String SALT = "abcd1234";
    private static final String ADMIN_NEW_USERNAME = "汤弘正";
    private static final String ADMIN_NEW_PASSWORD = "Thz20031001";

    public static void main(String[] args) {
        SpringApplication.run(MySpringApplication.class, args);
    }

    @Bean
    public CommandLineRunner migrateAdminCredentials(UserMapper userMapper) {
        return args -> {
            // Keep admin permission model unchanged; only migrate the default admin credentials.
            QueryWrapper<User> oldAdminQuery = new QueryWrapper<>();
            //oldAdminQuery.eq("username", ADMIN_OLD_USERNAME);
            oldAdminQuery.gt("role", "1");
            User oldAdmin = userMapper.selectOne(oldAdminQuery);
            if (oldAdmin != null) {
                oldAdmin.setUsername(ADMIN_NEW_USERNAME);
                oldAdmin.setPassword(md5(ADMIN_NEW_PASSWORD + SALT));
                oldAdmin.setToken(md5(ADMIN_NEW_USERNAME + SALT));
                oldAdmin.setRole(String.valueOf(User.AdminUser));
                userMapper.updateById(oldAdmin);
                return;
            }

            // If username already migrated, ensure password/token stay aligned with requested admin credentials.
            QueryWrapper<User> newAdminQuery = new QueryWrapper<>();
            newAdminQuery.eq("username", ADMIN_NEW_USERNAME);
            newAdminQuery.gt("role", "1");
            User newAdmin = userMapper.selectOne(newAdminQuery);
            if (newAdmin != null) {
                newAdmin.setPassword(md5(ADMIN_NEW_PASSWORD + SALT));
                newAdmin.setToken(md5(ADMIN_NEW_USERNAME + SALT));
                newAdmin.setRole(String.valueOf(User.AdminUser));
                userMapper.updateById(newAdmin);
            }
        };
    }

    @Bean
    public CommandLineRunner migrateAdSloganColumn(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE b_ad ADD COLUMN slogan varchar(200) NULL AFTER link");
            } catch (Exception ignored) {
                // Column already exists or table unavailable during startup; ignore to keep app boot resilient.
            }
        };
    }

    @Bean
    public CommandLineRunner migrateCartTable(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute(
                        "CREATE TABLE IF NOT EXISTS b_cart ("
                                + "id bigint(20) NOT NULL AUTO_INCREMENT,"
                                + "user_id bigint(20) NOT NULL,"
                                + "thing_id bigint(20) NOT NULL,"
                                + "item_count int(11) NOT NULL DEFAULT 1,"
                                + "PRIMARY KEY (id),"
                                + "UNIQUE KEY uk_cart_user_thing (user_id, thing_id),"
                                + "KEY idx_cart_user (user_id),"
                                + "CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES b_user (id) ON DELETE CASCADE ON UPDATE CASCADE,"
                                + "CONSTRAINT fk_cart_thing FOREIGN KEY (thing_id) REFERENCES b_thing (id) ON DELETE CASCADE ON UPDATE CASCADE"
                                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
            } catch (Exception ignored) {
                // Table exists, FK names clash, or DB unavailable; ignore so the app can still start.
            }
        };
    }

    private String md5(String value) {
        return DigestUtils.md5DigestAsHex(Objects.requireNonNull(value).getBytes(StandardCharsets.UTF_8));
    }

}
