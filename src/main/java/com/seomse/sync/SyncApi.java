package com.seomse.sync;

import com.seomse.api.ApiMessage;
import com.seomse.api.Messages;
import com.seomse.commons.utils.ExceptionUtil;

/**
 * <pre>
 *  파 일 명 : SyncService.java
 *  설    명 : 동기화 서비스
 *
 *
 *  작 성 자 : macle
 *  작 성 일 : 2019.10.23
 *  버    전 : 1.0
 *  수정이력 :
 *  기타사항 :
 * </pre>
 * @author Copyrights 2019 by ㈜섬세한사람들. All right reserved.
 */
public class SyncApi extends ApiMessage {


    @Override
    public void receive(String message) {
        try{
            SynchronizerManager synchronizerManager = SynchronizerManager.getInstance();
            if(!synchronizerManager.isIng()){
                synchronizerManager.sync();
            }
            communication.sendMessage(Messages.SUCCESS);
        }catch(Exception e){
            communication.sendMessage(Messages.FAIL + ExceptionUtil.getStackTrace(e));
        }
    }
}
