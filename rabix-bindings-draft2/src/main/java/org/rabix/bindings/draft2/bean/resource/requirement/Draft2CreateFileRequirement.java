package org.rabix.bindings.draft2.bean.resource.requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.draft2.bean.Draft2Job;
import org.rabix.bindings.draft2.bean.resource.Draft2Resource;
import org.rabix.bindings.draft2.bean.resource.Draft2ResourceType;
import org.rabix.bindings.draft2.expression.Draft2ExpressionException;
import org.rabix.bindings.draft2.expression.helper.Draft2ExpressionBeanHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;

public class Draft2CreateFileRequirement extends Draft2Resource {

  public final static String KEY_FILE_DEF = "fileDef";
  public final static String KEY_FILENAME = "filename";
  public final static String KEY_FILE_CONTENT = "fileContent";

  @JsonIgnore
  public List<Draft2FileRequirement> getFileRequirements() {
    List<Map<String, Object>> fileDefs = getValue(KEY_FILE_DEF);

    if (fileDefs == null) {
      return null;
    }

    List<Draft2FileRequirement> fileRequirements = new ArrayList<>();

    for (Map<String, Object> fileDef : fileDefs) {
      Object filename = getFilename(fileDef);
      Object content = getFileContent(fileDef);
      fileRequirements.add(new Draft2FileRequirement(filename, content));
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
  public class Draft2FileRequirement {
    private Object filename;
    private Object content;

    public Draft2FileRequirement(Object filename, Object content) {
      this.filename = filename;
      this.content = content;
    }

    public Object getContent(Draft2Job job) throws Draft2ExpressionException {
      if (Draft2ExpressionBeanHelper.isExpression(content)) {
        return Draft2ExpressionBeanHelper.evaluate(job, content);
      }
      return content;
    }

    public Object getFilename(Draft2Job job) throws Draft2ExpressionException {
      if (Draft2ExpressionBeanHelper.isExpression(filename)) {
        return Draft2ExpressionBeanHelper.evaluate(job, filename);
      }
      return filename;
    }

    @Override
    public String toString() {
      return "FileRequirement [filename=" + filename + ", content=" + content + "]";
    }

  }

  @Override
  @JsonIgnore
  public Draft2ResourceType getTypeEnum() {
    return Draft2ResourceType.CREATE_FILE_REQUIREMENT;
  }

  @Override
  public String toString() {
    return "CreateFileRequirement [" + raw + "]";
  }
}
