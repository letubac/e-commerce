package com.ecommerce.config;

import vn.com.unit.miragesql.miragesql.annotation.PrimaryKey;
import vn.com.unit.miragesql.miragesql.dialect.Dialect;
import vn.com.unit.miragesql.miragesql.type.ValueType;

/**
 * SQLServerDialect
 *
 * @version 01-00
 * @since 01-00
 * @author 
 */
public class SQLServerDialect implements Dialect {

	@Override
	public String getName() {
		return "sqlserver";
	}

	@Override
	public boolean needsParameterForResultSet() {
		return false;
	}

	@Override
	public boolean supportsGenerationType(PrimaryKey.GenerationType generationType) {
		if(generationType == PrimaryKey.GenerationType.IDENTITY){
			return false;
		}
		return true;
	}

	@Override
	public String getCountSql(String sql) {
		return "SELECT COUNT(*) FROM (" + sql + ") A";
	}

	@Override
	public ValueType<?> getValueType() {
		return null;
	}

	@Override
	public String getSequenceSql(String sequenceName) {
		return String.format(
				"SELECT (NEXT VALUE FOR %s) AS NEXTVAL", sequenceName);
	}

}