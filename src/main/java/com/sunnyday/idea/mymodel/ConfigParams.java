package com.sunnyday.idea.mymodel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author wangxinxin
 * @date 2019/7/9
 */
@Component
public class ConfigParams {

    @Value("${myconfig.srcPath}")
    private String srcPath;

    public String getSrcPath() {
        return srcPath;
    }
    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    @Override
    public String toString() {
        return "ConfigParams{" +
                ", srcPath='" + srcPath + '\'' +
                '}';
    }
}
