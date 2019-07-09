package com.sunnyday.idea.config;

import com.sunnyday.idea.analyticsquery.AreasTotalPerHour;
import com.sunnyday.idea.mymodel.ConfigParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wangxinxin
 */
@Slf4j
@Component
public class InitConfig implements CommandLineRunner {

    @Autowired
    private AreasTotalPerHour atp;
    @Autowired
    private ConfigParams configParams;

    @Override
    public void run(String... args) throws Exception {
        atp.calculate(configParams.getSrcPath());
    }
}
