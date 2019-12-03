package xyz.tpn.dbsecurity.mapper;


import org.apache.ibatis.annotations.*;
import xyz.tpn.dbsecurity.model.ParamInfo;

import java.util.List;

public interface ParamInfoMapper {

    @Insert("insert into t_param(id, param_name, param_value, type) values(#{id}, #{paramName}, #{paramValue}, #{type})")
    void insert(ParamInfo paramInfo);

    void insertBatch(List<ParamInfo> paramInfoList);

    @Update("update t_param set param_name= #{paramName}, param_value = #{paramValue} where id=#{id}")
    void updateById(ParamInfo paramInfo);

    @Update("update t_param set id=id, param_value = #{paramValue} where ( id=#{id} and param_name=#{paramName})")
    void updateByParamName(ParamInfo paramInfo);

    @Update("update t_param set param_name= 'name', param_value = #{paramValue} where id=#{id} and type='encrypt'")
    void updateSetFixedParamNameById(ParamInfo paramInfo);

    @Update("update t_param set param_value = #{paramValue} where id=#{id} and param_name='name' and type='encrypt'")
    void updateByFixedParamName(ParamInfo paramInfo);

    @Update("update t_param set param_value = #{paramValue} where id=#{id}")
    void updateByIdNoParamName(ParamInfo paramInfo);

    @Update("update t_param set param_name=#{paramName} where param_value = #{paramValue}  and  id=#{id}")
    void updateByParamValue(ParamInfo paramInfo);

    @Update("update t_param set type=#{type} where param_value = #{paramValue}  and  id=#{id} and param_name=#{paramName}")
    void updateByParamValueAndParamName(ParamInfo paramInfo);

    @Select("select * from t_param i")
    List<ParamInfo> selectAll();

    @Select("select param_value from t_param i")
    List<ParamInfo> selectAllParamValue();

    @Select("select * from t_param i where param_name=#{paramName} and param_value=#{paramValue}")
    List<ParamInfo> selectByParamNameAndParamValue(@Param("paramName") String paramName, @Param("paramValue") String paramValue);

    @Select("select * from t_param i where type='encrypt' and param_value=#{paramValue}")
    List<ParamInfo> selectByFixedTypeAndParamValue(@Param("paramValue") String paramValue);

    @Select("select param_value from t_param i where param_name='bankCard'")
    List<ParamInfo> selectParamValueByFixedParamName();

    @Delete("delete from t_param")
    void deleteAll();
}
