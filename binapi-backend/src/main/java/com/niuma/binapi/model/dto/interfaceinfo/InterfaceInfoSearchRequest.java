package com.niuma.binapi.model.dto.interfaceinfo;

import com.niuma.binapicommon.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author niuma
 * @create 2023-05-28 13:57
 */
@Data
public class InterfaceInfoSearchRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = -6270861325558763872L;
    private String searchText;
    private String type;
}
