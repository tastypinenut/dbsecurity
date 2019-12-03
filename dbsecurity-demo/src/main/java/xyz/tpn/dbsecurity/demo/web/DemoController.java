package xyz.tpn.dbsecurity.demo.web;

import xyz.tpn.dbsecurity.demo.dal.mapper.UserInfoMapper;
import xyz.tpn.dbsecurity.demo.dal.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class DemoController {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @RequestMapping("/insert")
    public String insertUser(String name, String bankCardNo, String type) {
        UserInfo userInfo = new UserInfo();
        userInfo.setName(name);
        userInfo.setBankCardNo(bankCardNo);
        userInfo.setType(type);
        userInfoMapper.insert(userInfo);
        return "success";
    }

    @RequestMapping("/insertBatch")
    public String insertUserBatch(String name, String bankCardNo, String type, int count) {

        List<UserInfo> userInfoList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UserInfo userInfo = new UserInfo();
            userInfo.setName(name);
            userInfo.setBankCardNo(bankCardNo);
            userInfo.setType(type);
            userInfoList.add(userInfo);
        }
        userInfoMapper.insertBatch(userInfoList);
        return "success";
    }


    @RequestMapping("/selectOne")
    public Object selectOne(Integer id) {
        UserInfo userInfo = userInfoMapper.selectById(id);
        return userInfo;
    }

    @RequestMapping("/update")
    public Object update(Long id) {
        UserInfo userInfo = new UserInfo();
        userInfo.setBankCardNo("abcd");
        userInfo.setName("tttt");
        userInfo.setId(id);
        userInfoMapper.updateById(userInfo);
        return userInfo;
    }

    @RequestMapping("/selectAll")
    public Object selectAll() {
        List<UserInfo> userInfoList = userInfoMapper.selectAll();
        return userInfoList;
    }

    @RequestMapping("/joinSelectAll")
    public Object joinSelectAll() {
        return userInfoMapper.joinSelectAll();
    }

    @RequestMapping("/selectByName")
    public Object selectByName(String name) {
        return userInfoMapper.selectByName(name);
    }

    @RequestMapping("/selectByType")
    public Object selectByType(String type) {
        return userInfoMapper.selectByType(type);
    }
}
