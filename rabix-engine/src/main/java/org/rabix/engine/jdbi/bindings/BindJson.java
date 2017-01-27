package org.rabix.engine.jdbi.bindings;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.SQLException;

import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

@BindingAnnotation(BindJson.JsonBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface BindJson {
  String value();

  public static class JsonBinderFactory implements BinderFactory<Annotation> {

    public Binder<BindJson, String> build(Annotation annotation) {
      return new Binder<BindJson, String>() {
        public void bind(SQLStatement<?> q, BindJson bind, String jsonString) {
          try {
            PGobject data = new PGobject();
            data.setType("jsonb");
            data.setValue(jsonString);
            q.bind(bind.value(), data);
          } catch (SQLException ex) {
            throw new IllegalStateException("Error Binding JSON", ex);
          }
        }
      };
    }

  }
}