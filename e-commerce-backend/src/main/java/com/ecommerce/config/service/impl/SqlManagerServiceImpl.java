package com.ecommerce.config.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.ecommerce.config.service.SqlManagerService;
import com.ecommerce.util.CustomMirageUtil;

import vn.com.unit.miragesql.miragesql.DefaultEntityOperator;
import vn.com.unit.miragesql.miragesql.SqlManagerImpl;
import vn.com.unit.miragesql.miragesql.bean.BeanDescFactory;
import vn.com.unit.miragesql.miragesql.dialect.PostgreSQLDialect;
import vn.com.unit.miragesql.miragesql.integration.spring.SpringConnectionProvider;
import vn.com.unit.miragesql.miragesql.naming.NameConverter;
import vn.com.unit.miragesql.miragesql.type.BigDecimalValueType;
import vn.com.unit.miragesql.miragesql.type.BooleanPrimitiveValueType;
import vn.com.unit.miragesql.miragesql.type.BooleanValueType;
import vn.com.unit.miragesql.miragesql.type.ByteArrayValueType;
import vn.com.unit.miragesql.miragesql.type.DoublePrimitiveValueType;
import vn.com.unit.miragesql.miragesql.type.DoubleValueType;
import vn.com.unit.miragesql.miragesql.type.FloatPrimitiveValueType;
import vn.com.unit.miragesql.miragesql.type.FloatValueType;
import vn.com.unit.miragesql.miragesql.type.IntegerPrimitiveValueType;
import vn.com.unit.miragesql.miragesql.type.IntegerValueType;
import vn.com.unit.miragesql.miragesql.type.LongPrimitiveValueType;
import vn.com.unit.miragesql.miragesql.type.LongValueType;
import vn.com.unit.miragesql.miragesql.type.ShortPrimitiveValueType;
import vn.com.unit.miragesql.miragesql.type.ShortValueType;
import vn.com.unit.miragesql.miragesql.type.SqlDateValueType;
import vn.com.unit.miragesql.miragesql.type.StringValueType;
import vn.com.unit.miragesql.miragesql.type.TimeValueType;
import vn.com.unit.miragesql.miragesql.type.TimestampValueType;
import vn.com.unit.miragesql.miragesql.type.UtilDateValueType;
import vn.com.unit.miragesql.miragesql.type.enumerate.EnumOneBasedOrdinalValueType;
import vn.com.unit.miragesql.miragesql.type.enumerate.EnumOrdinalValueType;
import vn.com.unit.miragesql.miragesql.type.enumerate.EnumStringValueType;

@Primary
@Service("sqlManagerPr")
/**
 * author: LeTuBac
 */
public class SqlManagerServiceImpl extends SqlManagerImpl implements SqlManagerService {
    private static final Logger logger = LoggerFactory.getLogger(SqlManagerServiceImpl.class);

    /**
     * Constructor-injected dependencies ensure a single SqlManager bean is fully
     * configured
     * and that the NameConverter is not null when repositories use it.
     */
    @Autowired
    public SqlManagerServiceImpl(SpringConnectionProvider connectionProvider,
            BeanDescFactory beanDescFactory,
            NameConverter nameConverter) {
        super();

        // core wiring
        this.setConnectionProvider(connectionProvider);
        this.setDialect(new PostgreSQLDialect());
        this.setBeanDescFactory(beanDescFactory);
        this.setNameConverter(nameConverter);

        // Remove reflection logic and directly set the nameConverter in
        // CustomMirageUtil
        CustomMirageUtil.nameConverter = nameConverter;
        logger.info("Set CustomMirageUtil.nameConverter to {}", nameConverter);

        this.setEntityOperator(new DefaultEntityOperator());

        // register value types (same list as trước)
        this.addValueType(new StringValueType());
        this.addValueType(new IntegerValueType());
        this.addValueType(new IntegerPrimitiveValueType());
        this.addValueType(new LongValueType());
        this.addValueType(new LongPrimitiveValueType());
        this.addValueType(new ShortValueType());
        this.addValueType(new ShortPrimitiveValueType());
        this.addValueType(new DoubleValueType());
        this.addValueType(new DoublePrimitiveValueType());
        this.addValueType(new FloatValueType());
        this.addValueType(new FloatPrimitiveValueType());
        this.addValueType(new BooleanValueType());
        this.addValueType(new BooleanPrimitiveValueType());
        this.addValueType(new BigDecimalValueType());
        this.addValueType(new SqlDateValueType());
        this.addValueType(new UtilDateValueType());
        this.addValueType(new TimeValueType());
        this.addValueType(new TimestampValueType());
        this.addValueType(new ByteArrayValueType());
        this.addValueType(new EnumStringValueType());
        this.addValueType(new EnumOrdinalValueType());
        this.addValueType(new EnumOneBasedOrdinalValueType());
    }

    @Override
    public Long getNextValBySeqName(String seqName) {
        logger.info("Fetching next value for sequence: {}", seqName);
        String querySql = dialect.getSequenceSql(seqName);
        Long sequenceValue = super.sqlExecutor.getSingleResult(Long.class, querySql, new Object[0]);
        logger.info("Next value for sequence {}: {}", seqName, sequenceValue);
        return sequenceValue;
    }
}
