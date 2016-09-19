package org.rabix.bindings.draft3.bean.resource.requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.draft3.bean.Draft3Job;
import org.rabix.bindings.draft3.bean.resource.Draft3Resource;
import org.rabix.bindings.draft3.bean.resource.Draft3ResourceType;
import org.rabix.bindings.draft3.expression.Draft3ExpressionException;
import org.rabix.bindings.draft3.expression.Draft3ExpressionResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;

public class Draft3CreateFileRequirement extends Draft3Resource {

  public final static String KEY_FILE_DEF = "fileDef";
  public final static String KEY_FILENAME = "filename";
  public final static String KEY_FILE_CONTENT = "fileContent";

  @JsonIgnore
  public List<Draft3FileRequirement> getFileRequirements() {
    List<Map<String, Object>> fileDefs = getValue(KEY_FILE_DEF);

    if (fileDefs == null) {
      return null;
    }

    List<Draft3FileRequirement> fileRequirements = new ArrayList<>();

    for (Map<String, Object> fileDef : fileDefs) {
      Object filename = getFilename(fileDef);
      Object content = getFileContent(fileDef);
      fileRequirements.add(new Draft3FileRequirement(filename, content));
    }
    return fileRequirements;
  }

  @JsonIgnore
  private Object getFilename(Map<String, Object> fileDef) {
    Preconditions.checkNotNull(fileDef);
    return fileDef.get(KEY_FILENAME);
  }

  @JsonIgnore
  private Object getFileContent(Map<String, Object> fileDef) {
    Preconditions.checkNotNull(fileDef);
    return fileDef.get(KEY_FILE_CONTENT);
  }

  /**
   * Single file requirement
   */
  public class Draft3FileRequirement {
    private Object filename;
    private Object content;

    public Draft3FileRequirement(Object filename, Object content) {
      this.filename = filename;
      this.content = content;
    }

    public Object getContent(Draft3Job job) throws Draft3ExpressionException {
      return Draft3ExpressionResolver.resolve(content, job, null);
    }

    public Object getFilename(Draft3Job job) throws Draft3ExpressionException {
      return Draft3ExpressionResolver.resolve(filename, job, null);
    }

    @Override
    public String toString() {
      return "Draft3FileRequirement [filename=" + filename + ", content=" + content + "]";
    }

  }

  @Override
  @JsonIgnore
  public Draft3ResourceType getType() {
    return Draft3ResourceType.CREATE_FILE_REQUIREMENT;
  }

  @Override
  public String toString() {
    return "Draft3CreateFileRequirement [" + raw + "]";
  }
}
