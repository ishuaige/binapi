package com.niuma.binapi.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


/**
 * @author niuma
 * @create 2023-03-22 20:57
 */
public interface InterfaceInfoDataSource<T> {

    /**
     * 数据源接口，新接入数据源必须实现
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<T> doSearch(String searchText, long pageNum, long pageSize);
}
