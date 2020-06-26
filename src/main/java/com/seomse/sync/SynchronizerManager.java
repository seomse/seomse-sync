package com.seomse.sync;

import com.seomse.commons.config.Config;
import com.seomse.commons.handler.ExceptionHandler;
import com.seomse.commons.utils.ExceptionUtil;
import com.seomse.commons.utils.PriorityUtil;
import com.seomse.commons.utils.date.RunningTime;
import com.seomse.commons.utils.date.TimeUtil;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <pre>
 *  파 일 명 : SynchronizerManager.java
 *  설    명 : 동기화 관리자
 *
 *  작 성 자 : macle
 *  작 성 일 : 2019.10.25
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


    private final Set<Synchronizer> syncSet = new HashSet<>();

    private Synchronizer[] syncArray ;

    private ExceptionHandler exceptionHandler = null;

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    private boolean isIng = false;

    private long lastSyncTime = System.currentTimeMillis();

    /**
     * 생성자
     */
    private SynchronizerManager(){

        try{

            String syncPackagesValue = Config.getConfig("sync.package", "com.seomse");
            String [] syncPackages = syncPackagesValue.split(",");
            for(String syncPackage : syncPackages) {
                Reflections ref = new Reflections(new ConfigurationBuilder()
                        .setScanners(new SubTypesScanner())
                        .setUrls(ClasspathHelper.forClassLoader())
                        .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(syncPackage))));
                for (Class<?> cl : ref.getSubTypesOf(Synchronizer.class)) {
                    try {

                        if (cl.isAnnotationPresent(Synchronization.class)) {
                            Synchronizer sync = (Synchronizer) cl.newInstance();
                            syncSet.add(sync);
                        }

                    } catch (Exception e) {
                        logger.error(ExceptionUtil.getStackTrace(e));
                    }
                }
            }

            if (syncSet.size() == 0) {
                this.syncArray = new Synchronizer[0];
                return;
            }

            changeArray();
        }catch(Exception e){
            logger.error(ExceptionUtil.getStackTrace(e));
        }
    }

    private void changeArray(){

        Synchronizer[] SyncArray = syncSet.toArray( new Synchronizer[0]);

        Arrays.sort(SyncArray, PriorityUtil.PRIORITY_SORT);

        this.syncArray = SyncArray;
    }

    private final Object lock = new Object();

    /**
     * 동기화  객체 추가
     * @param synchronizer 동기화 객체
     */
    public void add(Synchronizer synchronizer){
        synchronized (lock){
            if(syncSet.contains(synchronizer)){
                return;
            }
            syncSet.add(synchronizer);
            changeArray();
        }
    }



    /**
     * 동기화 객체 제거
     * @param synchronizer 동기화 객체
     */
    public void remove(Synchronizer synchronizer){
        synchronized (lock){
            if(!syncSet.contains(synchronizer)){
                return;
            }
            syncSet.remove(synchronizer);

            if(syncSet.size() == 0){
                this.syncArray = new Synchronizer[0];
            }else{
                changeArray();
            }
        }
    }


    private final Object syncLock = new Object();

    /**
     * 초기에 처음 실행될 이벤트 정의
     */
    public void sync(){
        synchronized (syncLock) {
            isIng = true;
            lastSyncTime = System.currentTimeMillis();
            RunningTime runningTime = new RunningTime();

            Synchronizer[] syncArray = this.syncArray;

            //순서정보를 명확하게 하기위해 i 사용 ( 순서가 꼭 지켜져야 함을 명시)
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < syncArray.length; i++) {
                try {
                    Synchronizer sync = syncArray[i];
                    logger.debug("sync : " + sync.getClass().getName());
                    sync.sync();

                    logger.debug(TimeUtil.getTimeValue(runningTime.getRunningTime()));

                } catch (Exception e) {
                    ExceptionUtil.exception(e, logger, exceptionHandler);
                }
            }
            isIng = false;
        }
    }

    /**
     * @return 동기화중인지 여부
     */
    public boolean isIng() {
        return isIng;
    }

    /**
     * @return 마지막 동기화 시간
     */
    public long getLastSyncTime() {
        return lastSyncTime;
    }
}
