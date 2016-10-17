package org.rabix.bindings.sb.bean.resource.requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.sb.bean.SBJob;
import org.rabix.bindings.sb.bean.resource.SBResource;
import org.rabix.bindings.sb.bean.resource.SBResourceType;
import org.rabix.bindings.sb.expression.SBExpressionException;
import org.rabix.bindings.sb.expression.helper.SBExpressionBeanHelper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;

public class SBCreateFileRequirement extends SBResource {

  public final static String KEY_FILE_DEF = "fileDef";
  public final static String KEY_FILENAME = "filename";
  public final static String KEY_FILE_CONTENT = "fileContent";

  @JsonIgnore
  public List<SBFileRequirement> getFileRequirements() {
    List<Map<String, Object>> fileDefs = getValue(KEY_FILE_DEF);

    if (fileDefs == null) {
      return null;
    }

    List<SBFileRequirement> fileRequirements = new ArrayList<>();

    for (Map<String, Object> fileDef : fileDefs) {
      Object filename = getFilename(fileDef);
      Object content = getFileContent(fileDef);
      fileRequirements.add(new SBFileRequirement(filename, content));
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
  public class SBFileRequirement {
    private Object filename;
    private Object content;

    public SBFileRequirement(Object filename, Object content) {
      this.filename = filename;
      this.content = content;
    }

    public Object getContent(SBJob job) throws SBExpressionException {
      if (SBExpressionBeanHelper.isExpression(content)) {
        return SBExpressionBeanHelper.evaluate(job, content);
      }
      return content;
    }

    public Object getFilename(SBJob job) throws SBExpressionException {
      if (SBExpressionBeanHelper.isExpression(filename)) {
        return SBExpressionBeanHelper.evaluate(job, filename);
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
  public SBResourceType getTypeEnum() {
    return SBResourceType.CREATE_FILE_REQUIREMENT;
  }

  @Override
  public String toString() {
    return "CreateFileRequirement [" + raw + "]";
  }
}
