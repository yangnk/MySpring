package com.yangnk.framework.dataSource;

public class DynamicDataSourceEntry {
    //默认数据源
    public final static String DEFAULT_SOURCE = null;
    private final static ThreadLocal<String> local = new ThreadLocal<String>();

    /**
     * 获取当前数据源名称
     * @return
     */
    public String get() {
        return local.get();
    }

    /**
     * 设置数据源名称
     * @param source
     */
    public void set(String source) {
        local.set(source);
    }

    /**
     * 动态设置数据源名称
     * @param year
     */
    public void set(int year) {
        local.set("DB_" + year);
    }
}
