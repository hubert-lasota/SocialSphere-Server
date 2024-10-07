package org.hl.socialspherebackend.application.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PageUtils {

    private static final Logger log = LoggerFactory.getLogger(PageUtils.class);

    private PageUtils() {}

    public static <T> Page<T> createPageImpl(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        if(start > list.size()) {
            log.debug("Cannot create page because start({}) is greater than list size({})", start, list.size());
            return Page.empty();
        }
        List<T> subList = list.subList(start, end);
        return new PageImpl<>(subList, pageable, list.size());
    }

}
