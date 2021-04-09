package com.atg.atgtools.sched;

import com.atg.atgtools.services.MockCheckService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MockStateCheckScheduler {
    @Autowired
    MockCheckService mockCheckService;

    private static final String ATG_ENV_CONFIG_FILE="config/tier_envs.json";

    public void mockCheckSched(){



    }
}
