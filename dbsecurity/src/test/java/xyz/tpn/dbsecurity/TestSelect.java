package xyz.tpn.dbsecurity;

import xyz.tpn.dbsecurity.mapper.ParamInfoMapper;
import xyz.tpn.dbsecurity.mapper.UserInfoMapper;
import xyz.tpn.dbsecurity.model.ParamInfo;
import xyz.tpn.dbsecurity.model.UserInfo;
import xyz.tpn.dbsecurity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

@Slf4j
public class TestSelect extends TestBase {

    @Before
    public void before() {
        super.before();

        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setName(SecurityUtils.encrypt("张三"));
        userInfo.setBankCardNo(SecurityUtils.encrypt("62260000001"));
        userInfo.setType("USER");
        defaultSqlSession.getMapper(UserInfoMapper.class).insert(userInfo);

        UserInfo userInfo2 = new UserInfo();
        userInfo2.setId(2L);
        userInfo2.setName(SecurityUtils.encrypt("李四"));
        userInfo2.setBankCardNo(SecurityUtils.encrypt("62260000002"));
        userInfo2.setType("USER");
        defaultSqlSession.getMapper(UserInfoMapper.class).insert(userInfo2);

        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setId(1L);
        paramInfo.setParamName("name");
        paramInfo.setType("encrypt");
        paramInfo.setParamValue(SecurityUtils.encrypt("13212345678"));
        defaultSqlSession.getMapper(ParamInfoMapper.class).insert(paramInfo);


        ParamInfo paramInfo2 = new ParamInfo();
        paramInfo2.setId(2L);
        paramInfo2.setParamName("bankCard");
        paramInfo2.setType("encrypt");
        paramInfo2.setParamValue(SecurityUtils.encrypt("62260000001"));
        defaultSqlSession.getMapper(ParamInfoMapper.class).insert(paramInfo2);

        ParamInfo paramInfo3 = new ParamInfo();
        paramInfo3.setId(3L);
        paramInfo3.setParamName("test");
        paramInfo3.setType("test");
        paramInfo3.setParamValue("13212343333");
        defaultSqlSession.getMapper(ParamInfoMapper.class).insert(paramInfo3);
    }

    @After
    public void after() {
        defaultSqlSession.getMapper(UserInfoMapper.class).deleteAll();
        defaultSqlSession.getMapper(ParamInfoMapper.class).deleteAll();
    }

