package org.csu.mypetstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.csu.mypetstore.entity.Product;

/**
 * product 表的数据访问接口，继承 BaseMapper 获得单表 CRUD 能力。
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
