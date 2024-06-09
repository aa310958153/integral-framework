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

import java.util.List;

/**
 * @author liqiang
 * @date 2023/2/27
 */
public interface OperationLogProcessHandle {
    void process(List<OperationLogMeta> meta);
}
