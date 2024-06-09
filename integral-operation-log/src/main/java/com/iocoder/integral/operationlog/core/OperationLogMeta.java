/*
 *   Copyright (c) 2022 Oray Inc. All rights reserverd.
 *
 *   No Part of this file may be reproduced, stored
 *   in a retrieval system, or transmitted, in any form, or by any means,
 *   electronic, mechanical, photocopying, recording, or otherwise,
 *   without the prior consent of Oray Inc.
 *
 *
 *   @author qiang.li
 *
 */

package com.iocoder.integral.operationlog.core;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liqiang
 * @date 2023/2/27
 */
@Data
public class OperationLogMeta implements Serializable {

    private static final long serialVersionUID = 1786179466424586522L;

    String operationType;

    String description;

    String targetType;

    String targetId;

    String afterValue;

    String beforeValue;

    String ip;

    Integer userId;
}
