package org.zap.framework.common.config;

import org.apache.commons.lang.BooleanUtils;

import java.util.Map;

/**
 * Created by Shin on 2017/12/4.
 */
public class DeployConfig {

    /**
     * 虚拟目录路径
     */
    private String sys_upload_path;
    /**
     * 启用GPS
     */
    private boolean gps_enable;

    private DeployConfig() {
    }

    private DeployConfig(String sys_upload_path, boolean gps_enable) {
        this.sys_upload_path = sys_upload_path;
        this.gps_enable = gps_enable;
    }

    private static DeployConfig instance;

    public static DeployConfig getInstance(Map<String, String> dicts) {
        if (instance == null) {
            instance = new DeployConfig(
                    dicts.get("sys_upload_path"),
                    BooleanUtils.toBoolean(dicts.get("gps_enable"))
            );
        }
        return instance;
    }

    public String getSys_upload_path() {
        return sys_upload_path;
    }

    public boolean isGps_enable() {
        return gps_enable;
    }
}
