package com.iocoder.integral.messaging;

import com.iocoder.integral.messaging.dto.StudentReqDTO;
import com.iocoder.integral.messaging.dto.StudentResDTO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class HelloWordTestImpl implements HelloworldService {

    @Override
    public List<StudentResDTO> getUserInfo(StudentReqDTO studentReqDTO) {
        return Arrays.asList(new StudentResDTO("小明", "22"));
    }

    @Override
    public List<StudentResDTO> getUserInfo2(List<StudentReqDTO> studentReqDTO) {
        return Arrays.asList(new StudentResDTO("小明", "2"));
    }

    @Override
    public List<StudentResDTO> getUserInfo3(Integer id) {
        return Arrays.asList(new StudentResDTO("小明", id.toString()));
    }

    @Override
    public List<StudentResDTO> getUserInfo4(StudentReqDTO studentReqDTO, Integer id) {
        return Arrays.asList(new StudentResDTO("小明4", id.toString()));
    }

    @Override
    public List<StudentResDTO> getUserInfo5(List<StudentReqDTO> studentReqDTO, Integer id) {
        return Arrays.asList(new StudentResDTO("小明4", id.toString()));
    }
}
