package com.westlakers.leap_bff.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.westlakers.leap_bff.dtos.AllTimeAnalyticsSummaryDTO;
import com.westlakers.leap_bff.dtos.TopInstrumentDTO;
import com.westlakers.leap_bff.dtos.TopInvestorDTO;

@Repository
public class TopAnalyticsRepository {

    private static final String INVESTORS_FROM_AND_GROUP_BY = """
            FROM fact_trading_volumes_hourly ftv
            JOIN dim_clients dc ON dc.client_key = ftv.client_key
            GROUP BY dc.user_id, dc.first_name, dc.last_name, dc.client_segment
            """;

    private static final String INSTRUMENTS_FROM_AND_GROUP_BY = """
            FROM fact_trading_volumes_hourly ftv
            JOIN dim_instruments di ON di.instrument_key = ftv.instrument_key
            GROUP BY di.instrument_id, di.instrument_code, di.instrument_name, di.instrument_type
            """;

    private static final String FIND_TOP_INVESTORS = """
            SELECT dc.user_id AS investor_id,
                   dc.first_name,
                   dc.last_name,
                   dc.client_segment,
                   SUM(ftv.trade_count)::BIGINT AS total_trades,
                   SUM(ftv.total_volume) AS total_volume,
                   SUM(ftv.total_value) AS total_value
            """ + INVESTORS_FROM_AND_GROUP_BY + """
            ORDER BY total_value DESC NULLS LAST, total_trades DESC, dc.user_id
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_INVESTORS =
            "SELECT COUNT(*) FROM (SELECT 1 " + INVESTORS_FROM_AND_GROUP_BY + ") grouped";

    private static final String FIND_TOP_INSTRUMENTS = """
            SELECT di.instrument_id,
                   di.instrument_code,
                   di.instrument_name,
                   di.instrument_type,
                   SUM(ftv.trade_count)::BIGINT AS total_trades,
                   SUM(ftv.total_volume) AS total_volume,
                   SUM(ftv.total_value) AS total_value
            """ + INSTRUMENTS_FROM_AND_GROUP_BY + """
            ORDER BY total_value DESC NULLS LAST, total_trades DESC, di.instrument_id
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_INSTRUMENTS =
            "SELECT COUNT(*) FROM (SELECT 1 " + INSTRUMENTS_FROM_AND_GROUP_BY + ") grouped";

        private static final String ALL_TIME_SUMMARY = """
                        WITH executed_orders AS (
                                SELECT stg.user_id,
                                           stg.instrument_id,
                                           stg.quantity,
                                           stg.execution_price
                                FROM stg_trading_volumes stg
                                WHERE stg.executed_at IS NOT NULL
                        ),
                        client_instrument_counts AS (
                                SELECT user_id, COUNT(DISTINCT instrument_id) AS instruments_count
                                FROM executed_orders
                                GROUP BY user_id
                        )
                        SELECT COALESCE(AVG(ABS(quantity)), 0) AS average_trade_size,
                                   COUNT(DISTINCT instrument_id)::BIGINT AS instruments_traded,
                                   COALESCE((SELECT AVG(instruments_count) FROM client_instrument_counts), 0)
                                           AS average_instruments_per_active_client,
                                   COUNT(DISTINCT user_id)::BIGINT AS operating_traders,
                                   COALESCE(SUM(ABS(quantity * execution_price)), 0) AS total_absolute_funds_processed
                        FROM executed_orders
                        """;

    private final JdbcTemplate analyticsJdbcTemplate;

        public TopAnalyticsRepository(@Qualifier("analyticsJdbcTemplate") JdbcTemplate analyticsJdbcTemplate) {
        this.analyticsJdbcTemplate = analyticsJdbcTemplate;
    }

    public List<TopInvestorDTO> findTopInvestors(int limit, int offset) {
        return analyticsJdbcTemplate.query(
                FIND_TOP_INVESTORS,
                (resultSet, rowNum) -> new TopInvestorDTO(
                        resultSet.getLong("investor_id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("client_segment"),
                        resultSet.getLong("total_trades"),
                        resultSet.getBigDecimal("total_volume"),
                        resultSet.getBigDecimal("total_value")),
                limit,
                offset);
    }

    public long countInvestors() {
        Long count = analyticsJdbcTemplate.queryForObject(COUNT_INVESTORS, Long.class);
        return count == null ? 0 : count;
    }

    public List<TopInstrumentDTO> findTopInstruments(int limit, int offset) {
        return analyticsJdbcTemplate.query(
                FIND_TOP_INSTRUMENTS,
                (resultSet, rowNum) -> new TopInstrumentDTO(
                        resultSet.getInt("instrument_id"),
                        resultSet.getString("instrument_code"),
                        resultSet.getString("instrument_name"),
                        resultSet.getString("instrument_type"),
                        resultSet.getLong("total_trades"),
                        resultSet.getBigDecimal("total_volume"),
                        resultSet.getBigDecimal("total_value")),
                limit,
                offset);
    }

    public long countInstruments() {
        Long count = analyticsJdbcTemplate.queryForObject(COUNT_INSTRUMENTS, Long.class);
        return count == null ? 0 : count;
    }

    public AllTimeAnalyticsSummaryDTO getAllTimeSummary() {
        return analyticsJdbcTemplate.queryForObject(
                ALL_TIME_SUMMARY,
                (resultSet, rowNum) -> new AllTimeAnalyticsSummaryDTO(
                        resultSet.getBigDecimal("average_trade_size"),
                        resultSet.getLong("instruments_traded"),
                        resultSet.getBigDecimal("average_instruments_per_active_client"),
                        resultSet.getLong("operating_traders"),
                        resultSet.getBigDecimal("total_absolute_funds_processed")));
        }
}
