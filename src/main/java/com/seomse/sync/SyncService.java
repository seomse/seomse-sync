package com.seomse.sync;

import com.seomse.commons.config.Config;
import com.seomse.commons.service.Service;
import com.seomse.commons.utils.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.util.PendingException;

/**
 * <pre>
 *  파 일 명 : SyncService.java
 *  설    명 : 동기화 서비스
 *
 *
 *  작 성 자 : macle
 *  작 성 일 : 2019.10.25
 *  버    전 : 1.0
 *  수정이력 :
 *  기타사항 :
 * </pre>
 * @author Copyrights 2019 by ㈜섬세한사람들. All right reserved.
 */
public class SyncService extends Service {


    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    private boolean isStop = false;

    //지연된시작
    private boolean isDelayedStart = false;

    /**
     * 생성자
     */
    public SyncService()
    {
        setSleepTime(Config.getLong("sync.service.sleep.time", 3600000L));
        setState(State.START);
    }

    @Override
    public void work() {
        try{
            SynchronizerManager synchronizerManager = SynchronizerManager.getInstance();
            if (Config.getBoolean("sync.service.flag", true)
                    && !synchronizerManager.isIng()) {
                synchronizerManager.sync();
            }
        }catch(Exception e){
            logger.error(ExceptionUtil.getStackTrace(e));
        }
    }



}
