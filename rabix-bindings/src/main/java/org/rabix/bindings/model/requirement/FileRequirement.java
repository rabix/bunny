package org.rabix.bindings.model.requirement;

import java.util.List;

import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;

public class FileRequirement extends Requirement {

  private final List<SingleFileRequirement> fileRequirements;

  public FileRequirement(List<SingleFileRequirement> fileRequirements) {
    this.fileRequirements = fileRequirements;
  }

  public List<SingleFileRequirement> getFileRequirements() {
    return fileRequirements;
  }
  
  @Override
  public boolean isCustom() {
    return false;
  }

  @Override
  public Object getData() {
    return null;
  }

  @Override
  public String getType() {
    return FILE_REQUIREMENT_TYPE;
  }
  
  @Override
  public String toString() {
    return "FileRequirement [fileRequirements=" + fileRequirements + "]";
  }

  public static class SingleFileRequirement {
    private String filename;

    public SingleFileRequirement(String filename) {
      this.filename = filename;
    }

    public String getFilename() {
      return filename;
    }

    @Override
    public String toString() {
      return "SingleFileRequirement [filename=" + filename + "]";
    }
    
  }

  public static class SingleTextFileRequirement extends SingleFileRequirement {

    private String content;

    public SingleTextFileRequirement(String filename, String content) {
      super(filename);
      this.content = content;
    }

    public String getContent() {
      return content;
    }

    @Override
    public String toString() {
      return "SingleTextFileRequirement [content=" + content + "]";
    }
    
  }

  public static class SingleInputFileRequirement extends SingleFileRequirement {

    protected FileValue content;
    protected boolean linkEnabled;

    public SingleInputFileRequirement(String filename, FileValue content, boolean linkEnabled) {
      super(filename);
      this.content = content;
      this.linkEnabled = linkEnabled;
    }

    public FileValue getContent() {
      return content;
    }

    public boolean isLinkEnabled() {
      return linkEnabled;
    }
    
    @Override
    public String toString() {
      return "SingleInputFileRequirement [content=" + content + ", linkEnabled=" + linkEnabled + "]";
    }
    
  }
  
  public static class SingleInputDirectoryRequirement extends SingleInputFileRequirement {

    public SingleInputDirectoryRequirement(String filename, DirectoryValue content, boolean isLinkEnabled) {
      super(filename, content, isLinkEnabled);
    }

    @Override
    public String toString() {
      return "SingleInputDirectoryRequirement [content=" + content + ", linkEnabled=" + linkEnabled + "]";
    }
    
  }

}
