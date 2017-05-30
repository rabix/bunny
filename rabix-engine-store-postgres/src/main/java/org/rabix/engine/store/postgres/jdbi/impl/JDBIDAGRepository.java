package org.rabix.engine.store.postgres.jdbi.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.store.repository.DAGRepository;
import org.rabix.engine.store.postgres.jdbi.bindings.BindJson;
import org.rabix.engine.store.postgres.jdbi.impl.JDBIDAGRepository.DAGNodeMapper;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(DAGNodeMapper.class)
public interface JDBIDAGRepository extends DAGRepository {

  @Override
  @SqlUpdate("insert into dag_node (id,dag) values (:id,:dag::jsonb)")
  void insert(@Bind("id") UUID id, @BindJson("dag") DAGNode dag);
  
  @Override
  @SqlQuery("WITH RECURSIVE flattened AS (\r\n\tSELECT null AS parent, replace(cast (dag->'id' AS TEXT), '\"', '') AS id, dag AS node, dag->'isContainer' AS is_container, id AS root_external_id\r\n\tFROM dag_node WHERE ( dag->>'children' ) IS NOT NULL AND id=:root_id \r\nUNION ALL\r\n\tSELECT replace(cast (f.node->'id' AS TEXT), '\"', '') AS parent, replace(cast (jsonb_array_elements(f.node->'children')->'id' AS TEXT), '\"', '') AS id, jsonb_array_elements(f.node->'children') AS node, jsonb_array_elements(f.node->'children')->'isContainer' AS is_container, f.root_external_id AS root_external_id\r\n\tFROM flattened f WHERE (f.node->'children') IS NOT NULL\r\n)\r\nSELECT parent, id, node, is_container, root_external_id FROM flattened\r\nWHERE id=:id AND root_external_id=:root_id \r\nUNION\r\nSELECT null AS parent, 'root' AS id, dag AS node, 'false' AS is_container, id AS root_external_id\r\n\tFROM dag_node WHERE id = :root_id AND dag->>'type' = 'EXECUTABLE'\r\n")
  DAGNode get(@Bind("id") String id, @Bind("root_id") UUID rootId);
  
  public static class DAGNodeMapper implements ResultSetMapper<DAGNode> {
    public DAGNode map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      return BeanSerializer.deserialize(r.getString("node"), DAGNode.class);
    }
  }
}
