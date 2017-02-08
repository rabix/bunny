package org.rabix.engine.jdbi.impl;

import java.lang.annotation.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.rabix.bindings.model.dag.DAGNode;
import org.rabix.common.json.BeanSerializer;
import org.rabix.engine.jdbi.impl.JDBIDAGRepository.DAGNodeMapper;
import org.rabix.engine.repository.DAGRepository;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

@RegisterMapper(DAGNodeMapper.class)
public interface JDBIDAGRepository extends DAGRepository {

  @SqlUpdate("insert into dag_node (root_id,dag) values (:root_id,:dag::jsonb)")
  void insert(@Bind("root_id") UUID rootId, @BindDAGNode DAGNode dag);
  
  @SqlQuery("WITH RECURSIVE flattened AS (\n" +
      "SELECT\n" +
      "        null AS parent,\n" +
      "        replace(cast (dag->'name' AS TEXT), '\"', '') AS name,\n" +
      "        dag AS node,\n" +
      "        dag->'isContainer' AS is_container,\n" +
      "        root_id\n" +
      "FROM dag_node WHERE ( dag->>'children' ) IS NOT NULL AND root_id=:root_id \n" +
      "UNION ALL\n" +
      "SELECT\n" +
      "        replace(cast (f.node->'name' AS TEXT), '\"', '') AS parent,\n" +
      "        replace(cast (jsonb_array_elements(f.node->'children')->'name' AS TEXT), '\"', '') AS name,\n" +
      "        jsonb_array_elements(f.node->'children') AS node,\n" +
      "        jsonb_array_elements(f.node->'children')->'isContainer' AS is_container,\n" +
      "        f.root_id\n" +
      "FROM flattened f WHERE (f.node->'children') IS NOT NULL\n" +
      ")\n" +
      "SELECT parent, name, node, is_container, root_id FROM flattened\n" +
      "WHERE name=:name AND root_id=:root_id \n" +
      "UNION\n" +
      "SELECT null AS parent, 'root' AS name, dag AS node, 'false' AS is_container, root_id\n" +
      "FROM dag_node WHERE root_id = :root_id AND dag->>'type' = 'EXECUTABLE'")
  DAGNode get(@Bind("name") String name, @Bind("root_id") UUID rootId);
  
  public static class DAGNodeMapper implements ResultSetMapper<DAGNode> {
    public DAGNode map(int index, ResultSet r, StatementContext ctx) throws SQLException {
      return BeanSerializer.deserialize(r.getString("node"), DAGNode.class);
    }
  }

  @BindingAnnotation(JDBIDAGRepository.BindDAGNode.DagBinderFactory.class)
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.PARAMETER })
  public static @interface BindDAGNode {
    public static class DagBinderFactory implements BinderFactory<Annotation> {
      public Binder<JDBIDAGRepository.BindDAGNode, DAGNode> build(Annotation annotation) {
        return new Binder<JDBIDAGRepository.BindDAGNode, DAGNode>() {
          public void bind(SQLStatement<?> q, JDBIDAGRepository.BindDAGNode bind, DAGNode dag) {
            q.bind("dag", BeanSerializer.serializeFull(dag));
          }
        };
      }
    }
  }
}
