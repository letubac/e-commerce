package com.ecommerce.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.ecommerce.constant.AppApiExceptionCodeConstant;
import com.ecommerce.constant.CommonConstant;
import com.ecommerce.constant.DataTypeConstant;
import com.ecommerce.exception.DetailException;
import com.ecommerce.service.CommonService;
import com.ecommerce.util.CommonDateUtil;
import com.ecommerce.util.CommonObjectUtil;
import com.ecommerce.util.CommonStringUtil;

@Service
public class CommonServiceImpl implements CommonService {

	// @Autowired
	// private DBRepository commonRepository;
	//
	private static String COLUMN_DEFAULT = "ID";

	@Override
	public Date getSystemDate() {
		return CommonDateUtil.getSystemDateTime();
	}

	@Override
	public String generateCodeFromIdWithFormat(Long seqId, String format) {
		if (null == format) {
			format = CommonConstant.YYMMDD;
		}
		// get next id from sequence
		Date sysDate = this.getSystemDate();
		// get date with format yyyyMMdd
		String dateFormat = CommonDateUtil.formatDateToString(sysDate, format);

		// ID with format 999
		String idStr = CommonStringUtil.right(seqId.toString(), 3);
		String id = CommonStringUtil.leftPad(idStr, 3, '0');
		// CODE with format #999YYMMDD
		return CommonConstant.SYSTEM_PREFIX.concat(id.concat(dateFormat));
	}

	@Override
	public String generateDocumentCode(Long seqId, String prefix, String format) {
		if (null == format) {
			format = CommonConstant.YYMMDD;
		}
		// get next id from sequence
		Date sysDate = this.getSystemDate();
		// get date with format yyyyMMdd
		String dateFormat = CommonDateUtil.formatDateToString(sysDate, format);

		// ID with format 9999
		String idStr = CommonStringUtil.right(seqId.toString(), 4);
		String id = CommonStringUtil.leftPad(idStr, 4, '0');
		// CODE with format >> 11-210823-1000
		return prefix.concat(CommonConstant.HYPHEN)
//        		.concat(dateFormat).concat(CommonConstant.HYPHEN)
				.concat(id);
	}

	public static void main(String[] args) {
		String seqId = "100021";
		String idStr = CommonStringUtil.right(seqId.toString(), 3);
		String id = CommonStringUtil.leftPad(idStr, 3, '0');

		System.out.println(id);
	}

	@Override
	public String generateCodeFromId(Long seqId) {
		return generateCodeFromIdWithFormat(seqId, null);
	}

	// @Override
	// public Sort buildSortAlias(Sort sort, Class<?> clazz, String alias) throws
	// DetailException {
	// List<String> columList = CommonObjectUtil.getColumnListFromEntity(clazz);
	// List<Order> ordersRs = new ArrayList<>();
	// for (Order order : sort) {
	// String property = order.getProperty().toUpperCase();
	// if (columList.indexOf(property) >= 0) {
	// order = order.isAscending() ?
	// Order.asc(alias.concat(CommonConstant.DOT).concat(property))
	// : Order.desc(alias.concat(CommonConstant.DOT).concat(property));
	// ordersRs.add(order);
	// } else {
	// throw new
	// DetailException(CommonExceptionCodeConstant.E101700_SORT_DYNAMIC_TABLE, new
	// String[] { order.getProperty() },
	// true);
	// }
	// }
	// return Sort.by(ordersRs);
	// }

	@Override
	public Sort buildSortAlias(Sort sort, Class<?> clazz, String alias) throws DetailException {
		List<Order> ordersRs = new ArrayList<>();
		String propertiesError = CommonStringUtil.EMPTY;
		try {
			for (Order order : sort) {
				String property = order.getProperty();
				propertiesError = property;
				String columnSort = CommonObjectUtil.getColumnListFromEntity(clazz, property);
				if (CommonStringUtil.isNotBlank(columnSort)) {
					order = order.isAscending() ? Order.asc(alias.concat(CommonConstant.DOT).concat(columnSort))
							: Order.desc(alias.concat(CommonConstant.DOT).concat(columnSort));
					ordersRs.add(order);
				} else {
					throw new DetailException(AppApiExceptionCodeConstant.E402608_SORT_DYNAMIC_TABLE,
							new String[] { order.getProperty() }, true);
				}
			}
		} catch (Exception e) {
			throw new DetailException(AppApiExceptionCodeConstant.E402608_SORT_DYNAMIC_TABLE,
					new String[] { propertiesError }, true);
		}

		return Sort.by(ordersRs);
	}

