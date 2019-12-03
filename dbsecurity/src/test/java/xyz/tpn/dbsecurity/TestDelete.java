package xyz.tpn.dbsecurity;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import xyz.tpn.dbsecurity.mapper.ParamInfoMapper;
import xyz.tpn.dbsecurity.mapper.UserInfoMapper;
import xyz.tpn.dbsecurity.model.ParamInfo;
import xyz.tpn.dbsecurity.model.UserInfo;
import xyz.tpn.dbsecurity.utils.SecurityUtils;

import java.util.List;

@Slf4j
public class TestDelete extends TestBase {

    @Before
    public void before() {
        super.before();

        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setName(SecurityUtils.encrypt("张三"));
        userInfo.setBankCardNo(SecurityUtils.encrypt("62260000001"));
        userInfo.setType("USER");
        defaultSqlSession.getMapper(UserInfoMapper.class).insert(userInfo);

        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setId(1L);
        paramInfo.setParamName("phone");
        paramInfo.setParamValue(SecurityUtils.encrypt("13212345678"));
        defaultSqlSession.getMapper(ParamInfoMapper.class).insert(paramInfo);
    }

    @After
    public void after() {
        defaultSqlSession.getMapper(UserInfoMapper.class).deleteAll();
        defaultSqlSession.getMapper(ParamInfoMapper.class).deleteAll();
    }

    // 单表删除
    @Test
    public void testDelete_deleteByName() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        fileMapper.deleteByName("张三");

        List<UserInfo> userInfoList = fileMapper.selectAll();
        Assert.assertTrue("查询结果应该为空", userInfoList.isEmpty());
    }

    // 单表删除
    @Test
    public void testDelete_deleteByInName() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        fileMapper.deleteByInName("李四", "张三");

        List<UserInfo> userInfoList = fileMapper.selectAll();
        Assert.assertTrue("查询结果应该为空", userInfoList.isEmpty());
    }
}
