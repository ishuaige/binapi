package com.niuma.binapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.niuma.binapi.model.entity.InterfaceCharging;
import com.niuma.binapi.service.InterfaceChargingService;
import com.niuma.binapi.mapper.InterfaceChargingMapper;
import com.niuma.binapicommon.common.ErrorCode;
import com.niuma.binapicommon.exception.BusinessException;
import com.niuma.binapicommon.model.dto.UnLockAvailablePiecesDTO;
import org.springframework.stereotype.Service;

/**
* @author niumazlb
* @description 针对表【interface_charging】的数据库操作Service实现
* @createDate 2023-04-30 20:44:10
*/
@Service
public class InterfaceChargingServiceImpl extends ServiceImpl<InterfaceChargingMapper, InterfaceCharging>
    implements InterfaceChargingService{

    @Override
    public boolean checkInventory(Long id) {
        if(id == null){
         throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceCharging interfaceCharging = this.getById(id);
        return Integer.parseInt(interfaceCharging.getAvailablePieces())>= 0;
    }

    @Override
    public boolean unLockAvailablePieces(UnLockAvailablePiecesDTO unLockAvailablePiecesDTO) {
        Long interfaceId = unLockAvailablePiecesDTO.getInterfaceId();
        Long count = unLockAvailablePiecesDTO.getCount();
        UpdateWrapper<InterfaceCharging> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceId", interfaceId).setSql("availablePieces = availablePieces + " + count);
        boolean update = this.update(updateWrapper);
        if(!update){
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return update;
    }
}




