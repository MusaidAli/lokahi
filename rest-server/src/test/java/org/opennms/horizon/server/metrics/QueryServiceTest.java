/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.server.metrics;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opennms.horizon.server.model.TimeRangeUnit;
import org.opennms.horizon.server.service.metrics.Constants;
import org.opennms.horizon.server.service.metrics.QueryService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;

import static org.opennms.horizon.server.service.metrics.Constants.QUERY_FOR_BW_IN_UTIL_PERCENTAGE;
import static org.opennms.horizon.server.service.metrics.Constants.QUERY_FOR_TOTAL_NETWORK_BITS_IN;
import static org.opennms.horizon.server.service.metrics.Constants.QUERY_FOR_TOTAL_NETWORK_BITS_OUT;
import static org.opennms.horizon.server.service.metrics.Constants.QUERY_FOR_TOTAL_NETWORK_IN_BITS;
import static org.opennms.horizon.server.service.metrics.Constants.QUERY_PREFIX;
import static org.opennms.horizon.server.service.metrics.Constants.TOTAL_NETWORK_BITS_IN;
import static org.opennms.horizon.server.service.metrics.Constants.TOTAL_NETWORK_BITS_OUT;

public class QueryServiceTest {


    @Test
    public void testLabelsSubstitution() {

        QueryService queryService = new QueryService();
        var labels = new HashMap<String, String>();
        labels.put("if_name", "en0");
        labels.put("monitor", "SNMP");
        labels.put("node_id", "5");
        var labelQuery = queryService.getLabelsQueryString(labels);
        var query = String.format(QUERY_FOR_TOTAL_NETWORK_IN_BITS, labelQuery);
        Assertions.assertEquals("irate(ifHCInOctets{if_name=\"en0\",monitor=\"SNMP\",node_id=\"5\"}[4m])*8", query);

        var bwUtilQuery = String.format(QUERY_FOR_BW_IN_UTIL_PERCENTAGE, labelQuery);
        Assertions.assertEquals("(irate(ifHCInOctets{if_name=\"en0\",monitor=\"SNMP\",node_id=\"5\"}[4m])*8) / " +
            "(ifHighSpeed{if_name=\"en0\",monitor=\"SNMP\",node_id=\"5\"} *1000000) * 100 " +
            "unless ifHighSpeed{if_name=\"en0\",monitor=\"SNMP\",node_id=\"5\"} == 0", bwUtilQuery);
    }

    @Test
    public void testCustomQuery() {
        QueryService queryService = new QueryService();
        var labels = new HashMap<String, String>();
        labels.put("instance", "192.168.1.1");
        labels.put("monitor", "ICMP");
        labels.put("system_id", "minion-standalone");
        var queryString = queryService.getQueryString(Optional.empty(), Constants.REACHABILITY_PERCENTAGE, labels, 24, TimeRangeUnit.HOUR);
        Assertions.assertEquals("query=(count_over_time(response_time_msec{instance=\"192.168.1.1\"," +
            "system_id=\"minion-standalone\",monitor=\"ICMP\"}[24h])/1440)*100 or vector(0)", queryString);
    }

    @Test
    void testTotalQuery() {
        long end = System.currentTimeMillis() / 1000L;
        long start = end - Duration.ofHours(24).getSeconds();
        QueryService queryService = new QueryService();

        // We pass our 'end' value to ensure our start/end values match exactly
        var bitsInQuery = queryService.getQueryString(Optional.empty(), TOTAL_NETWORK_BITS_IN, new HashMap<>(), 24, TimeRangeUnit.HOUR, end);
        var inSplitQuery = bitsInQuery.split("&");
        Assertions.assertEquals(QUERY_PREFIX + URLEncoder.encode(QUERY_FOR_TOTAL_NETWORK_BITS_IN, StandardCharsets.UTF_8), inSplitQuery[0]);
        Assertions.assertEquals("start=" + start, inSplitQuery[1]);
        Assertions.assertEquals("end=" + end, inSplitQuery[2]);
        Assertions.assertEquals("step=2m", inSplitQuery[3]);

        // We pass our 'end' value to ensure our start/end values match exactly
        var bitsOutQuery = queryService.getQueryString(Optional.empty(), TOTAL_NETWORK_BITS_OUT, new HashMap<>(), 24, TimeRangeUnit.HOUR, end);
        var outSplitQuery = bitsOutQuery.split("&");
        Assertions.assertEquals(QUERY_PREFIX + URLEncoder.encode(QUERY_FOR_TOTAL_NETWORK_BITS_OUT, StandardCharsets.UTF_8), outSplitQuery[0]);
        Assertions.assertEquals("start=" + start, outSplitQuery[1]);
        Assertions.assertEquals("end=" + end, outSplitQuery[2]);
        Assertions.assertEquals("step=2m", outSplitQuery[3]);
    }

}
