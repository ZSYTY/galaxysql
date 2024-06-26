/*
 * Copyright [2013-2021], Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.polardbx.optimizer.utils;

import com.alibaba.polardbx.common.jdbc.IConnection;
import com.alibaba.polardbx.common.jdbc.IDataSource;
import com.alibaba.polardbx.common.jdbc.MasterSlave;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public interface IConnectionHolder {

    IConnection getConnection(String schema, String group, IDataSource ds) throws SQLException;

    IConnection getConnection(String schema, String group, IDataSource ds, MasterSlave masterSlave)
        throws SQLException;

    void closeAllConnections();

    void tryClose(IConnection conn, String groupName) throws SQLException;

    Collection<IConnection> getAllConnection();

    /**
     * Find held groups of a schema
     */
    default List<String> getHeldGroupsOfSchema(String schema) {
        return Collections.emptyList();
    }

    void kill();

    /**
     * For each physical connections, get group and connection id, and consume them.
     */
    void handleConnIds(BiConsumer<String, Long> consumer);

    Set<String> getHeldSchemas();

}
