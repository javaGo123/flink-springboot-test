/*
 * Copyright 2018 data Artisans GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sunnyday.idea.analyticsquery;

import com.sunnyday.idea.utils.GeoUtils;
import com.sunnyday.idea.utils.TaxiRide;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import com.sunnyday.idea.utils.TaxiRides;
import org.springframework.stereotype.Component;

/**
 * This example application runs a SQL query on a stream of taxi ride events.
 *
 * The query computes the average total amount paid per drop off-location and hour of day.
 * We discretize the coordinates of the drop-off location into cells of 250x250 meters.
 *
 * The stream of taxi ride events is read from a gzipped CSV file.
 * The file can be downloaded from
 *
 * https://drive.google.com/file/d/0B0TBL8JNn3JgTGNJTEJaQmFMbk0
 *
 * When starting the application the path to the file has to be provided as a parameter.
 */
/**
 * @author wangxinxin
 * @date 2019/7/9
 */
@Component
public class AreasTotalPerHourOfDay {

    public static void calculate(String inputPath) throws Exception {

        // create execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // configure event-time and watermarks
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.getConfig().setAutoWatermarkInterval(1000L);

        // create table environment
        StreamTableEnvironment tEnv = TableEnvironment.getTableEnvironment(env);
        // register user-defined function
        tEnv.registerFunction("toCellId", new GeoUtils.ToCellId());

        // get taxi ride event stream
        DataStream<TaxiRide> rides = TaxiRides.getRides(env, inputPath);
        // register taxi ride event stream as table "Rides"
        tEnv.registerDataStream(
            "Rides",
            rides,
            "medallion, licenseId, pickUpTime, dropOffTime.rowtime, " +
                "pickUpLon, pickUpLat, dropOffLon, dropOffLat, total");

        // define SQL query to compute average total per area and hour of day.
        Table result = tEnv.sqlQuery(
            "SELECT " +
            "  toCellId(dropOffLon, dropOffLat) AS area, " +
            "  EXTRACT(HOUR FROM dropOffTime) AS hourOfDay, " +
            "  AVG(total) AS avgTotal " +
            "FROM Rides " +
            "GROUP BY " +
            "  toCellId(dropOffLon, dropOffLat), " +
            "  EXTRACT(HOUR FROM dropOffTime)"
        );

        // convert result table into a retraction stream and print it
        tEnv.toRetractStream(result, Row.class)
                .print();

        // execute the query
        env.execute();
    }
}
