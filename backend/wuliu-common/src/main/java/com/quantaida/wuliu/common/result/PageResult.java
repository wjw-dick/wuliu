package com.quantaida.wuliu.common.result;

import lombok.Data;

import java.util.List;

/** 分页返回结构：list / total / page / size。 */
@Data
public class PageResult<T> {
    private List<T> list;
    private long total;
    private long page;
    private long size;

    public static <T> PageResult<T> of(List<T> list, long total, long page, long size) {
        PageResult<T> p = new PageResult<>();
        p.list = list;
        p.total = total;
        p.page = page;
        p.size = size;
        return p;
    }
}
