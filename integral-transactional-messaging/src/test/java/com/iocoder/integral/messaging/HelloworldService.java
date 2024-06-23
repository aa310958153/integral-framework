package com.iocoder.integral.messaging;

import com.iocoder.integral.messaging.dto.StudentReqDTO;
import com.iocoder.integral.messaging.dto.StudentResDTO;

import java.util.List;

public interface HelloworldService {
    List<StudentResDTO> getUserInfo(StudentReqDTO studentReqDTO);

    List<StudentResDTO> getUserInfo2(List<StudentReqDTO> studentReqDTO);

    List<StudentResDTO> getUserInfo3(Integer id);

    List<StudentResDTO> getUserInfo4(StudentReqDTO studentReqDTO, Integer id);

    List<StudentResDTO> getUserInfo5(List<StudentReqDTO> studentReqDTO, Integer id);
}
