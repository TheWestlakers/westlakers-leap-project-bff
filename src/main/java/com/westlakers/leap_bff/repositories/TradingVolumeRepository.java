package com.westlakers.leap_bff.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.westlakers.leap_bff.entities.TradingVolume;

@Repository
public class TradingVolumeRepository {

    private static final String FIND_PAGE_ORDERED_BY_EXECUTED_AT = """
                 SELECT stv.staging_id, stv.order_id, stv.order_status_id, os.status_name AS order_status,
                     stv.account_id, stv.instrument_id, stv.user_id,
                   side, quantity, limit_price, executed_at, account_type,
                   first_name, last_name, instrument_code, instrument_name,
                   instrument_type, client_segment, created_at
                 FROM stg_trading_volumes stv
                 LEFT JOIN order_status os ON stv.order_status_id = os.order_status_id
                 ORDER BY stv.executed_at DESC NULLS LAST, stv.staging_id ASC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_ALL = "SELECT COUNT(*) FROM stg_trading_volumes";

    private final JdbcTemplate analyticsJdbcTemplate;

    public TradingVolumeRepository(JdbcTemplate analyticsJdbcTemplate) {
        this.analyticsJdbcTemplate = analyticsJdbcTemplate;
    }

    public List<TradingVolume> findPageOrderedByExecutedAt(int pageSize, int offset) {
        return analyticsJdbcTemplate.query(
                FIND_PAGE_ORDERED_BY_EXECUTED_AT,
                new TradingVolumeRowMapper(),
                pageSize,
                offset);
    }

    public long countAll() {
        Long count = analyticsJdbcTemplate.queryForObject(COUNT_ALL, Long.class);
        return count == null ? 0 : count;
    }

    private static class TradingVolumeRowMapper implements RowMapper<TradingVolume> {
        @Override
        public TradingVolume mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return new TradingVolume(
                    resultSet.getLong("staging_id"),
                    resultSet.getLong("order_id"),
                    resultSet.getString("order_status_id"),
                    resultSet.getString("order_status"),
                    resultSet.getLong("account_id"),
                    resultSet.getInt("instrument_id"),
                    resultSet.getLong("user_id"),
                    resultSet.getString("side"),
                    resultSet.getBigDecimal("quantity"),
                    resultSet.getBigDecimal("limit_price"),
                    resultSet.getTimestamp("executed_at") == null
                            ? null : resultSet.getTimestamp("executed_at").toLocalDateTime(),
                    resultSet.getString("account_type"),
                    resultSet.getString("first_name"),
                    resultSet.getString("last_name"),
                    resultSet.getString("instrument_code"),
                    resultSet.getString("instrument_name"),
                    resultSet.getString("instrument_type"),
                    resultSet.getString("client_segment"),
                    resultSet.getTimestamp("created_at") == null
                            ? null : resultSet.getTimestamp("created_at").toLocalDateTime());
        }
    }
}