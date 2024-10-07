package org.hl.socialspherebackend.infrastructure.init_dev_data;

import org.springframework.beans.factory.InitializingBean;


class InitData implements InitializingBean {
    private final UserInitData userInitData;
    private final PostInitData postInitData;
    private final ChatInitData chatInitData;

    InitData(UserInitData userInitData, PostInitData postInitData, ChatInitData chatInitData) {
        this.userInitData = userInitData;
        this.postInitData = postInitData;
        this.chatInitData = chatInitData;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        userInitData.initData();
        postInitData.initData();
        chatInitData.initData();
    }

}
