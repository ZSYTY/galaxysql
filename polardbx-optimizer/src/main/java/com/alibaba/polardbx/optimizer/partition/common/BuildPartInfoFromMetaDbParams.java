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

package com.alibaba.polardbx.optimizer.partition.common;

import com.alibaba.polardbx.gms.partition.TablePartitionConfig;
import com.alibaba.polardbx.gms.tablegroup.PartitionGroupRecord;
import com.alibaba.polardbx.optimizer.config.table.ColumnMeta;

import java.util.List;
import java.util.Map;

/**
 * @author chenghui.lch
 */
public class BuildPartInfoFromMetaDbParams {
    protected TablePartitionConfig tbPartConf;
    protected List<ColumnMeta> allColumnMetas;
    protected Map<Long, PartitionGroupRecord> partitionGroupRecordsMap;

    public BuildPartInfoFromMetaDbParams() {
    }

    public TablePartitionConfig getTbPartConf() {
        return tbPartConf;
    }

    public void setTbPartConf(TablePartitionConfig tbPartConf) {
        this.tbPartConf = tbPartConf;
    }

    public List<ColumnMeta> getAllColumnMetas() {
        return allColumnMetas;
    }

    public void setAllColumnMetas(List<ColumnMeta> allColumnMetas) {
        this.allColumnMetas = allColumnMetas;
    }

    public Map<Long, PartitionGroupRecord> getPartitionGroupRecordsMap() {
        return partitionGroupRecordsMap;
    }

    public void setPartitionGroupRecordsMap(
        Map<Long, PartitionGroupRecord> partitionGroupRecordsMap) {
        this.partitionGroupRecordsMap = partitionGroupRecordsMap;
    }
}
