package com.seomse.sync;

import com.seomse.commons.config.Config;
import com.seomse.commons.utils.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class SyncService implements Runnable{


    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    private boolean isStop = false;

    private long waitTime;

    //지연된시작
    private boolean isDelayedStart = false;

    /**
     * 생성자
     */
    public SyncService(){
        waitTime = Config.getLong("sync.service.wait.time", 3600000L);
    }

    /**
     * 지연된 시작 설정
     * @param delayedStart 지연된 시작 여부
     */
    public void setDelayedStart(boolean delayedStart) {
        isDelayedStart = delayedStart;
    }

    @Override
    public void run() {

        if(isStop){
            return;
        }
        try{

            //처음에 시작되고 실행되므로 첫시작은 대기후 시작한다
            if(isDelayedStart)
                Thread.sleep(waitTime);

            while (!isStop) {

                SynchronizerManager synchronizerManager = SynchronizerManager.getInstance();
                if (Config.getBoolean("sync.service.flag", true)
                        && !synchronizerManager.isIng()) {
                    synchronizerManager.sync();
                }
                Thread.sleep(waitTime);
            }


        }catch(Exception e){
            logger.error(ExceptionUtil.getStackTrace(e));
        }

    }


    public void stopService(){
        isStop = true;
    }

}
