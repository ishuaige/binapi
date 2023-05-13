package com.niuma.binapi.service;

import com.niuma.binapi.model.entity.InterfaceCharging;
import com.baomidou.mybatisplus.extension.service.IService;
import com.niuma.binapicommon.model.dto.UnLockAvailablePiecesDTO;

/**
* @author niumazlb
* @description 针对表【interface_charging】的数据库操作Service
* @createDate 2023-04-30 20:44:10
*/
public interface InterfaceChargingService extends IService<InterfaceCharging> {

    /**
     * 检查某个接口的库存是否充足
     * @param id
     * @return
     */
    boolean checkInventory(Long id);

    /**
     * 解锁库存
     * @param unLockAvailablePiecesDTO
     * @return
     */
    boolean unLockAvailablePieces(UnLockAvailablePiecesDTO unLockAvailablePiecesDTO);
}
