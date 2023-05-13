package com.niuma.binapi.service.impl.inner;

import com.niuma.binapi.model.entity.InterfaceCharging;
import com.niuma.binapi.service.InterfaceChargingService;
import com.niuma.binapicommon.common.ErrorCode;
import com.niuma.binapicommon.exception.BusinessException;
import com.niuma.binapicommon.model.dto.UnLockAvailablePiecesDTO;
import com.niuma.binapicommon.model.dto.UpdateAvailablePiecesDTO;
import com.niuma.binapicommon.service.InnerInterfaceChargingService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @author niuma
 * @create 2023-05-03 16:21
 */
@DubboService
public class InnerInterfaceChargingServiceImpl implements InnerInterfaceChargingService {
    @Resource
    InterfaceChargingService interfaceChargingService;

    @Override
    public boolean checkInventory(Long id) {
        return interfaceChargingService.checkInventory(id);
    }

    @Override
    public boolean updateAvailablePieces(UpdateAvailablePiecesDTO updateAvailablePiecesDTO) {
        Long chargingId = updateAvailablePiecesDTO.getChargingId();
        Long count = updateAvailablePiecesDTO.getCount();
        if (null == chargingId || null == count || count < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        InterfaceCharging interfaceCharging = interfaceChargingService.getById(chargingId);
        if (interfaceCharging == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        String availablePieces = interfaceCharging.getAvailablePieces();
        long b = Long.parseLong(availablePieces) - count;
        if (b < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "库存不足");
        }

        InterfaceCharging updateInterfaceCharging = new InterfaceCharging();
        updateInterfaceCharging.setId(chargingId);
        updateInterfaceCharging.setAvailablePieces(String.valueOf(b));
        boolean update = interfaceChargingService.updateById(updateInterfaceCharging);
        return update;
    }

    @Override
    public boolean unLockAvailablePieces(UnLockAvailablePiecesDTO unLockAvailablePiecesDTO) {
        return interfaceChargingService.unLockAvailablePieces(unLockAvailablePiecesDTO);
    }
}
