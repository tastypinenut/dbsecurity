package xyz.tpn.dbsecurity;

import xyz.tpn.dbsecurity.mapper.ParamInfoMapper;
import xyz.tpn.dbsecurity.mapper.UserInfoMapper;
import xyz.tpn.dbsecurity.model.ParamInfo;
import xyz.tpn.dbsecurity.model.UserInfo;
import xyz.tpn.dbsecurity.utils.SecurityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TestUpdate extends TestBase {
    @Before
    public void before() {
        super.before();
    }

    @After
    public void after() {
        defaultSqlSession.getMapper(UserInfoMapper.class).deleteAll();
        defaultSqlSession.getMapper(ParamInfoMapper.class).deleteAll();
    }

    @Test
    public void testUpdate() {
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setId(1L);
        newUserInfo.setName("张三");
        newUserInfo.setBankCardNo("62260000001");
        newUserInfo.setType("USER");
        getSession("mybatis-config.xml").getMapper(UserInfoMapper.class).insert(newUserInfo);

        newUserInfo.setName("张三2");
        newUserInfo.setBankCardNo("62260000002");
        getSession("mybatis-config.xml").getMapper(UserInfoMapper.class).updateById(newUserInfo);

        List<UserInfo> userInfoList = defaultSqlSession.getMapper(UserInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当已加密", SecurityUtils.isEncrypted(userInfo.getName()));
            Assert.assertEquals("姓名应当已被修改", "张三2", SecurityUtils.decrypt(userInfo.getName()));
            Assert.assertTrue("银行卡号应当已加密", SecurityUtils.isEncrypted(userInfo.getBankCardNo()));
            Assert.assertEquals("银行卡号应当已被修改", "62260000002", SecurityUtils.decrypt(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    @Test
    public void testUpdate2() {
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setId(1L);
        newUserInfo.setName("张三");
        newUserInfo.setBankCardNo("62260000001");
        newUserInfo.setType("USER");
        getSession("mybatis-config.xml").getMapper(UserInfoMapper.class).insert(newUserInfo);

        newUserInfo.setName("张三");
        newUserInfo.setBankCardNo("62260000002");
        newUserInfo.setType("USER");
        getSession("mybatis-config.xml").getMapper(UserInfoMapper.class).updateById2(newUserInfo);

        List<UserInfo> userInfoList = defaultSqlSession.getMapper(UserInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当已加密", SecurityUtils.isEncrypted(userInfo.getName()));
            Assert.assertEquals("姓名应当没被修改", "张三", SecurityUtils.decrypt(userInfo.getName()));
            Assert.assertTrue("银行卡号应当已加密", SecurityUtils.isEncrypted(userInfo.getBankCardNo()));
            Assert.assertEquals("银行卡号应当已被修改", "62260000002", SecurityUtils.decrypt(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    @Test
    public void testUpdate_updateByName() {
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setId(1L);
        newUserInfo.setName("张三");
        newUserInfo.setBankCardNo("62260000001");
        newUserInfo.setType("USER");
        getSession("mybatis-config.xml").getMapper(UserInfoMapper.class).insert(newUserInfo);

        newUserInfo.setName("张三");
        newUserInfo.setBankCardNo("62260000002");
        getSession("mybatis-config.xml").getMapper(UserInfoMapper.class).updateByName(newUserInfo);

        List<UserInfo> userInfoList = defaultSqlSession.getMapper(UserInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当已加密", SecurityUtils.isEncrypted(userInfo.getName()));
            Assert.assertTrue("银行卡号应当已加密", SecurityUtils.isEncrypted(userInfo.getBankCardNo()));
            Assert.assertEquals("银行卡号应当已被修改", "62260000002", SecurityUtils.decrypt(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }

        newUserInfo.setName("张三2");
        newUserInfo.setBankCardNo("62260000002");
        getSession("mybatis-config.xml").getMapper(UserInfoMapper.class).updateUserInfoByName(newUserInfo, "张三");

        UserInfo userInfo = defaultSqlSession.getMapper(UserInfoMapper.class).selectById(1L);//selectAll();
        Assert.assertTrue("查询结果不为空", userInfo != null);
        Assert.assertTrue("姓名应当已加密", SecurityUtils.isEncrypted(userInfo.getName()));
        Assert.assertEquals("姓名应当已被修改", "张三2", SecurityUtils.decrypt(userInfo.getName()));
        Assert.assertTrue("银行卡号应当已加密", SecurityUtils.isEncrypted(userInfo.getBankCardNo()));
        Assert.assertEquals("银行卡号应当已被修改", "62260000002", SecurityUtils.decrypt(userInfo.getBankCardNo()));
        Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
    }

    @Test
    public void testUpdateNull() {
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setId(1L);
        newUserInfo.setName("张三");
        newUserInfo.setBankCardNo("62260000001");
        newUserInfo.setType("USER");
        getSession("mybatis-config.xml").getMapper(UserInfoMapper.class).insert(newUserInfo);

        newUserInfo.setName("张三2");
        newUserInfo.setBankCardNo(null);
        getSession("mybatis-config.xml").getMapper(UserInfoMapper.class).updateById(newUserInfo);

        List<UserInfo> userInfoList = defaultSqlSession.getMapper(UserInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当已加密", SecurityUtils.isEncrypted(userInfo.getName()));
            Assert.assertEquals("姓名应当已被修改", "张三2", SecurityUtils.decrypt(userInfo.getName()));
            Assert.assertNull("银行卡号应当已被修改", userInfo.getBankCardNo());
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    @Test
    public void testUpdateVerticalTableEncrypted() {
        ParamInfo newParamInfo = new ParamInfo();
        newParamInfo.setId(1L);
        newParamInfo.setParamName("test");
        newParamInfo.setParamValue("13212345678");
        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).insert(newParamInfo);

        newParamInfo.setParamName("bankCard");
        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).updateById(newParamInfo);

        List<ParamInfo> paramInfoList = defaultSqlSession.getMapper(ParamInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            Assert.assertTrue("手机号应当被加密", SecurityUtils.isEncrypted(paramInfo.getParamValue()));
        }
    }

    @Test
    public void testUpdateVerticalTableEncrypted_byParamName() {
        ParamInfo newParamInfo = new ParamInfo();
        newParamInfo.setId(1L);
        newParamInfo.setParamName("name");
        newParamInfo.setParamValue("13212345678");
        defaultSqlSession.getMapper(ParamInfoMapper.class).insert(newParamInfo);

        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).updateByParamName(newParamInfo);

        List<ParamInfo> paramInfoList = defaultSqlSession.getMapper(ParamInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            Assert.assertTrue("手机号应当被加密", SecurityUtils.isEncrypted(paramInfo.getParamValue()));
        }
    }

    @Test
    public void testUpdateVerticalTableEncrypted_byParamName_fixed1() {
        ParamInfo newParamInfo = new ParamInfo();
        newParamInfo.setId(1L);
        newParamInfo.setParamName("name");
        newParamInfo.setParamValue("13212345678");
        newParamInfo.setType("encrypt");
        defaultSqlSession.getMapper(ParamInfoMapper.class).insert(newParamInfo);

        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).updateByFixedParamName(newParamInfo);

        List<ParamInfo> paramInfoList = defaultSqlSession.getMapper(ParamInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            Assert.assertTrue("手机号应当被加密", SecurityUtils.isEncrypted(paramInfo.getParamValue()));
        }
    }

    @Test
    public void testUpdateVerticalTableEncrypted_setParamName_fixed1() {
        ParamInfo newParamInfo = new ParamInfo();
        newParamInfo.setId(1L);
        newParamInfo.setParamName("name");
        newParamInfo.setParamValue("13212345678");
        newParamInfo.setType("encrypt");
        defaultSqlSession.getMapper(ParamInfoMapper.class).insert(newParamInfo);

        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).updateSetFixedParamNameById(newParamInfo);

        List<ParamInfo> paramInfoList = defaultSqlSession.getMapper(ParamInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            Assert.assertTrue("手机号应当被加密", SecurityUtils.isEncrypted(paramInfo.getParamValue()));
        }
    }

    @Test
    public void testUpdateVerticalTableEncrypted_byParamValue() {
        ParamInfo newParamInfo = new ParamInfo();
        newParamInfo.setId(1L);
        newParamInfo.setParamName("bankCard");
        newParamInfo.setParamValue("13212345678");
        newParamInfo.setType("encrypt");
        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).insert(newParamInfo);

        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).updateByParamValue(newParamInfo);

        List<ParamInfo> paramInfoList = defaultSqlSession.getMapper(ParamInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            Assert.assertTrue("手机号应当被加密", SecurityUtils.isEncrypted(paramInfo.getParamValue()));
            Assert.assertTrue("paramName不应当被修改", "bankCard".equals(paramInfo.getParamName()));
        }
    }


    @Test
    public void testUpdateVerticalTableEncrypted_byParamValueAndParamName() {
        ParamInfo newParamInfo = new ParamInfo();
        newParamInfo.setId(1L);
        newParamInfo.setParamName("bankCard");
        newParamInfo.setParamValue("13212345678");
        newParamInfo.setType("encrypt");
        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).insert(newParamInfo);

        newParamInfo.setType("test");
        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).updateByParamValueAndParamName(newParamInfo);

        List<ParamInfo> paramInfoList = defaultSqlSession.getMapper(ParamInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            Assert.assertTrue("手机号应当被加密", SecurityUtils.isEncrypted(paramInfo.getParamValue()));
            Assert.assertEquals("type应当被修改", "test", paramInfo.getType());

        }
    }


    @Test
    public void testUpdateVerticalTableEncrypted_noConditionColumn() {
        ParamInfo newParamInfo = new ParamInfo();
        newParamInfo.setId(1L);
        newParamInfo.setParamName("bankCard");
        newParamInfo.setParamValue("13212345678");
        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).insert(newParamInfo);

        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).updateByIdNoParamName(newParamInfo);

        List<ParamInfo> paramInfoList = defaultSqlSession.getMapper(ParamInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            Assert.assertTrue("手机号不应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
        }
    }

    @Test
    public void testUpdateVerticalTableUnEncrypted() {
        ParamInfo newParamInfo = new ParamInfo();
        newParamInfo.setId(1L);
        newParamInfo.setParamName("bankCard");
        newParamInfo.setParamValue("13212345678");
        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).insert(newParamInfo);

        newParamInfo.setParamName("test");
        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).updateById(newParamInfo);

        List<ParamInfo> paramInfoList = defaultSqlSession.getMapper(ParamInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            Assert.assertTrue("手机号不应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
        }
    }
}
