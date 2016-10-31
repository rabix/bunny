package org.rabix.bindings.cwl.bean.resource.requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.rabix.bindings.cwl.bean.CWLJob;
import org.rabix.bindings.cwl.bean.resource.CWLResource;
import org.rabix.bindings.cwl.bean.resource.CWLResourceType;
import org.rabix.bindings.cwl.expression.CWLExpressionException;
import org.rabix.bindings.cwl.expression.CWLExpressionResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;

public class CWLCreateFileRequirement extends CWLResource {

  public final static String KEY_FILE_DEF = "fileDef";
  public final static String KEY_FILENAME = "filename";
  public final static String KEY_FILE_CONTENT = "fileContent";

  @JsonIgnore
  public List<CWLFileRequirement> getFileRequirements() {
    List<Map<String, Object>> fileDefs = getValue(KEY_FILE_DEF);

    if (fileDefs == null) {
      return null;
    }

    List<CWLFileRequirement> fileRequirements = new ArrayList<>();

    for (Map<String, Object> fileDef : fileDefs) {
      Object filename = getFilename(fileDef);
      Object content = getFileContent(fileDef);
      fileRequirements.add(new CWLFileRequirement(filename, content));
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
  public class CWLFileRequirement {
    private Object filename;
    private Object content;

    public CWLFileRequirement(Object filename, Object content) {
      this.filename = filename;
      this.content = content;
    }

    public Object getContent(CWLJob job) throws CWLExpressionException {
      return CWLExpressionResolver.resolve(content, job, null);
    }

    public Object getFilename(CWLJob job) throws CWLExpressionException {
      return CWLExpressionResolver.resolve(filename, job, null);
    }

    @Override
    public String toString() {
      return "CWLFileRequirement [filename=" + filename + ", content=" + content + "]";
    }

  }

  @Override
  @JsonIgnore
  public CWLResourceType getTypeEnum() {
    return CWLResourceType.CREATE_FILE_REQUIREMENT;
  }

  @Override
  public String toString() {
    return "CWLCreateFileRequirement [" + raw + "]";
  }
}
