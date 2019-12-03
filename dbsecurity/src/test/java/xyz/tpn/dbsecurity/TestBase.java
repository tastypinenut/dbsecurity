package xyz.tpn.dbsecurity;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;

public abstract class TestBase {

    protected SqlSession defaultSqlSession;

    protected void before() {
        defaultSqlSession = getSession("mybatis-config-default.xml");
    }


    protected SqlSession getSession(String configFile) {
        Reader reader;
        try {
            reader = Resources.getResourceAsReader(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sqlSessionFactory = builder.build(reader);
        return sqlSessionFactory.openSession(true);
    }
}
