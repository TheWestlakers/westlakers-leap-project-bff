package com.westlakers.leap_bff.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.westlakers.leap_bff.dtos.TopInstrumentDTO;
import com.westlakers.leap_bff.dtos.TopInvestorDTO;

@Repository
public class TopAnalyticsRepository {

    private static final String FIND_TOP_INVESTORS = """
            SELECT dc.user_id AS investor_id,
                   dc.first_name,
                   dc.last_name,
                   dc.client_segment,
                   SUM(aca.total_trades)::BIGINT AS total_trades,
                   SUM(aca.total_volume) AS total_volume,
                   SUM(aca.total_value) AS total_value
            FROM agg_client_activity_daily aca
            JOIN dim_clients dc ON dc.client_key = aca.client_key
            GROUP BY dc.user_id, dc.first_name, dc.last_name, dc.client_segment
            ORDER BY total_value DESC NULLS LAST, total_trades DESC, dc.user_id
            LIMIT ?
            """;

    private static final String FIND_TOP_INSTRUMENTS = """
            SELECT di.instrument_id,
                   di.instrument_code,
                   di.instrument_name,
                   di.instrument_type,
                   SUM(aia.total_trades)::BIGINT AS total_trades,
                   SUM(aia.total_volume) AS total_volume,
                   SUM(aia.total_value) AS total_value
            FROM agg_instrument_activity aia
            JOIN dim_instruments di ON di.instrument_key = aia.instrument_key
            GROUP BY di.instrument_id, di.instrument_code, di.instrument_name, di.instrument_type
            ORDER BY total_value DESC NULLS LAST, total_trades DESC, di.instrument_id
            LIMIT ?
            """;

    private final JdbcTemplate analyticsJdbcTemplate;

        public TopAnalyticsRepository(@Qualifier("analyticsJdbcTemplate") JdbcTemplate analyticsJdbcTemplate) {
        this.analyticsJdbcTemplate = analyticsJdbcTemplate;
    }

    public List<TopInvestorDTO> findTopInvestors(int limit) {
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
                limit);
    }

    public List<TopInstrumentDTO> findTopInstruments(int limit) {
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
                limit);
    }
}