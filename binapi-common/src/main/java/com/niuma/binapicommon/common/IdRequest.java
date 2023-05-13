package com.niuma.binapicommon.common;

import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author niuma
 */
@Data
public class IdRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}