	@Override
	public Sort buildSortAliasNotUseDefault(Sort sort, Class<?> clazz, String alias) throws DetailException {
		List<Order> ordersRs = new ArrayList<>();
		String propertiesError = CommonStringUtil.EMPTY;
		try {
			for (Order order : sort) {
				String property = order.getProperty();
				propertiesError = property;
				String columnSort = CommonObjectUtil.getColumnListFromEntity(clazz, property);

				/**
				 * IF property not equal column default sort and property not found clazz then
				 * break function
				 */
				if (COLUMN_DEFAULT.equals(columnSort) && !columnSort.equals(property)) {
					continue;
				}
				if (CommonStringUtil.isNotBlank(columnSort)) {
					order = order.isAscending() ? Order.asc(alias.concat(CommonConstant.DOT).concat(columnSort))
							: Order.desc(alias.concat(CommonConstant.DOT).concat(columnSort));
					ordersRs.add(order);
				} else {
					throw new DetailException(AppApiExceptionCodeConstant.E402608_SORT_DYNAMIC_TABLE,
							new String[] { order.getProperty() }, true);
				}
			}
		} catch (Exception e) {
			throw new DetailException(AppApiExceptionCodeConstant.E402608_SORT_DYNAMIC_TABLE,
					new String[] { propertiesError }, true);
		}

		return Sort.by(ordersRs);
	}

	@Override
	public <T extends Enum<T>> Sort buildSortEnums(Sort sort, T[] enumsDatas, Class<?> clazz) throws DetailException {
		List<Order> ordersRs = new ArrayList<>();
		String propertiesError = CommonStringUtil.EMPTY;
		try {
			for (Order order : sort) {
				String property = order.getProperty();
				propertiesError = property;
				String valueMapingEnum = Stream.of(enumsDatas).filter(item -> item.toString().equals(property))
						.map(Enum::name).findFirst().orElse(null);
				String columnSort = CommonStringUtil.isNotBlank(valueMapingEnum) ? valueMapingEnum
						: CommonObjectUtil.getColumnListFromEntity(clazz, property);

				if (CommonStringUtil.isNotBlank(columnSort)) {
					order = order.isAscending() ? Order.asc(columnSort) : Order.desc(columnSort);
					ordersRs.add(order);
				} else {
					throw new DetailException(AppApiExceptionCodeConstant.E402608_SORT_DYNAMIC_TABLE,
							new String[] { order.getProperty() }, true);
				}
			}
		} catch (Exception e) {
			throw new DetailException(AppApiExceptionCodeConstant.E402608_SORT_DYNAMIC_TABLE,
					new String[] { propertiesError }, true);
		}

		return Sort.by(ordersRs);
	}

	@Override
	public String generateTitleS(String code, String title) {
		if (code != null) {
			return code.concat(CommonConstant.SPACE).concat(title);
		}
		return title;
	}

	@Override
	public Object parseValueByDataType(String value, String type) {
		if (CommonStringUtil.isNotBlank(value) && value.startsWith("\"") && value.endsWith("\"")) {
			value = value.substring(1, value.length() - 1);
		}
		Object result = null;

		switch (type) {
		case DataTypeConstant.DATA_TYPE_BOOLEAN:
			Boolean valueBoolean = null;
			try {
				valueBoolean = Boolean.valueOf(value);
			} catch (Exception e) {
				valueBoolean = null;
			}
			result = valueBoolean;
			break;
		case DataTypeConstant.DATA_TYPE_DATE:
			Date valueDate = null;
			try {
				valueDate = CommonDateUtil.parseDate(value, CommonDateUtil.DDMMYYYY_HYPHEN,
						CommonDateUtil.DATE_TIME_HYPHEN);
			} catch (Exception e) {
			}
			result = valueDate;
			break;
		case DataTypeConstant.DATA_TYPE_DOUBLE:
			Double valueDouble = null;
			try {
				valueDouble = Double.valueOf(value);
			} catch (Exception e) {
				valueDouble = null;
			}
			result = valueDouble;
			break;
		case DataTypeConstant.DATA_TYPE_FLOAT:
			Float valueFloat = null;
			try {
				valueFloat = Float.valueOf(value);
			} catch (Exception e) {
				valueFloat = null;
			}
			result = valueFloat;
			break;
		case DataTypeConstant.DATA_TYPE_INTEGER:
			Integer valueInteger = null;
			try {
				valueInteger = Integer.valueOf(value);
			} catch (Exception e) {
				valueInteger = null;
			}
			result = valueInteger;
			break;
		case DataTypeConstant.DATA_TYPE_LONG:
			Long valueLong = null;
			try {
				valueLong = Long.valueOf(value.toString().replace(",", ""));
			} catch (Exception e) {
				valueLong = null;
			}
			result = valueLong;
			break;
		default:
			result = value;
		}

		return result;
	}

	@Override
	public Date getSystemDateTime() {
		return CommonDateUtil.getSystemDateTime();
	}

}