    // 单表查询
    @Test
    public void testDecrypt() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        List<UserInfo> userInfoList = fileMapper.selectAll();
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被解密", SecurityUtils.isDecrypted(userInfo.getName()));
            Assert.assertTrue("银行卡号应当被解密", SecurityUtils.isDecrypted(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    // 联表查询
    @Test
    public void testDecrypt_joinSelect() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        List<UserInfo> userInfoList = fileMapper.joinSelectAll();
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被解密", SecurityUtils.isDecrypted(userInfo.getName()));
            Assert.assertTrue("银行卡号应当被解密", SecurityUtils.isDecrypted(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    // 联表查询，其中SQL中带有库名
    @Test
    public void testDecrypt_joinSelectAllWithDatabaseName() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        List<UserInfo> userInfoList = fileMapper.joinSelectAllWithDatabaseName();
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被解密", SecurityUtils.isDecrypted(userInfo.getName()));
            Assert.assertTrue("银行卡号应当被解密", SecurityUtils.isDecrypted(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    // 带加密参数的查询
    @Test
    public void testDecrypt_selectByName() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        List<UserInfo> userInfoList = fileMapper.selectByName("张三");
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被解密", SecurityUtils.isDecrypted(userInfo.getName()));
            Assert.assertTrue("银行卡号应当被解密", SecurityUtils.isDecrypted(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    // 带加密参数的查询
    @Test
    public void testDecrypt_selectByInName() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        List<UserInfo> userInfoList = fileMapper.selectByInName("张三", "李四");
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被解密", SecurityUtils.isDecrypted(userInfo.getName()));
            Assert.assertTrue("银行卡号应当被解密", SecurityUtils.isDecrypted(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    // 带参数的查询
    @Test
    public void testDecrypt_selectByType() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        List<UserInfo> userInfoList = fileMapper.selectByType("USER");
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被解密", SecurityUtils.isDecrypted(userInfo.getName()));
            Assert.assertTrue("银行卡号应当被解密", SecurityUtils.isDecrypted(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    // 子查询
    @Test
    public void testDecrypt_subSelectAll() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        List<UserInfo> userInfoList = fileMapper.subSelectAll();
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被解密", SecurityUtils.isDecrypted(userInfo.getName()));
            Assert.assertTrue("银行卡号应当被解密", SecurityUtils.isDecrypted(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    // 带加密参数子查询
    @Test
    public void testDecrypt_subSelectAllWithParam() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        List<UserInfo> userInfoList = fileMapper.subSelectAllWithParam("张三", "USER", "62260000001");
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被解密", SecurityUtils.isDecrypted(userInfo.getName()));
            Assert.assertTrue("银行卡号应当被解密", SecurityUtils.isDecrypted(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    // 带加密参数的查询统计
    @Test
    public void testDecrypt_countByName() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        Integer count = fileMapper.countByName("张三");
        Assert.assertNotNull("查询结果不为空", count);
        Assert.assertTrue("查询结果数量大于0", count > 0);
    }

    // 带加密参数union查询
    // TODO union查询不支持（没法找到表名）
    @Test
    public void testDecrypt_unionSelectAllWithParam() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        List<UserInfo> userInfoList = fileMapper.unionSelectAllWithParam("张三", "USER", "13212345678");
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名   没法解密", SecurityUtils.isEncrypted(userInfo.getName()));
            Assert.assertTrue("银行卡号没法解密", SecurityUtils.isEncrypted(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    // 带加密参数GroupBy
    @Test
    public void testDecrypt_selectByNameGroupBy() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        List<UserInfo> userInfoList = fileMapper.selectByNameGroupBy("张三");
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被解密", SecurityUtils.isDecrypted(userInfo.getName()));
        }
    }

    // 带加密参数OrderBy
    @Test
    public void testDecrypt_selectByNameOrderBy() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        List<UserInfo> userInfoList = fileMapper.selectByNameOrderBy("张三");
        Assert.assertTrue("查询结果不为空", !userInfoList.isEmpty());
        for (UserInfo userInfo : userInfoList) {
            Assert.assertTrue("姓名应当被解密", SecurityUtils.isDecrypted(userInfo.getName()));
            Assert.assertTrue("银行卡号应当被解密", SecurityUtils.isDecrypted(userInfo.getBankCardNo()));
            Assert.assertTrue("类型不需要解密", SecurityUtils.unEncryptedColumn(userInfo.getType()));
        }
    }

    // 带加密参数的查询
    @Test
    public void testDecrypt_selectByNameVariable() {
        UserInfoMapper fileMapper = getSession("mybatis-config.xml").getMapper(UserInfoMapper.class);

        List<UserInfo> userInfoList = fileMapper.selectByNameVariable("张三");// ${name}已被mybatis预编译
        Assert.assertTrue("查询结果应当为空", userInfoList.isEmpty());
    }

    // 条件型字段解密
    @Test
    public void testDecrypt_selectAllVerticalTable() {
        ParamInfoMapper paramInfoMapper = getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class);

        List<ParamInfo> paramInfoList = paramInfoMapper.selectAll();
        Assert.assertTrue("查询结果应当为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            if (paramInfo.getParamName().equals("name") || paramInfo.getParamName().equals("bankCard") ) {
                Assert.assertTrue("手机号应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
            } else {
                Assert.assertTrue("手机号不应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
            }
        }
    }

    // 条件型字段解密
    @Test
    public void testDecrypt_selectByParamValueAndParamName() {
        ParamInfoMapper paramInfoMapper = getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class);

        List<ParamInfo> paramInfoList = paramInfoMapper.selectByParamNameAndParamValue("name", "13212345678");
        Assert.assertTrue("查询结果应当为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            if (paramInfo.getParamName().equals("name")) {
                Assert.assertTrue("手机号应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
            } else {
                Assert.assertTrue("手机号不应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
            }
        }
    }

    // 条件型字段解密
    @Test
    public void testDecrypt_selectByParamValueAndParamName2() {
        ParamInfoMapper paramInfoMapper = getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class);

        List<ParamInfo> paramInfoList = paramInfoMapper.selectByParamNameAndParamValue("test", "13212343333");
        Assert.assertTrue("查询结果应当为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            if (paramInfo.getParamName().equals("name")) {
                Assert.assertTrue("手机号应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
            } else {
                Assert.assertTrue("手机号不应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
            }
        }
    }

    // 条件型字段解密
    @Test
    public void testDecrypt_selectByFixedTypeAndParamValue() {
        ParamInfoMapper paramInfoMapper = getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class);

        List<ParamInfo> paramInfoList = paramInfoMapper.selectByFixedTypeAndParamValue("13212345678");
        Assert.assertTrue("查询结果应当为空", paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            if (paramInfo.getParamName().equals("name")) {
                Assert.assertTrue("手机号应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
            } else {
                Assert.assertTrue("手机号不应当被加密", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
            }
        }
    }

    // 条件型字段解密
    @Test
    public void testDecrypt_selectParamValueByFixedType() {
        ParamInfoMapper paramInfoMapper = getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class);

        List<ParamInfo> paramInfoList = paramInfoMapper.selectParamValueByFixedParamName();
        Assert.assertTrue("查询结果应当为空", !paramInfoList.isEmpty());
        for (ParamInfo paramInfo : paramInfoList) {
            // TODO 条件字段在查询条件中，没法解析
            Assert.assertTrue("手机号不应当被解密", SecurityUtils.isEncrypted(paramInfo.getParamValue()));
        }
    }

    // 条件型字段解密，条件字段不存在无法解密
    @Test
    public void testDecrypt_selectAllVerticalTable_noConditionColumn() {
        ParamInfoMapper paramInfoMapper = getSession("mybatis-config.xml").getMapper(ParamInfoMapper.class);

        List<ParamInfo> paramInfoList = paramInfoMapper.selectAllParamValue();
        Assert.assertTrue("查询结果应当为空", !paramInfoList.isEmpty());
        int i = 0;
        for (ParamInfo paramInfo : paramInfoList) {
            if (i == 0 || i==1) {
                Assert.assertTrue("手机号应当还是加密的", SecurityUtils.isEncrypted(paramInfo.getParamValue()));
            } else {
                Assert.assertTrue("手机号不应当是加密的", SecurityUtils.unEncryptedColumn(paramInfo.getParamValue()));
            }
            i++;
        }
    }
}
