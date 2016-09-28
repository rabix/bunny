package org.rabix.bindings.model.requirement;

import java.util.List;

import org.rabix.bindings.model.DirectoryValue;
import org.rabix.bindings.model.FileValue;

public class FileRequirement implements Requirement {

  private final List<SingleFileRequirement> fileRequirements;

  public FileRequirement(List<SingleFileRequirement> fileRequirements) {
    this.fileRequirements = fileRequirements;
  }

  public List<SingleFileRequirement> getFileRequirements() {
    return fileRequirements;
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

    private FileValue content;

    public SingleInputFileRequirement(String filename, FileValue content) {
      super(filename);
      this.content = content;
    }

    public FileValue getContent() {
      return content;
    }

    @Override
    public String toString() {
      return "SingleInputFileRequirement [content=" + content + "]";
    }
    
  }
  
  public static class SingleInputDirectoryRequirement extends SingleInputFileRequirement {

    private DirectoryValue content;

    public SingleInputDirectoryRequirement(String filename, DirectoryValue content) {
      super(filename, content);
    }

    public FileValue getContent() {
      return content;
    }

    @Override
    public String toString() {
      return "SingleInputDirectoryRequirement [content=" + content + "]";
    }
    
  }
}
