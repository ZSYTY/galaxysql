/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.sql;

import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.util.ImmutableNullableList;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Parse tree for {@code CREATE MATERIALIZED VIEW} statement.
 */
public class SqlCreateMaterializedView extends SqlCreate {
  public final @Nullable SqlNodeList columnList;
  public final SqlNode query;
  public final boolean bRefresh;

  private static final SqlOperator OPERATOR =
      new SqlSpecialOperator("CREATE MATERIALIZED VIEW",
          SqlKind.CREATE_MATERIALIZED_VIEW);

  /** Creates a SqlCreateView. */
  public SqlCreateMaterializedView(SqlParserPos pos, SqlIdentifier name, @Nullable SqlNodeList columnList,
                            SqlNode query, boolean bRefresh) {
    super(OPERATOR, pos, false, false);
    this.name = Objects.requireNonNull(name, "name");
    this.columnList = columnList; // may be null
    this.query = Objects.requireNonNull(query, "query");
    this.bRefresh = bRefresh;
  }

  @SuppressWarnings("nullness")
  @Override public List<SqlNode> getOperandList() {
    return ImmutableNullableList.of(name, columnList, query);
  }

  @Override public void unparse(SqlWriter writer, int leftPrec, int rightPrec) {
    writer.keyword("CREATE");
    writer.keyword("MATERIALIZED VIEW");
    if (ifNotExists) {
      writer.keyword("IF NOT EXISTS");
    }
    name.unparse(writer, leftPrec, rightPrec);
    if (columnList != null) {
      SqlWriter.Frame frame = writer.startList("(", ")");
      for (SqlNode c : columnList) {
        writer.sep(",");
        c.unparse(writer, 0, 0);
      }
      writer.endList(frame);
    }
    writer.keyword("AS");
    writer.newlineAndIndent();
    query.unparse(writer, 0, 0);
  }
}
