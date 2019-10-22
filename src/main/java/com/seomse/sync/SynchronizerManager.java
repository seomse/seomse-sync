package com.seomse.sync;

import com.seomse.commons.config.Config;
import com.seomse.commons.handler.ExceptionHandler;
import com.seomse.commons.utils.ExceptionUtil;
import com.seomse.commons.utils.PriorityUtil;
import com.seomse.commons.utils.date.RunningTime;
import com.seomse.commons.utils.date.TimeUtil;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * <pre>
 *  파 일 명 : SynchronizerManager.java
 *  설    명 : 동기화 관리자
 *
 *  작 성 자 : macle
 *  작 성 일 : 2019.10.23
 *  버    전 : 1.0
 *  수정이력 :
 *  기타사항 :
 * </pre>
 * @author Copyrights 2019 by ㈜섬세한사람들. All right reserved.
 */
public class SynchronizerManager {

    private static final Logger logger = LoggerFactory.getLogger(SynchronizerManager.class);


    private static class Singleton{
        private static final SynchronizerManager instance = new SynchronizerManager();
    }

    /**
     * 인스턴스 얻기
     * @return Singleton instance
     */
    public static SynchronizerManager getInstance(){
        return Singleton.instance;
    }

    private Synchronizer[] syncArray;

    private ExceptionHandler exceptionHandler = null;

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * 생성자
     */
    private SynchronizerManager(){

        try{

            String syncPackage = Config.getConfig("sync.package", "com.seomse");

            Reflections ref = new Reflections (syncPackage);
            List<Synchronizer> syncList = new ArrayList<>();
            for (Class<?> cl : ref.getSubTypesOf(Synchronizer.class)) {
                try{
                    //noinspection deprecation
                    Synchronizer sync = (Synchronizer)cl.newInstance();
                    syncList.add(sync);

                }catch(Exception e){logger.error(ExceptionUtil.getStackTrace(e));}
            }

            if(syncList.size() == 0){
                this.syncArray = new Synchronizer[0];
                return;
            }
            Comparator<Synchronizer> syncSort = new Comparator<Synchronizer>() {
                @Override
                public int compare(Synchronizer i1, Synchronizer i2 ) {
                    int seq1 = PriorityUtil.getSeq(i1.getClass());
                    int seq2 = PriorityUtil.getSeq(i2.getClass());
                    return Integer.compare(seq1, seq2);
                }
            };

            Synchronizer[] SyncArray = syncList.toArray( new Synchronizer[0]);

            Arrays.sort(SyncArray, syncSort);

            this.syncArray = SyncArray;

        }catch(Exception e){
            logger.error(ExceptionUtil.getStackTrace(e));
        }

    }

    /**
     * 초기에 처음 실행될 이벤트 정의
     */
    public void sync(){
        RunningTime runningTime = new RunningTime();

        //순서정보를 명확하게 하기위해 i 사용 ( 순서가 꼭 지켜져야 함을 명시)
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < syncArray.length ; i++) {
            try {
                Synchronizer sync = syncArray[i];
                logger.debug("sync : " + sync.getClass().getName());
                sync.sync();

                logger.debug(TimeUtil.getTimeValue(runningTime.getRunningTime()));

            }catch(Exception e){
                ExceptionUtil.exception(e,logger,exceptionHandler);
            }
        }
    }

}
