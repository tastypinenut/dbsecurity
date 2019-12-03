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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestInsert extends TestBase {
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
    public void testInsert() {
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setName("张三");
        newUserInfo.setBankCardNo("62260000001");
        newUserInfo.setType("USER");
        getSession("mybatis-config.xml").getMapper(UserInfoMapper.class).insert(newUserInfo);

        List<UserInfo> userInfoList = defaultSqlSession.getMapper(UserInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被加密", SecurityUtils.isEncrypted(userInfo.getName()));
            Assert.assertTrue("银行卡号应当被加密", SecurityUtils.isEncrypted(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不应当被加密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }


    @Test
    public void testInsertBatch() {
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setName("张三");
        newUserInfo.setBankCardNo("62260000001");
        newUserInfo.setType("USER");
        getSession("mybatis-config.xml").getMapper(UserInfoMapper.class).insertBatch(Arrays.asList(newUserInfo, newUserInfo));

        List<UserInfo> userInfoList = defaultSqlSession.getMapper(UserInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被加密", SecurityUtils.isEncrypted(userInfo.getName()));
            Assert.assertTrue("银行卡号应当被加密", SecurityUtils.isEncrypted(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不应当被加密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    @Test
    public void testInsertNull() {
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setName("张三");
        newUserInfo.setBankCardNo(null);
        newUserInfo.setType("USER");
        getSession("mybatis-config.xml").getMapper(UserInfoMapper.class).insert(newUserInfo);

        List<UserInfo> userInfoList = defaultSqlSession.getMapper(UserInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被加密", SecurityUtils.isEncrypted(userInfo.getName()));
            Assert.assertNull("银行卡号应当为空", userInfo.getBankCardNo());
            Assert.assertTrue("类型不应当被加密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    @Test
    public void testInsertEmpty() {
        UserInfo newUserInfo = new UserInfo();
        newUserInfo.setName("张三");
        newUserInfo.setBankCardNo("");
        newUserInfo.setType("USER");
        getSession("mybatis-config.xml").getMapper(UserInfoMapper.class).insert(newUserInfo);

        List<UserInfo> userInfoList = defaultSqlSession.getMapper(UserInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被加密", SecurityUtils.isEncrypted(userInfo.getName()));
            Assert.assertEquals("银行卡号应当为空", "", userInfo.getBankCardNo());
            Assert.assertTrue("类型不应当被加密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    @Test
    public void testBatchInsertVerticalTable() {
        ParamInfo newParamInfo = new ParamInfo();
        newParamInfo.setParamName("bankCard");
        newParamInfo.setParamValue("62260000001");

        ParamInfo newParamInfo2 = new ParamInfo();
        newParamInfo2.setParamName("name");
        newParamInfo2.setParamValue("张三");

        List<ParamInfo> newParamInfoList = new ArrayList<>();
        newParamInfoList.add(newParamInfo);
        newParamInfoList.add(newParamInfo2);

        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).insertBatch(newParamInfoList);

        List<ParamInfo> paramInfoList = defaultSqlSession.getMapper(ParamInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            Assert.assertTrue("应当被加密", SecurityUtils.isEncrypted(paramInfo.getParamValue()));
        }
    }

    @Test
    public void testBatchInsertVerticalTable_mixedEncrypt() {
        ParamInfo newParamInfo = new ParamInfo();
        newParamInfo.setParamName("bankCard");
        newParamInfo.setParamValue("62260000001");

        ParamInfo newParamInfo2 = new ParamInfo();
        newParamInfo2.setParamName("test");
        newParamInfo2.setParamValue("13212342222");

        List<ParamInfo> newParamInfoList = new ArrayList<>();
        newParamInfoList.add(newParamInfo);
        newParamInfoList.add(newParamInfo2);

        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).insertBatch(newParamInfoList);

        List<ParamInfo> paramInfoList = defaultSqlSession.getMapper(ParamInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            if (paramInfo.getParamName().equals("bankCard")) {
                Assert.assertTrue("银行卡号应当被加密", SecurityUtils.isEncrypted(paramInfo.getParamValue()));
            } else {
                Assert.assertTrue("其他值不应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
            }
        }
    }

    @Test
    public void testBatchInsertVerticalTable_mixedEncrypt2() {
        ParamInfo newParamInfo = new ParamInfo();
        newParamInfo.setParamName("test");
        newParamInfo.setParamValue("13212341111");

        ParamInfo newParamInfo2 = new ParamInfo();
        newParamInfo2.setParamName("bankCard");
        newParamInfo2.setParamValue("62260000001");

        List<ParamInfo> newParamInfoList = new ArrayList<>();
        newParamInfoList.add(newParamInfo);
        newParamInfoList.add(newParamInfo2);

        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).insertBatch(newParamInfoList);

        List<ParamInfo> paramInfoList = defaultSqlSession.getMapper(ParamInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            if (paramInfo.getParamName().equals("bankCard")) {
                Assert.assertTrue("银行卡号应当被加密", SecurityUtils.isEncrypted(paramInfo.getParamValue()));
            } else {
                Assert.assertTrue("其他值不应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
            }
        }
    }

    @Test
    public void testBatchInsertVerticalTable_mixedEncrypt3() {
        ParamInfo newParamInfo = new ParamInfo();
        newParamInfo.setParamName("bankCard");
        newParamInfo.setParamValue("62260000001");

        ParamInfo newParamInfo2 = new ParamInfo();
        newParamInfo2.setParamName("test");
        newParamInfo2.setParamValue("13212342222");

        ParamInfo newParamInfo3 = new ParamInfo();
        newParamInfo3.setParamName("name");
        newParamInfo3.setParamValue("张三");

        List<ParamInfo> newParamInfoList = new ArrayList<>();
        newParamInfoList.add(newParamInfo);
        newParamInfoList.add(newParamInfo2);
        newParamInfoList.add(newParamInfo3);

        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).insertBatch(newParamInfoList);

        List<ParamInfo> paramInfoList = defaultSqlSession.getMapper(ParamInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            if (paramInfo.getParamName().equals("bankCard") || paramInfo.getParamName().equals("name") ) {
                Assert.assertTrue("银行卡号应当被加密", SecurityUtils.isEncrypted(paramInfo.getParamValue()));
            } else {
                Assert.assertTrue("其他值不应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
            }
        }
    }

    @Test
    public void testBatchInsertVerticalTable_noConditionColumn() {
        ParamInfo newParamInfo = new ParamInfo();
        newParamInfo.setParamValue("13212341111");

        ParamInfo newParamInfo2 = new ParamInfo();
        newParamInfo2.setParamValue("13212342222");

        List<ParamInfo> newParamInfoList = new ArrayList<>();
        newParamInfoList.add(newParamInfo);
        newParamInfoList.add(newParamInfo2);

        getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class).insertBatch(newParamInfoList);

        List<ParamInfo> paramInfoList = defaultSqlSession.getMapper(ParamInfoMapper.class).selectAll();
        Assert.assertTrue("查询结果不为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            Assert.assertTrue("其他值不应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
        }
    }
}
